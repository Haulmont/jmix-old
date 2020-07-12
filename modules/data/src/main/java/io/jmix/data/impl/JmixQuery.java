/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.data.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.jmix.core.Id;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.EntityFetcher;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.entitycache.QueryCacheManager;
import io.jmix.data.impl.entitycache.QueryKey;
import io.jmix.data.persistence.DbmsFeatures;
import io.jmix.data.persistence.DbmsSpecifics;
import org.eclipse.persistence.config.CascadePolicy;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.helper.CubaUtil;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JmixQuery<E> implements TypedQuery<E> {

    private static final Logger log = LoggerFactory.getLogger(JmixQuery.class);

    private final EntityManager entityManager;
    private Class<E> resultClass;

    protected Environment environment;
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected ExtendedEntities extendedEntities;
    protected FetchPlanRepository viewRepository;
    protected PersistenceSupport support;
    protected FetchGroupManager fetchGroupMgr;
    protected EntityFetcher entityFetcher;
    protected QueryCacheManager queryCacheMgr;
    protected QueryTransformerFactory queryTransformerFactory;
    protected QueryHintsProcessor hintsProcessor;
    protected DbmsSpecifics dbmsSpecifics;
    protected Collection<QueryMacroHandler> macroHandlers;

    protected JpaQuery query;
    protected boolean isNative;
    protected String queryString;
    protected String transformedQueryString;
    protected Set<Param> params = new HashSet<>();
    protected Map<String, Object> hints;
    protected LockModeType lockMode;
    protected List<FetchPlan> fetchPlans = new ArrayList<>();
    protected Integer maxResults;
    protected Integer firstResult;
    protected boolean singleResultExpected;
    protected boolean cacheable;
    protected FlushModeType flushMode;

    public JmixQuery(EntityManager entityManager, BeanLocator beanLocator, boolean isNative, String qlString,
                     @Nullable Class<E> resultClass) {
        this.entityManager = entityManager;
        this.isNative = isNative;
        this.queryString = qlString;
        this.resultClass = resultClass;

        environment = beanLocator.get(Environment.class);
        metadata = beanLocator.get(Metadata.NAME);
        metadataTools = beanLocator.get(MetadataTools.NAME);
        extendedEntities = beanLocator.get(ExtendedEntities.NAME);
        viewRepository = beanLocator.get(FetchPlanRepository.NAME);
        support = beanLocator.get(PersistenceSupport.NAME);
        fetchGroupMgr = beanLocator.get(FetchGroupManager.NAME);
        entityFetcher = beanLocator.get(EntityFetcher.NAME);
        queryCacheMgr = beanLocator.get(QueryCacheManager.NAME);
        queryTransformerFactory = beanLocator.get(QueryTransformerFactory.NAME);
        hintsProcessor = beanLocator.get(QueryHintsProcessor.NAME);
        dbmsSpecifics = beanLocator.get(DbmsSpecifics.NAME);
        macroHandlers = beanLocator.getAll(QueryMacroHandler.class).values();
    }

    @Override
    public List<E> getResultList() {
        if (log.isDebugEnabled())
            log.debug(queryString.replaceAll("[\\t\\n\\x0B\\f\\r]", " "));

        singleResultExpected = false;

        JpaQuery<E> query = getQuery();
        preExecute(query);

        @SuppressWarnings("unchecked")
        List<E> resultList = (List<E>) getResultFromCache(query, false, obj -> {
            ((List) obj).stream().filter(item -> item instanceof JmixEntity).forEach(item -> {
                for (FetchPlan fetchPlan : fetchPlans) {
                    entityFetcher.fetch((JmixEntity) item, fetchPlan);
                }
            });
        });
        return resultList;
    }

    @Override
    public E getSingleResult() {
        if (log.isDebugEnabled())
            log.debug(queryString.replaceAll("[\\t\\n\\x0B\\f\\r]", " "));

        singleResultExpected = true;

        JpaQuery<E> jpaQuery = getQuery();
        preExecute(jpaQuery);

        @SuppressWarnings("unchecked")
        E result = (E) getResultFromCache(jpaQuery, true, obj -> {
            if (obj instanceof JmixEntity) {
                for (FetchPlan fetchPlan : fetchPlans) {
                    entityFetcher.fetch((JmixEntity) obj, fetchPlan);
                }
            }
        });
        return result;
    }

    @Override
    public TypedQuery<E> setMaxResults(int maxResult) {
        this.maxResults = maxResult;
        if (query != null)
            query.setMaxResults(maxResult);
        return this;
    }

    @Override
    public TypedQuery<E> setFirstResult(int startPosition) {
        this.firstResult = startPosition;
        if (query != null)
            query.setFirstResult(startPosition);
        return this;
    }

    @Override
    public TypedQuery<E> setHint(String hintName, Object value) {
        if (PersistenceHints.FETCH_PLAN.equals(hintName)) {
            if (isNative)
                throw new UnsupportedOperationException("FetchPlan is not supported for native queries");
            if (value == null) {
                fetchPlans.clear();
            } else if (value instanceof Collection) {
                //noinspection unchecked
                fetchPlans.addAll((Collection) value);
            } else {
                fetchPlans.add((FetchPlan) value);
            }
        } else if (PersistenceHints.CACHEABLE.equals(hintName)) {
            cacheable = (boolean) value;
        }
        if (hints == null) {
            hints = new HashMap<>();
        }
        hints.put(hintName, value);
        return this;
    }

    @Override
    public <T> TypedQuery<E> setParameter(Parameter<T> param, T value) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<E> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<E> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedQuery<E> setParameter(String name, Object value) {
        return internalSetParameter(name, value);
    }

    @Override
    public TypedQuery<E> setParameter(String name, Calendar value, TemporalType temporalType) {
        checkState();
        params.add(new Param(name, value.getTime(), temporalType));
        return this;
    }

    @Override
    public TypedQuery<E> setParameter(String name, Date value, TemporalType temporalType) {
        checkState();
        params.add(new Param(name, value, temporalType));
        return this;
    }

    @Override
    public TypedQuery<E> setParameter(int position, Object value) {
        return internalSetParameter(position, value);
    }

    @Override
    public TypedQuery<E> setParameter(int position, Calendar value, TemporalType temporalType) {
        checkState();
        params.add(new Param(position, value.getTime(), temporalType));
        return this;
    }

    @Override
    public TypedQuery<E> setParameter(int position, Date value, TemporalType temporalType) {
        checkState();
        params.add(new Param(position, value, temporalType));
        return this;
    }

    @Override
    public TypedQuery<E> setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return this;
    }

    @Override
    public TypedQuery<E> setLockMode(LockModeType lockMode) {
        checkState();
        this.lockMode = lockMode;
        return this;
    }

    @Override
    public int executeUpdate() {
        JpaQuery<E> jpaQuery = getQuery();
        DatabaseQuery databaseQuery = jpaQuery.getDatabaseQuery();
        Class referenceClass = databaseQuery.getReferenceClass();
        boolean isDeleteQuery = databaseQuery.isDeleteObjectQuery() || databaseQuery.isDeleteAllQuery();
        boolean enableDeleteInSoftDeleteMode =
                Boolean.parseBoolean(environment.getProperty("jmix.data.enableDeleteStatementInSoftDeleteMode"));
        if (!enableDeleteInSoftDeleteMode && PersistenceHints.isSoftDeletion(entityManager) && isDeleteQuery) {
            if (metadataTools.isSoftDeleted(referenceClass)) {
                throw new UnsupportedOperationException("Delete queries are not supported with enabled soft deletion. " +
                        "Use 'cuba.enableDeleteStatementInSoftDeleteMode' application property to roll back to legacy behavior.");
            }
        }
        // In some cache configurations (in particular, when shared cache is on, but for some entities cache is set to ISOLATED),
        // EclipseLink does not evict updated entities from cache automatically.
        Cache cache = jpaQuery.getEntityManager().getEntityManagerFactory().getCache();
        if (referenceClass != null) {
            cache.evict(referenceClass);
            queryCacheMgr.invalidate(referenceClass, true);
        } else {
            cache.evictAll();
            queryCacheMgr.invalidateAll(true);
        }
        preExecute(jpaQuery);
        return jpaQuery.executeUpdate();
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public Map<String, Object> getHints() {
        return hints == null ? Collections.emptyMap() : hints;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter<?> getParameter(int position) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParameterValue(String name) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParameterValue(int position) {
        // todo JPA query parameters
        throw new UnsupportedOperationException();
    }

    @Override
    public FlushModeType getFlushMode() {
        return flushMode;
    }

    @Override
    public LockModeType getLockMode() {
        return lockMode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(this.getClass())) {
            // unwraps any proxy to JmixQuery
            return (T) this;
        }
        return getQuery().unwrap(cls);
    }

    @Nullable
    public E getSingleResultOrNull() {
        if (log.isDebugEnabled())
            log.debug(queryString.replaceAll("[\\t\\n\\x0B\\f\\r]", " "));

        Integer saveMaxResults = maxResults;
        maxResults = 1;
        try {
            JpaQuery<E> query = getQuery();
            preExecute(query);
            @SuppressWarnings("unchecked")
            List<E> resultList = (List<E>) getResultFromCache(query, false, obj -> {
                List list = (List) obj;
                if (!list.isEmpty()) {
                    Object item = list.get(0);
                    if (item instanceof JmixEntity) {
                        for (FetchPlan fetchPlan : fetchPlans) {
                            entityFetcher.fetch((JmixEntity) item, fetchPlan);
                        }
                    }
                }
            });
            if (resultList.isEmpty()) {
                return null;
            } else {
                return resultList.get(0);
            }
        } finally {
            maxResults = saveMaxResults;
        }
    }

    /**
     * INTERNAL
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * INTERNAL
     */
    public void setQueryString(String queryString) {
        checkState();
        this.queryString = queryString;
    }

    /**
     * INTERNAL
     */
    public void setSingleResultExpected(boolean singleResultExpected) {
        this.singleResultExpected = singleResultExpected;
    }

    private JpaQuery<E> getQuery() {
        if (query == null) {
            FetchPlan fetchPlan = fetchPlans.isEmpty() ? null : fetchPlans.get(0);

            if (isNative) {
                log.trace("Creating SQL query: {}", queryString);
                if (resultClass == null)
                    query = (JpaQuery) entityManager.createNativeQuery(queryString);
                else {
                    if (!JmixEntity.class.isAssignableFrom(resultClass)) {
                        throw new IllegalArgumentException("Non-entity result class for native query is not supported" +
                                " by EclipseLink: " + resultClass);
                    }
                    Class effectiveClass = extendedEntities.getEffectiveClass(resultClass);
                    query = (JpaQuery) entityManager.createNativeQuery(queryString, effectiveClass);
                }
            } else {
                log.trace("Creating JPQL query: {}", queryString);
                transformedQueryString = transformQueryString();
                log.trace("Transformed JPQL query: {}", transformedQueryString);

                Class effectiveClass = getEffectiveResultClass();
                query = buildJPAQuery(transformedQueryString, effectiveClass);
                if (fetchPlan != null) {
                    MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
                    if (!metadataTools.isCacheable(metaClass) || !singleResultExpected) {
                        query.setHint(QueryHints.REFRESH, HintValues.TRUE);
                        query.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeByMapping);
                    }
                }
            }

            if (flushMode == null) {
                if (fetchPlan != null && !fetchPlan.loadPartialEntities()) {
                    query.setFlushMode(FlushModeType.AUTO);
                } else {
                    query.setFlushMode(FlushModeType.COMMIT);
                }
            } else {
                query.setFlushMode(flushMode);
            }

            boolean nullParam = false;
            for (Param param : params) {
                param.apply(query);
                if (param.value == null)
                    nullParam = true;
            }

            addMacroParams(query);

            // disable SQL caching to support "is null" generation
            if (nullParam)
                query.setHint(QueryHints.PREPARE, HintValues.FALSE);

            // Set maxResults and firstResult only if the query is not by ID, otherwise EclipseLink does not select
            // nested collections in some cases
            if (maxResults != null && !singleResultExpected)
                query.setMaxResults(maxResults);
            if (firstResult != null && !singleResultExpected)
                query.setFirstResult(firstResult);

            if (lockMode != null)
                query.setLockMode(lockMode);

            if (hints != null) {
                for (Map.Entry<String, Object> hint : hints.entrySet()) {
                    hintsProcessor.applyQueryHint(query, hint.getKey(), hint.getValue());
                }
            }

            for (int i = 0; i < fetchPlans.size(); i++) {
                if (i == 0)
                    fetchGroupMgr.setFetchPlan(query, queryString, fetchPlans.get(i), singleResultExpected);
                else
                    fetchGroupMgr.addFetchPlan(query, queryString, fetchPlans.get(i), singleResultExpected);
            }
        }
        return query;
    }

    @Nullable
    private Class getEffectiveResultClass() {
        if (resultClass == null) {
            return null;
        }
        if (JmixEntity.class.isAssignableFrom(resultClass)) {
            return extendedEntities.getEffectiveClass(resultClass);
        }
        return resultClass;
    }

    private JpaQuery buildJPAQuery(String queryString, Class<E> resultClass) {
        boolean useJPQLCache = true;
        FetchPlan fetchPlan = fetchPlans.isEmpty() ? null : fetchPlans.get(0);
        if (fetchPlan != null) {
            boolean useFetchGroup = fetchPlan.loadPartialEntities();
            for (FetchPlan it : fetchPlans) {
                FetchGroupDescription description = fetchGroupMgr.calculateFetchGroup(queryString, it, singleResultExpected, useFetchGroup);
                if (description.hasBatches()) {
                    useJPQLCache = false;
                    break;
                }
            }
        }
        if (!useJPQLCache) {
            CubaUtil.setEnabledJPQLParseCache(false);
        }
        try {
            if (resultClass != null) {
                return (JpaQuery) entityManager.createQuery(queryString, resultClass);
            } else {
                return (JpaQuery) entityManager.createQuery(queryString);
            }
        } finally {
            CubaUtil.setEnabledJPQLParseCache(true);
        }
    }

    private String transformQueryString() {
        String result = expandMacros(queryString);

        boolean rebuildParser = false;
        QueryParser parser = queryTransformerFactory.parser(result);

        String entityName = parser.getEntityName();
        Class effectiveClass = extendedEntities.getEffectiveClass(entityName);
        MetaClass effectiveMetaClass = metadata.getClass(effectiveClass);
        String effectiveEntityName = effectiveMetaClass.getName();
        if (!effectiveEntityName.equals(entityName)) {
            QueryTransformer transformer = queryTransformerFactory.transformer(result);
            transformer.replaceEntityName(effectiveEntityName);
            result = transformer.getResult();
            rebuildParser = true;
        }

        if (firstResult != null && firstResult > 0) {
            String storeName = metadataTools.getStoreName(effectiveMetaClass);
            DbmsFeatures dbmsFeatures = dbmsSpecifics.getDbmsFeatures(storeName);
            if (dbmsFeatures.useOrderByForPaging()) {
                QueryTransformer transformer = queryTransformerFactory.transformer(result);
                transformer.addOrderByIdIfNotExists(metadataTools.getPrimaryKeyName(effectiveMetaClass));
                result = transformer.getResult();
                rebuildParser = true;
            }
        }

        result = replaceParams(result, parser);

        if (rebuildParser) {
            parser = queryTransformerFactory.parser(result);
        }
        String nestedEntityName = parser.getOriginalEntityName();
        String nestedEntityPath = parser.getOriginalEntityPath();
        if (nestedEntityName != null) {
            if (parser.isCollectionOriginalEntitySelect()) {
                throw new IllegalStateException(String.format("Collection attributes are not supported in select clause: %s", nestedEntityPath));
            }
            QueryTransformer transformer = queryTransformerFactory.transformer(result);
            transformer.replaceWithSelectEntityVariable("tempEntityAlias");
            transformer.addFirstSelectionSource(String.format("%s tempEntityAlias", nestedEntityName));
            transformer.addWhereAsIs(String.format("tempEntityAlias.id = %s.id", nestedEntityPath));
            transformer.addEntityInGroupBy("tempEntityAlias");
            result = transformer.getResult();
        }

        result = replaceIsNullAndIsNotNullStatements(result);

        return result;
    }

    private String expandMacros(String queryStr) {
        String result = queryStr;
        if (macroHandlers != null) {
            for (QueryMacroHandler handler : macroHandlers) {
                result = handler.expandMacro(result);
            }
        }
        return result;
    }

    private String replaceParams(String query, QueryParser parser) {
        String result = query;
        Set<String> paramNames = Sets.newHashSet(parser.getParamNames());
        for (Iterator<Param> iterator = params.iterator(); iterator.hasNext(); ) {
            Param param = iterator.next();
            String paramName = param.name.toString();
            if (param.value instanceof String) {
                String strValue = (String) param.value;
                if (strValue.startsWith("(?i)")) {
                    result = replaceCaseInsensitiveParam(result, paramName);
                    param.value = strValue.substring(4).toLowerCase();
                }
            }
            if (param.isNamedParam()) {
                paramNames.remove(paramName);
                if (param.value instanceof Collection) {
                    Collection collectionValue = (Collection) param.value;
                    if (collectionValue.isEmpty()) {
                        result = replaceInCollectionParam(result, paramName);
                        iterator.remove();
                    }
                }
                if (param.value == null) {
                    if (parser.isParameterInCondition(paramName)) {
                        result = replaceInCollectionParam(result, paramName);
                        iterator.remove();
                    }
                }
            }
        }
        for (String paramName : paramNames) {
            result = replaceInCollectionParam(result, paramName);
        }
        return result;
    }

    private String replaceCaseInsensitiveParam(String query, String paramName) {
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        transformer.handleCaseInsensitiveParam(paramName);
        return transformer.getResult();
    }

    private String replaceInCollectionParam(String query, String paramName) {
        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        transformer.replaceInCondition(paramName);
        return transformer.getResult();
    }

    private String replaceIsNullAndIsNotNullStatements(String query) {
        Set<Param> replacedParams = new HashSet<>();

        QueryTransformer transformer = queryTransformerFactory.transformer(query);
        params.stream()
                .filter(Param::isNamedParam)
                .map(param -> Maps.immutableEntry(param, transformer.replaceIsNullStatements(
                        param.name.toString(), param.value == null)))
                .filter(Map.Entry::getValue)
                .forEach(entry -> replacedParams.add(entry.getKey()));

        if (replacedParams.isEmpty()) {
            return query;
        }

        String resultQuery = transformer.getResult();

        QueryParser parser = queryTransformerFactory.parser(resultQuery);
        params.removeAll(replacedParams.stream()
                .filter(param -> !parser.isParameterUsedInAnyCondition(param.name.toString()))
                .collect(Collectors.toSet()));

        return resultQuery;
    }

    private void addMacroParams(javax.persistence.TypedQuery jpaQuery) {
        if (macroHandlers != null) {
            for (QueryMacroHandler handler : macroHandlers) {

                Map<String, Object> namedParams = new HashMap<>();
                for (Param param : params) {
                    if (param.name instanceof String)
                        namedParams.put((String) param.name, param.value);
                }

                Map<String, Class> paramsTypes = new HashMap<>();
                for (Parameter<?> parameter : jpaQuery.getParameters()) {
                    if (parameter.getName() != null) {
                        paramsTypes.put(parameter.getName(), parameter.getParameterType());
                    }
                }
                handler.setQueryParams(namedParams);
                handler.setExpandedParamTypes(paramsTypes);

                for (Map.Entry<String, Object> entry : handler.getParams().entrySet()) {
                    jpaQuery.setParameter(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void preExecute(JpaQuery<E> jpaQuery) {
        // copying behaviour of org.eclipse.persistence.internal.jpa.QueryImpl.executeReadQuery()
        DatabaseQuery elDbQuery = ((EJBQueryImpl) jpaQuery).getDatabaseQueryInternal();
        boolean isObjectLevelReadQuery = elDbQuery.isObjectLevelReadQuery();
        if (jpaQuery.getFlushMode() == FlushModeType.AUTO
                && (!isObjectLevelReadQuery || !((ObjectLevelReadQuery) elDbQuery).isReadOnly())) {
            // flush is expected
            support.processFlush(entityManager, true);
        }
    }

    private Object getResultFromCache(JpaQuery jpaQuery, boolean singleResult, Consumer<Object> fetcher) {
        Preconditions.checkNotNull(fetcher);
        boolean useQueryCache = cacheable && !isNative && queryCacheMgr.isEnabled() && lockMode == null;
        Object result;
        if (useQueryCache) {
            QueryParser parser = QueryTransformerFactory.createParser(transformedQueryString);
            String entityName = parser.getEntityName();
            useQueryCache = parser.isEntitySelect(entityName);
            QueryKey queryKey = null;
            if (useQueryCache) {
                queryKey = QueryKey.create(transformedQueryString, PersistenceHints.isSoftDeletion(entityManager), singleResult, jpaQuery);
                result = singleResult ? queryCacheMgr.getSingleResultFromCache(queryKey, fetchPlans) :
                        queryCacheMgr.getResultListFromCache(queryKey, fetchPlans);
                if (result != null) {
                    return result;
                }
            }
            try {
                result = singleResult ? jpaQuery.getSingleResult() : jpaQuery.getResultList();
            } catch (NoResultException | NonUniqueResultException ex) {
                if (useQueryCache && singleResult) {
                    queryCacheMgr.putResultToCache(queryKey, null, entityName, parser.getAllEntityNames(), ex);
                }
                throw ex;
            }
            fetcher.accept(result);
            if (useQueryCache) {
                queryCacheMgr.putResultToCache(queryKey,
                        singleResult ? Collections.singletonList(result) : (List) result,
                        entityName, parser.getAllEntityNames());
            }
        } else {
            result = singleResult ? jpaQuery.getSingleResult() : jpaQuery.getResultList();
            fetcher.accept(result);
        }
        return result;
    }

    private void checkState() {
        if (query != null)
            throw new IllegalStateException("Query delegate has already been created");
    }

    private TypedQuery<E> internalSetParameter(String name, Object value) {
        checkState();

        if (value instanceof Id) {
            value = ((Id) value).getValue();

        } else if (value instanceof Ids) {
            value = ((Ids) value).getValues();

        } else if (value instanceof EnumClass) {
            value = ((EnumClass) value).getId();

        } else if (isCollectionOfEntitiesOrEnums(value)) {
            value = convertToCollectionOfIds(value);

        }
        params.add(new Param(name, value));
        return this;
    }

    private TypedQuery<E> internalSetParameter(int position, Object value) {
        checkState();

        DbmsFeatures dbmsFeatures = dbmsSpecifics.getDbmsFeatures();
        if (isNative && (value instanceof UUID) && (dbmsFeatures.getUuidTypeClassName() != null)) {
            Class<?> c = ReflectionHelper.getClass(dbmsFeatures.getUuidTypeClassName());
            try {
                value = ReflectionHelper.newInstance(c, value);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Error setting parameter value", e);
            }

        } else if (value instanceof EnumClass) {
            value = ((EnumClass) value).getId();

        } else if (isCollectionOfEntitiesOrEnums(value)) {
            value = convertToCollectionOfIds(value);

        }

        params.add(new Param(position, value));
        return this;
    }

    private boolean isCollectionOfEntitiesOrEnums(Object value) {
        return value instanceof Collection
                && ((Collection<?>) value).stream().allMatch(it -> it instanceof JmixEntity || it instanceof EnumClass);
    }

    private Object convertToCollectionOfIds(Object value) {
        return ((Collection<?>) value).stream()
                .map(it -> it instanceof JmixEntity ? EntityValues.getId(((JmixEntity) it)) : ((EnumClass) it).getId())
                .collect(Collectors.toList());
    }

    private static class Param {
        private Object name;
        private Object value;
        private TemporalType temporalType;

        private Class<?> actualParamType;

        public Param(Object name, Object value) {
            this.name = name;
            this.value = value;
        }

        public Param(Object name, Date value, TemporalType temporalType) {
            this.name = name;
            this.value = value;
            this.temporalType = temporalType;
        }

        public void apply(JpaQuery query) {
            if (temporalType != null) {
                if (name instanceof Integer)
                    query.setParameter((int) name, (Date) value, temporalType);
                else
                    query.setParameter((String) name, (Date) value, temporalType);
            } else {
                if (value instanceof Date && !isValidParamType(query))
                    convertValue();

                if (name instanceof Integer)
                    query.setParameter((int) name, value);
                else
                    query.setParameter((String) name, value);
            }
        }

        public boolean isNamedParam() {
            return name instanceof String;
        }

        private boolean isValidParamType(JpaQuery query) {
            if (value == null || query.getDatabaseQuery() == null)
                return true;

            int index = query.getDatabaseQuery().getArguments().indexOf(String.valueOf(name));
            if (index == -1)
                return true;
            actualParamType = query.getDatabaseQuery().getArgumentTypes().get(index);

            if (actualParamType == null)
                return true;

            return actualParamType.isAssignableFrom(value.getClass());
        }

        private void convertValue() {
            if (value == null || actualParamType == null || actualParamType.isAssignableFrom(value.getClass()))
                return;

            // Since ConversionManager incorrectly converts Date into LocalDate or LocalDateTime
            if (value instanceof Date) {
                if (actualParamType == ClassConstants.TIME_LDATE) {
                    Date date = (Date) value;
                    value = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else if (actualParamType == ClassConstants.TIME_LDATETIME) {
                    Date date = (Date) value;
                    value = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            } else {
                ConversionManager conversionManager = ConversionManager.getDefaultManager();
                try {
                    value = conversionManager.convertObject(value, actualParamType);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Param param = (Param) o;
            return name.equals(param.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
