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

package io.jmix.data.impl.entitycache;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.cluster.ClusterListenerAdapter;
import io.jmix.core.cluster.ClusterManager;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.data.DataProperties;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component(QueryCacheManager.NAME)
public class QueryCacheManager {

    public static final String NAME = "data_QueryCacheManager";

    @Autowired
    protected DataProperties properties;
    @Autowired
    protected ClusterManager clusterManager;
    @Autowired
    protected QueryCache queryCache;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected StoreAwareLocator storeAwareLocator;

    protected static final Logger log = LoggerFactory.getLogger(QueryCacheManager.class);

    @PostConstruct
    public void init() {
        clusterManager.addListener(InvalidateQueryCacheMsg.class, new ClusterListenerAdapter<InvalidateQueryCacheMsg>() {
            @Override
            public void receive(InvalidateQueryCacheMsg message) {
                if (message.invalidateAll) {
                    queryCache.invalidateAll();
                } else if (message.queryKey != null) {
                    queryCache.invalidate(message.queryKey);
                } else {
                    queryCache.invalidate(message.typeNames);
                }
            }
        });
    }

    /**
     * Returns true if query cache enabled
     */
    public boolean isEnabled() {
        return properties.isQueryCacheEnabled();
    }

    /**
     * Get query results from query cache by specified {@code queryKey}
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getResultListFromCache(QueryKey queryKey, List<FetchPlan> views) {
        log.debug("Looking for query in cache: {}", queryKey.printDescription());
        List<T> resultList = null;
        QueryResult queryResult = queryCache.get(queryKey);
        if (queryResult != null) {
            MetaClass metaClass = metadata.getClass(queryResult.getType());
            String storeName = metadataTools.getStoreName(metaClass);
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            resultList = new ArrayList<>(queryResult.getResult().size());
            if (!metadataTools.isCacheable(metaClass)) {
                log.warn("Using cacheable query without entity cache for {}", queryResult.getType());
            }
            for (Object id : queryResult.getResult()) {
                resultList.add(em.find(metaClass.getJavaClass(), id, PersistenceHints.builder().withFetchPlans(views).build()));
            }
        } else {
            log.debug("Query results are not found in cache: {}", queryKey.printDescription());
        }
        return resultList;
    }

    /**
     * Get single query results from query cache by specified {@code queryKey}
     * If query is cached and no results found exception is thrown
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingleResultFromCache(QueryKey queryKey, List<FetchPlan> views) {
        log.debug("Looking for query in cache: {}", queryKey.printDescription());
        QueryResult queryResult = queryCache.get(queryKey);
        if (queryResult != null) {
            MetaClass metaClass = metadata.getClass(queryResult.getType());
            if (!metadataTools.isCacheable(metaClass)) {
                log.warn("Using cacheable query without entity cache for {}", queryResult.getType());
            }
            if (queryResult.getException() != null) {
                RuntimeException ex = queryResult.getException();
                ex.fillInStackTrace();
                throw queryResult.getException();
            }
            String storeName = metadataTools.getStoreName(metaClass);
            EntityManager em = storeAwareLocator.getEntityManager(storeName);
            for (Object id : queryResult.getResult()) {
                return (T) em.find(metaClass.getJavaClass(), id, PersistenceHints.builder().withFetchPlans(views).build());
            }
        }
        log.debug("Query results are not found in cache: {}", queryKey.printDescription());
        return null;
    }


    /**
     * Put query results into query cache for specified query {@code queryKey}.
     * Results are extracted as identifiers from {@code resultList}
     *
     * @param type         - result entity type (metaClass name)
     * @param relatedTypes - query dependent types (metaClass names). It's a list of entity types used in query
     */
    @SuppressWarnings("unchecked")
    public void putResultToCache(QueryKey queryKey, List resultList, String type, Set<String> relatedTypes) {
        QueryResult queryResult;
        if (resultList.size() > 0) {
            List idList = (List) resultList.stream()
                    .filter(item -> item instanceof JmixEntity)
                    .map(item -> EntityValues.getId(((JmixEntity) item)))
                    .collect(Collectors.toList());
            queryResult = new QueryResult(idList, type, getDescendants(relatedTypes));
        } else {
            queryResult = new QueryResult(Collections.emptyList(), type, getDescendants(relatedTypes));
        }
        log.debug("Put results into cache for query: {}, relatedTypes: {}", queryKey.printDescription(), relatedTypes);
        queryCache.put(queryKey, queryResult);
    }

