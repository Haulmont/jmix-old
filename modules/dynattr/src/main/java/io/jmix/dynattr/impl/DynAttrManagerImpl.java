/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.dynattr.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.EntityOp;
import io.jmix.core.security.Security;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.dynattr.*;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.dynattr.impl.model.CategoryAttributeValue;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

//TODO: take into account category
@Component(DynAttrManager.NAME)
public class DynAttrManagerImpl implements DynAttrManager {
    public static final int MAX_ENTITIES_FOR_ATTRIBUTE_VALUES_BATCH = 100;

    private static final Logger log = LoggerFactory.getLogger(DynAttrManagerImpl.class);

    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Security security;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    FetchPlans fetchPlans;

    protected String dynamicAttributesStore = Stores.MAIN;

    @Override
    public void storeValues(Collection<JmixEntity> entities) {
        storeAwareLocator.getTransactionTemplate(dynamicAttributesStore)
                .executeWithoutResult(status -> {
                    for (JmixEntity entity : entities) {
                        doStoreValues(entity);
                    }
                });
    }

    public void loadValues(Collection<JmixEntity> entities, @Nullable FetchPlan fetchPlan) {
        Multimap<MetaClass, JmixEntity> entitiesToLoad = collectEntitiesToLoad(entities, fetchPlan);
        if (!entitiesToLoad.isEmpty()) {
            storeAwareLocator.getTransactionTemplate(dynamicAttributesStore)
                    .executeWithoutResult(status -> {
                        for (MetaClass entityClass : entitiesToLoad.keySet()) {
                            doFetchValues(entityClass, entitiesToLoad.get(entityClass));
                        }
                    });
        }
    }

    @SuppressWarnings("unchecked")
    protected void doStoreValues(JmixEntity entity) {
        DynamicAttributesState state = (DynamicAttributesState)
                entity.__getEntityEntry().getExtraState(DynamicAttributesState.class);
        if (state != null && state.getDynamicAttributes() != null) {
            EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

            DynamicAttributes dynamicModel = state.getDynamicAttributes();
            DynamicAttributes.Changes changes = dynamicModel.getChanges();

            if (changes.hasChanges()) {

                MetaClass metaClass = metadata.getClass(entity.getClass());
                List<CategoryAttributeValue> attributeValues = loadValues(metaClass, Collections.singletonList(referenceToEntitySupport.getReferenceId(entity)));

                for (CategoryAttributeValue attributeValue : attributeValues) {
                    String attributeName = attributeValue.getCode();
                    if (changes.isDeleted(attributeName)) {
                        setValueToCategoryAttributeValue(attributeValue, null);
                        entityManager.remove(attributeValue);
                    } else if (changes.isUpdated(attributeName)) {
                        setValueToCategoryAttributeValue(attributeValue, dynamicModel.getValue(attributeName));

                        if (BooleanUtils.isTrue(attributeValue.getCategoryAttribute().getIsCollection())) {
                            doStoreCollectionValue(attributeValue);
                        }
                    }
                }

                for (String attributeName : changes.getCreated()) {
                    if (changes.isCreated(attributeName)) {
                        dynAttrMetadata.getAttributeByCode(metaClass, attributeName)
                                .ifPresent(attribute -> {
                                    CategoryAttributeValue attributeValue = metadata.create(CategoryAttributeValue.class);
                                    setValueToCategoryAttributeValue(attributeValue, dynamicModel.getValue(attributeName));
                                    attributeValue.setObjectEntityId(referenceToEntitySupport.getReferenceId(entity));
                                    attributeValue.setCode(attributeName);
                                    attributeValue.setCategoryAttribute((CategoryAttribute) attribute.getSource());

                                    entityManager.persist(attributeValue);

                                    if (attribute.isCollection()) {
                                        doStoreCollectionValue(attributeValue);
                                    }
                                });
                    }
                }
            }
            //todo: refresh state
            //state.setValues(mergedValues);
        }
    }

