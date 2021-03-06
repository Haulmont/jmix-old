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

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityEntrySoftDelete;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.converters.AuditConversionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.persistence.internal.helper.CubaUtil;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import java.lang.reflect.Field;
import java.util.*;

public class JmixEntityManager implements EntityManager {

    private EntityManager delegate;

    private BeanFactory beanFactory;

    private PersistenceSupport support;
    private ExtendedEntities extendedEntities;
    private Metadata metadata;
    private MetadataTools metadataTools;
    private EntityStates entityStates;
    private EntityListenerManager entityListenerMgr;
    private EntityPersistingEventManager entityPersistingEventMgr;
    private TimeSource timeSource;
    private AuditInfoProvider auditInfoProvider;
    private AuditConversionService auditConverter;

    private static final Logger log = LoggerFactory.getLogger(JmixEntityManager.class);

    public JmixEntityManager(EntityManager delegate, BeanFactory beanFactory) {
        this.delegate = delegate;
        this.beanFactory = beanFactory;

        support = (PersistenceSupport) beanFactory.getBean(PersistenceSupport.NAME);
        extendedEntities = (ExtendedEntities) beanFactory.getBean(ExtendedEntities.NAME);
        metadataTools = (MetadataTools) beanFactory.getBean(MetadataTools.NAME);
        metadata = (Metadata) beanFactory.getBean(Metadata.NAME);
        entityStates = (EntityStates) beanFactory.getBean(EntityStates.NAME);
        entityListenerMgr = (EntityListenerManager) beanFactory.getBean(EntityListenerManager.NAME);
        entityPersistingEventMgr = (EntityPersistingEventManager) beanFactory.getBean(EntityPersistingEventManager.NAME);
        timeSource = (TimeSource) beanFactory.getBean(TimeSource.NAME);
        auditInfoProvider = (AuditInfoProvider) beanFactory.getBean(AuditInfoProvider.NAME);
        auditConverter = (AuditConversionService) beanFactory.getBean(AuditConversionService.NAME);
    }