    /**
     * Put query results into query cache for specified query {@code queryKey}.
     * Results are extracted as identifiers from entity {@code result}
     *
     * @param type         - result entity type (metaClass name)
     * @param relatedTypes - query dependent types (metaClass names). It's a list of entity types used in query
     * @param exception    - store exception in the query cache if {@link TypedQuery#getSingleResult()} throws exception
     */
    @SuppressWarnings("unchecked")
    public <T> void putResultToCache(QueryKey queryKey, T result, String type, Set<String> relatedTypes, RuntimeException exception) {
        QueryResult queryResult;
        if (exception == null) {
            queryResult = new QueryResult(Collections.singletonList(EntityValues.getId(((JmixEntity) result))), type, relatedTypes);
        } else {
            queryResult = new QueryResult(Collections.emptyList(), type, relatedTypes, exception);
        }
        log.debug("Put results into cache for query: {}, relatedTypes: {}", queryKey.printDescription(), relatedTypes);
        queryCache.put(queryKey, queryResult);
    }

    /**
     * Discards cached query results for java class (associated with metaClass) {@code typeClass}
     *
     * @param sendInCluster - if true - discard queries results in all query caches in cluster
     */
    public void invalidate(Class typeClass, boolean sendInCluster) {
        if (isEnabled()) {
            MetaClass metaClass = metadata.getClass(typeClass);
            invalidate(metaClass.getName(), sendInCluster);
        }
    }

    /**
     * Discards cached query results for metaClass name {@code typeName}
     *
     * @param sendInCluster - if true - discard queries results in all query caches in cluster
     */
    public void invalidate(String typeName, boolean sendInCluster) {
        if (isEnabled()) {
            queryCache.invalidate(typeName);
            if (sendInCluster) {
                MetaClass metaClass = metadata.findClass(typeName);
                if (metaClass != null && metadataTools.isCacheable(metaClass)) {
                    clusterManager.send(new InvalidateQueryCacheMsg(Sets.newHashSet(typeName)));
                }
            }
        }
    }

    /**
     * Discards cached query results for metaClass names {@code typeNames}
     *
     * @param sendInCluster - if true - discard queries results in all query caches in cluster
     */
    public void invalidate(Set<String> typeNames, boolean sendInCluster) {
        if (isEnabled()) {
            if (typeNames != null && typeNames.size() > 0) {
                queryCache.invalidate(typeNames);
                if (sendInCluster) {
                    boolean hasCacheable = typeNames.stream().anyMatch(typeName -> {
                        MetaClass metaClass = metadata.findClass(typeName);
                        return metaClass != null && metadataTools.isCacheable(metaClass);
                    });
                    if (hasCacheable) {
                        clusterManager.send(new InvalidateQueryCacheMsg(typeNames));
                    }
                }
            }
        }
    }

    /**
     * Discards cached query results for query identifier {@code queryId}
     *
     * @param sendInCluster - if true - discard queries results in all query caches in cluster
     */
    public void invalidate(UUID queryId, boolean sendInCluster) {
        Preconditions.checkNotNull(queryId, "Query identifier is null");
        if (isEnabled()) {
            QueryKey queryKey = queryCache.invalidate(queryId);
            if (queryKey != null && sendInCluster) {
                clusterManager.send(new InvalidateQueryCacheMsg(queryKey));
            }
        }
    }

    /**
     * Discards all query results in the cache.
     *
     * @param sendInCluster - if true - discard queries results in all query caches in cluster
     */
    public void invalidateAll(boolean sendInCluster) {
        if (isEnabled()) {
            queryCache.invalidateAll();
            if (sendInCluster) {
                clusterManager.send(new InvalidateQueryCacheMsg(true));
            }
        }
    }

    protected Set<String> getDescendants(Set<String> relatedTypes) {
        if (relatedTypes == null) return null;
        Set<String> newRelatedTypes = new HashSet<>();
        relatedTypes.forEach(type -> {
            newRelatedTypes.add(type);
            MetaClass metaClass = metadata.getClass(type);
            if (metaClass.getDescendants() != null) {
                Set<String> descendants = metaClass.getDescendants().stream()
                        .filter(it -> it.getJavaClass() != null && !it.getJavaClass().isAnnotationPresent(MappedSuperclass.class))
                        .map(MetadataObject::getName).collect(Collectors.toSet());
                newRelatedTypes.addAll(descendants);
            }
        });
        return newRelatedTypes;
    }

    protected static class InvalidateQueryCacheMsg implements Serializable {
        private static final long serialVersionUID = -9099037380378341477L;

        protected Set<String> typeNames;
        protected QueryKey queryKey;
        protected boolean invalidateAll;

        public InvalidateQueryCacheMsg(Set<String> typeNames) {
            this.typeNames = typeNames;
        }

        public InvalidateQueryCacheMsg(boolean invalidateAll) {
            this.invalidateAll = invalidateAll;
        }

        public InvalidateQueryCacheMsg(QueryKey queryKey) {
            this.queryKey = queryKey;
        }
    }
}