    /**
     * Removes nested {@code CategoryAttributeValue} entities for items that were removed from the collection value
     * and creates new child {@code CategoryAttributeValue} instances for just added collection value items.
     *
     * @param collectionAttributeValue
     */
    protected void doStoreCollectionValue(CategoryAttributeValue collectionAttributeValue) {
        EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

        List<Object> collection = collectionAttributeValue.getTransientCollectionValue();
        List<Object> newCollection = new ArrayList<>(collection);

        if (collectionAttributeValue.getChildValues() != null) {
            for (CategoryAttributeValue existingChild : collectionAttributeValue.getChildValues()) {
                if (!existingChild.isDeleted()) {
                    if (!collection.contains(existingChild.getValue())) {
                        entityManager.remove(existingChild);
                    }
                    newCollection.remove(existingChild.getValue());
                }
            }
        }

        for (Object value : newCollection) {
            CategoryAttributeValue childValue = metadata.create(CategoryAttributeValue.class);
            childValue.setParent(collectionAttributeValue);
            setValueToCategoryAttributeValue(childValue, value);
            if (collectionAttributeValue.getObjectEntityId() != null) {
                childValue.setObjectEntityId(collectionAttributeValue.getObjectEntityId());
            }
            childValue.setCode(collectionAttributeValue.getCode());
            childValue.setCategoryAttribute(collectionAttributeValue.getCategoryAttribute());
            entityManager.persist(childValue);
        }
    }

    protected void doFetchValues(MetaClass metaClass, Collection<JmixEntity> entities) {
        if (dynAttrMetadata.getAttributes(metaClass).isEmpty() ||
                metadataTools.hasCompositePrimaryKey(metaClass) && !metadataTools.hasUuid(metaClass)) {
            for (JmixEntity entity : entities) {
                DynamicAttributesState state = new DynamicAttributesState(entity.__getEntityEntry());
                entity.__getEntityEntry().addExtraState(state);
            }
        } else {
            List<Object> ids = entities.stream()
                    .map(e -> referenceToEntitySupport.getReferenceId(e))
                    .collect(Collectors.toList());

            Multimap<Object, CategoryAttributeValue> allAttributeValues = HashMultimap.create();

            List<Object> currentIds = new ArrayList<>();
            for (Object id : ids) {
                currentIds.add(id);
                if (currentIds.size() >= MAX_ENTITIES_FOR_ATTRIBUTE_VALUES_BATCH) {
                    for (CategoryAttributeValue attributeValue : loadValues(metaClass, currentIds)) {
                        allAttributeValues.put(attributeValue.getObjectEntityId(), attributeValue);
                    }
                    currentIds = new ArrayList<>();
                }
            }
            if (!currentIds.isEmpty()) {
                for (CategoryAttributeValue attributeValue : loadValues(metaClass, currentIds)) {
                    allAttributeValues.put(attributeValue.getObjectEntityId(), attributeValue);
                }
            }

            for (JmixEntity entity : entities) {
                Collection<CategoryAttributeValue> values = allAttributeValues.get(referenceToEntitySupport.getReferenceId(entity));
                DynamicAttributesState state = new DynamicAttributesState(entity.__getEntityEntry());
                entity.__getEntityEntry().addExtraState(state);

                Map<String, Object> map = new HashMap<>();
                if (values != null && !values.isEmpty()) {
                    for (CategoryAttributeValue categoryAttributeValue : values) {
                        CategoryAttribute attribute = categoryAttributeValue.getCategoryAttribute();
                        if (attribute != null) {
                            map.put(attribute.getCode(), categoryAttributeValue.getValue());
                        }
                    }
                }
                state.setDynamicAttributes(new DynamicAttributes(map));
            }
        }
    }

    protected List<CategoryAttributeValue> loadValues(MetaClass metaClass, List<Object> entityIds) {

        List<CategoryAttributeValue> mainAttributeValues = findValuesByEntityIds(metaClass, entityIds);

        List<CategoryAttributeValue> entityValues = mainAttributeValues.stream()
                .filter(v -> v.getObjectEntityValueId() != null)
                .collect(Collectors.toList());

        List<CategoryAttributeValue> collectionValues = mainAttributeValues.stream()
                .filter(v -> BooleanUtils.isTrue(v.getCategoryAttribute().getIsCollection()))
                .collect(Collectors.toList());

        if (collectionValues.isEmpty()) {
            fetchEntityValues(entityValues);

            return mainAttributeValues;
        } else {
            List<CategoryAttributeValue> reloadedCollectionValues = fetchCollectionValues(collectionValues);

            List<CategoryAttributeValue> values = new ArrayList<>(mainAttributeValues.size());

            for (CategoryAttributeValue value : reloadedCollectionValues) {
                if (value.getCategoryAttribute().getDataType() == AttributeType.ENTITY && value.getChildValues() != null) {
                    for (CategoryAttributeValue child : value.getChildValues()) {
                        if (!child.isDeleted()) {
                            entityValues.add(child);
                        }
                    }
                }
            }
            fetchEntityValues(entityValues);

            for (CategoryAttributeValue value : reloadedCollectionValues) {
                if (value.getChildValues() != null) {
                    value.setTransientCollectionValue(
                            value.getChildValues().stream()
                                    .filter(v -> !v.isDeleted())
                                    .map(CategoryAttributeValue::getValue)
                                    .collect(Collectors.toList())
                    );
                }
            }

            for (CategoryAttributeValue value : mainAttributeValues) {
                if (!reloadedCollectionValues.contains(value)) {
                    values.add(value);
                }
            }
            values.addAll(reloadedCollectionValues);

            return values;
        }
    }