    @Override
    public void persist(Object entity) {
        if (entity instanceof JmixEntity) {
            entityPersistingEventMgr.publishEvent((JmixEntity) entity);
        }
        internalPersist(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T merge(T object) {
        log.debug("merge {}", object);

        if (!(object instanceof JmixEntity)) {
            return delegate.merge(object);
        }

        JmixEntity entity = (JmixEntity) object;

        if (entityStates.isManaged(entity))
            return object;

        String storeName = support.getStorageName(delegate.unwrap(UnitOfWork.class));
        entityListenerMgr.fireListener(entity, EntityListenerType.BEFORE_ATTACH, storeName);

        if ((entityStates.isNew(entity) || !entityStates.isDetached(entity)) && EntityValues.getId(entity) != null) {
            // if a new instance is passed to merge(), we suppose it is persistent but "not detached"
            JmixEntity destEntity = findOrCreate(entity.getClass(), EntityValues.getId(entity));
            deepCopyIgnoringNulls(entity, destEntity, Sets.newIdentityHashSet());
            if (entityStates.isNew(destEntity)) {
                entityPersistingEventMgr.publishEvent(entity);
            }
            return (T) destEntity;
        }

        JmixEntity merged = internalMerge(entity);
        support.registerInstance(merged, this);
        return (T) merged;
    }

    @Override
    public void remove(Object object) {
        log.debug("remove {}", object);

        if (!(object instanceof JmixEntity)) {
            delegate.remove(object);
            return;
        }
        JmixEntity entity = (JmixEntity) object;

        if (entityStates.isDetached(entity)) {
            entity = internalMerge(entity);
        }

        if (entity.__getEntityEntry() instanceof EntityEntrySoftDelete && PersistenceHints.isSoftDeletion(delegate)) {
            EntityEntrySoftDelete softDeleteEntry = (EntityEntrySoftDelete) entity.__getEntityEntry();

            softDeleteEntry.setDeletedDate(auditConverter.convert(
                    timeSource.currentTimestamp(),
                    softDeleteEntry.getDeletedDateClass()));

            if (softDeleteEntry.getDeletedByClass() != null) {
                softDeleteEntry.setDeletedBy(auditConverter.convert(
                        auditInfoProvider.getCurrentUser(),
                        softDeleteEntry.getDeletedByClass()));
            }
        } else {
            delegate.remove(entity);
            entity.__getEntityEntry().setRemoved(true);
        }
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return internalFind(entityClass, primaryKey, LockModeType.NONE, Collections.emptyMap());
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return internalFind(entityClass, primaryKey, LockModeType.NONE, properties);
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return internalFind(entityClass, primaryKey, lockMode, Collections.emptyMap());
    }

    @Override
    @Nullable
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return internalFind(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        //noinspection unchecked
        Class<T> effectiveClass = extendedEntities.getEffectiveClass(entityClass);

        T reference = delegate.getReference(effectiveClass, primaryKey);
        ((JmixEntity) reference).__getEntityEntry().setNew(false);
        return reference;
    }

    @Override
    public void flush() {
        support.processFlush(this, false);
        delegate.flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        delegate.lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        delegate.lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity) {
        delegate.refresh(entity);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        delegate.refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        delegate.refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        delegate.refresh(entity, lockMode, properties);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void detach(Object entity) {
        delegate.detach(entity);
        if (entity instanceof JmixEntity) {
            support.detach(this, (JmixEntity) entity);
        }
    }

    @Override
    public boolean contains(Object entity) {
        return delegate.contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return delegate.getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        if (PersistenceHints.SOFT_DELETION.equals(propertyName)) {
            Preconditions.checkNotNullArgument(value, "soft deletion value must not be null");
            setSoftDeletion((Boolean) value);
        } else {
            delegate.setProperty(propertyName, value);
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Query createQuery(String qlString) {
        return new JmixQuery(delegate, beanFactory, false, qlString, null);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return delegate.createQuery(criteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return delegate.createQuery(updateQuery);
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return delegate.createQuery(deleteQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return new JmixQuery<T>(delegate, beanFactory, false, qlString, resultClass);
    }

    @Override
    public Query createNamedQuery(String name) {
        return delegate.createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return delegate.createNamedQuery(name, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return new JmixQuery(delegate, beanFactory, true, sqlString, null);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return new JmixQuery(delegate, beanFactory, true, sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        throw new UnsupportedOperationException("SqlResultSetMapping is not supported");
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return delegate.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return delegate.createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return delegate.createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return delegate.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        delegate.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return delegate.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return delegate.unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return delegate.getDelegate();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return delegate.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return delegate.createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return delegate.createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return delegate.getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return delegate.getEntityGraphs(entityClass);
    }

    private void internalPersist(Object entity) {
        delegate.persist(entity);
        if (entity instanceof JmixEntity) {
            support.registerInstance((JmixEntity) entity, this);
        }
    }

    private void setSoftDeletion(boolean softDeletion) {
        delegate.setProperty(PersistenceHints.SOFT_DELETION, softDeletion);
        CubaUtil.setSoftDeletion(softDeletion);
        CubaUtil.setOriginalSoftDeletion(softDeletion);
    }

    private boolean isSoftDeletion(Map<String, Object> properties) {
        Boolean softDeletionInProps = properties == null ? null : (Boolean) properties.get(PersistenceHints.SOFT_DELETION);
        return (softDeletionInProps == null || softDeletionInProps) && PersistenceHints.isSoftDeletion(delegate);
    }

    @Nullable
    private <T> T internalFind(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        Preconditions.checkNotNullArgument(primaryKey, "primaryKey is null");

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(entityClass);

        Collection<FetchPlan> fetchPlans = PersistenceHints.getFetchPlans(properties);
        if (!fetchPlans.isEmpty()) {
            return findPartial(metaClass, primaryKey, fetchPlans);
        }

        Object realId = primaryKey;
        log.debug("find {} by id={}", entityClass.getSimpleName(), realId);
        Class<T> javaClass = metaClass.getJavaClass();

        T entity = delegate.find(javaClass, realId, lockMode, properties);

        if (entity != null && EntityValues.isSoftDeleted((JmixEntity) entity)
                && isSoftDeletion(properties))
            return null; // in case of entity cache
        else
            return entity;
    }

    private <T> T findPartial(MetaClass metaClass, Object id, Collection<FetchPlan> fetchPlans) {
        Object realId = id;
        log.debug("find {} by id={}, fetchPlans={}", metaClass.getJavaClass().getSimpleName(), realId, fetchPlans);

        String pkName = metadataTools.getPrimaryKeyName(metaClass);
        if (pkName == null)
            throw new IllegalStateException("Cannot determine PK name for entity " + metaClass);

        JmixQuery query = (JmixQuery) createQuery(String.format("select e from %s e where e.%s = ?1", metaClass.getName(), pkName));
        query.setSingleResultExpected(true);
        query.setParameter(1, realId);
        query.setHint(PersistenceHints.FETCH_PLAN, fetchPlans);

        //noinspection unchecked
        return (T) query.getSingleResultOrNull();
    }

    private <T extends JmixEntity> T internalMerge(T entity) {
        try {
            CubaUtil.setSoftDeletion(false);
            CubaUtil.setOriginalSoftDeletion(false);

            T merged = delegate.merge(entity);

            // copy non-persistent attributes to the resulting merged instance
            for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
                if (!metadataTools.isPersistent(property) && !property.isReadOnly()) {
                    // copy using reflection to avoid executing getter/setter code
                    Field field = FieldUtils.getField(entity.getClass(), property.getName(), true);
                    if (field != null) {
                        try {
                            Object value = FieldUtils.readField(field, entity);
                            if (value != null) {
                                FieldUtils.writeField(field, merged, value);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Error copying non-persistent attribute value to merged instance", e);
                        }
                    }
                }
            }

            return merged;
        } finally {
            boolean softDeletion = PersistenceHints.isSoftDeletion(delegate);
            CubaUtil.setSoftDeletion(softDeletion);
            CubaUtil.setOriginalSoftDeletion(softDeletion);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends JmixEntity> T findOrCreate(Class<T> entityClass, Object id) {
        JmixEntity reloadedRef = find(entityClass, id);
        if (reloadedRef == null) {
            reloadedRef = metadata.create(entityClass);
            EntityValues.setId(reloadedRef, id);
            internalPersist(reloadedRef);
        }
        return (T) reloadedRef;
    }

    /**
     * Copies all property values from source to dest excluding null values.
     */
    protected void deepCopyIgnoringNulls(JmixEntity source, JmixEntity dest, Set<JmixEntity> visited) {
        if (visited.contains(source))
            return;
        visited.add(source);

        for (MetaProperty srcProperty : metadata.getClass(source).getProperties()) {
            String name = srcProperty.getName();

            if (!entityStates.isLoaded(source, name)) {
                continue;
            }

            if (srcProperty.isReadOnly()) {
                continue;
            }

            Object value = EntityValues.getValue(source, name);
            if (value == null) {
                continue;
            }

            if (srcProperty.getRange().isClass() && !metadataTools.isEmbedded(srcProperty)) {
                if (!metadataTools.isOwningSide(srcProperty))
                    continue;

                Class refClass = srcProperty.getRange().asClass().getJavaClass();
                if (!metadataTools.isPersistent(refClass))
                    continue;

                if (srcProperty.getRange().getCardinality().isMany()) {
                    if (!metadataTools.isOwningSide(srcProperty)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Collection<JmixEntity> srcCollection = (Collection) value;
                    Collection<JmixEntity> dstCollection = EntityValues.getValue(dest, name);
                    if (dstCollection == null)
                        throw new RuntimeException("Collection is null: " + srcProperty);
                    boolean equal = srcCollection.size() == dstCollection.size();
                    if (equal) {
                        if (srcProperty.getRange().isOrdered()) {
                            equal = Arrays.equals(srcCollection.toArray(), dstCollection.toArray());
                        } else {
                            equal = CollectionUtils.isEqualCollection(srcCollection, dstCollection);
                        }
                    }
                    if (!equal) {
                        dstCollection.clear();
                        for (JmixEntity srcRef : srcCollection) {
                            JmixEntity reloadedRef = findOrCreate(srcRef.getClass(), EntityValues.getId(srcRef));
                            dstCollection.add(reloadedRef);
                            deepCopyIgnoringNulls(srcRef, reloadedRef, visited);
                        }
                    }
                } else {
                    JmixEntity srcRef = (JmixEntity) value;
                    JmixEntity destRef = EntityValues.getValue(dest, name);
                    if (srcRef.equals(destRef)) {
                        deepCopyIgnoringNulls(srcRef, destRef, visited);
                    } else {
                        JmixEntity reloadedRef = findOrCreate(srcRef.getClass(), EntityValues.getId(srcRef));
                        EntityValues.setValue(dest, name, reloadedRef);
                        deepCopyIgnoringNulls(srcRef, reloadedRef, visited);
                    }
                }
            } else if (metadataTools.isEmbedded(srcProperty)) {
                JmixEntity srcRef = (JmixEntity) value;
                JmixEntity destRef = EntityValues.getValue(dest, name);
                if (destRef != null) {
                    deepCopyIgnoringNulls(srcRef, destRef, visited);
                } else {
                    JmixEntity newRef = metadata.create(srcProperty.getRange().asClass().getJavaClass());
                    EntityValues.setValue(dest, name, newRef);
                    deepCopyIgnoringNulls(srcRef, newRef, visited);
                }
            } else {
                EntityValues.setValue(dest, name, value);
            }
        }
    }
}