    protected List<CategoryAttributeValue> findValuesByEntityIds(MetaClass metaClass, List<Object> entityIds) {
        EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

        FetchPlan fetchPlan = fetchPlans.builder(CategoryAttributeValue.class)
                .add("categoryAttribute", builder -> {
                    builder.addFetchPlan(FetchPlan.LOCAL);
                    builder.add("defaultEntity", FetchPlan.LOCAL);
                    builder.add("category");
                })
                .build();

        List<CategoryAttributeValue> result;
        if (metadataTools.hasUuid(metaClass)) {
            result = entityManager.createQuery(
                    String.format("select v from sys_CategoryAttributeValue v where v.entity.%s in :ids and v.parent is null",
                            referenceToEntitySupport.getReferenceIdPropertyName(metaClass)), CategoryAttributeValue.class)
                    .setParameter("ids", entityIds)
                    .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                    .getResultList();
        } else {
            result = entityManager.createQuery(String.format("select v from sys_CategoryAttributeValue v where v.entity.%s in :ids " +
                            "and v.categoryAttribute.categoryEntityType = :entityType and v.parent is null",
                    referenceToEntitySupport.getReferenceIdPropertyName(metaClass)), CategoryAttributeValue.class)
                    .setParameter("ids", entityIds)
                    .setParameter("entityType", metaClass.getName())
                    .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                    .getResultList();
        }
        return result.stream()
                .filter(v -> !v.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Method loads entity values for CategoryAttributeValues of entity type and sets entity values to the corresponding
     * property of the {@code CategoryAttributeValue} entity.
     */
    @SuppressWarnings("unchecked")
    protected void fetchEntityValues(List<CategoryAttributeValue> values) {
        Multimap<MetaClass, Object> entityIds = HashMultimap.create();
        Multimap<MetaClass, CategoryAttributeValue> valuesByType = HashMultimap.create();

        for (CategoryAttributeValue value : values) {
            String className = value.getCategoryAttribute().getEntityClass();
            try {
                Class<?> aClass = ReflectionHelper.loadClass(className);
                MetaClass metaClass = metadata.getClass(aClass);
                if (security.isEntityOpPermitted(metaClass, EntityOp.READ)) {
                    entityIds.put(metaClass, value.getObjectEntityValueId());
                    valuesByType.put(metaClass, value);
                }
            } catch (ClassNotFoundException e) {
                log.error("Class {} not found", className);
            }
        }

        EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

        for (Map.Entry<MetaClass, Collection<Object>> entry : entityIds.asMap().entrySet()) {

            MetaClass metaClass = entry.getKey();
            Collection<Object> ids = entry.getValue();

            if (!ids.isEmpty()) {
                String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
                List<JmixEntity> resultList = entityManager.createQuery(
                        String.format("select e from %s e where e.%s in :ids", metaClass.getName(), pkName))
                        .setParameter("ids", ids)
                        .setHint(PersistenceHints.FETCH_PLAN, fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.INSTANCE_NAME))
                        .getResultList();


                Map<Object, JmixEntity> entityById = new LinkedHashMap<>();
                for (JmixEntity entity : resultList) {
                    entityById.put(EntityValues.getId(entity), entity);
                }

                for (CategoryAttributeValue value : valuesByType.get(metaClass)) {
                    value.setTransientEntityValue(entityById.get(value.getObjectEntityValueId()));
                }
            }
        }
    }

    protected List<CategoryAttributeValue> fetchCollectionValues(List<CategoryAttributeValue> values) {
        EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

        List<UUID> ids = values.stream()
                .map(BaseUuidEntity::getId)
                .collect(Collectors.toList());

        FetchPlan fetchPlan = fetchPlans.builder(CategoryAttributeValue.class)
                .addFetchPlan(FetchPlan.LOCAL)
                .add("childValues", FetchPlan.LOCAL)
                .add("categoryAttribute",
                        builder -> builder.addFetchPlan(FetchPlan.LOCAL).add("category"))
                .build();

        return entityManager.createQuery("select v from sys_CategoryAttributeValue v where v.id in :ids", CategoryAttributeValue.class)
                .setParameter("ids", ids)
                .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                .getResultList();
    }

    protected Multimap<MetaClass, JmixEntity> collectEntitiesToLoad(Collection<JmixEntity> entities, @Nullable FetchPlan fetchPlan) {
        Multimap<MetaClass, JmixEntity> entitiesByType = HashMultimap.create();
        if (fetchPlan != null) {
            Set<Class> dependentClasses = collectEntityClasses(fetchPlan, new HashSet<>()).stream()
                    .filter(aClass -> !dynAttrMetadata.getAttributes(metadata.getClass(aClass)).isEmpty())
                    .collect(Collectors.toSet());
            for (JmixEntity entity : entities) {
                entitiesByType.put(extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(entity.getClass())), entity);
                if (!dependentClasses.isEmpty()) {
                    metadataTools.traverseAttributes(entity, new EntityAttributeVisitor() {
                        @Override
                        public void visit(JmixEntity visitedEntity, MetaProperty property) {
                            if (dependentClasses.contains(property.getRange().asClass().getJavaClass()) &&
                                    entityStates.isLoaded(visitedEntity, property.getName())) {
                                Object value = EntityValues.getValue(visitedEntity, property.getName());
                                if (value != null) {
                                    if (value instanceof Collection) {
                                        //noinspection rawtypes
                                        for (Object item : ((Collection) value)) {
                                            if (item instanceof JmixEntity) {
                                                entitiesByType.put(metadata.getClass(item.getClass()), (JmixEntity) item);
                                            }
                                        }
                                    } else if (value instanceof JmixEntity) {
                                        if (!((JmixEntity) value).__getEntityEntry().isEmbeddable()) {
                                            entitiesByType.put(metadata.getClass(value.getClass()), (JmixEntity) value);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public boolean skip(MetaProperty property) {
                            return !metadataTools.isPersistent(property) || !property.getRange().isClass();
                        }
                    });
                }

            }
        } else if (!entities.isEmpty()) {
            for (JmixEntity entity : entities) {
                MetaClass metaClass = metadata.getClass(entity.getClass());
                if (!dynAttrMetadata.getAttributes(metaClass).isEmpty()) {
                    entitiesByType.put(extendedEntities.getOriginalOrThisMetaClass(metaClass), entity);
                }
            }
        }
        return entitiesByType;
    }

    protected Set<Class<?>> collectEntityClasses(FetchPlan fetchPlan, Set<FetchPlan> visited) {
        if (visited.contains(fetchPlan)) {
            return Collections.emptySet();
        } else {
            visited.add(fetchPlan);
        }

        HashSet<Class<?>> classes = new HashSet<>();
        classes.add(fetchPlan.getEntityClass());
        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            if (property.getFetchPlan() != null) {
                classes.addAll(collectEntityClasses(property.getFetchPlan(), visited));
            }
        }
        return classes;
    }

    protected void setValueToCategoryAttributeValue(CategoryAttributeValue cav, @Nullable Object value) {
        if (value == null) {
            cav.setStringValue(null);
            cav.setIntValue(null);
            cav.setDoubleValue(null);
            cav.setDecimalValue(null);
            cav.setBooleanValue(null);
            cav.setDateValue(null);
            cav.setDateWithoutTimeValue(null);
            cav.getEntityValue().setObjectEntityId(null);
            cav.setTransientEntityValue(null);
            cav.setTransientCollectionValue(null);
        } else if (value instanceof LocalDate) {
            cav.setDateWithoutTimeValue((LocalDate) value);
        } else if (value instanceof Date) {
            cav.setDateValue((Date) value);
        } else if (value instanceof Integer) {
            cav.setIntValue((Integer) value);
        } else if (value instanceof Double) {
            cav.setDoubleValue((Double) value);
        } else if (value instanceof BigDecimal) {
            cav.setDecimalValue((BigDecimal) value);
        } else if (value instanceof Boolean) {
            cav.setBooleanValue((Boolean) value);
        } else if (value instanceof JmixEntity) {
            Object referenceId = referenceToEntitySupport.getReferenceId((JmixEntity) value);
            cav.getEntityValue().setObjectEntityId(referenceId);
            cav.setTransientEntityValue((JmixEntity) value);
        } else if (value instanceof String) {
            cav.setStringValue((String) value);
        } else if (value instanceof List) {
            cav.setTransientCollectionValue((List<Object>) value);
        } else {
            throw new IllegalArgumentException("Unsupported value type " + value.getClass());
        }
    }
}