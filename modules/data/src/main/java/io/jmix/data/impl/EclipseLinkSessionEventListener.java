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

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.entity.EntityEntrySoftDelete;
import io.jmix.core.entity.JmixSettersEnhanced;
import io.jmix.core.entity.annotation.DisableEnhancing;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.persistence.EntityNotEnhancedException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.persistence.annotations.CacheCoordinationType;
import org.eclipse.persistence.config.CacheIsolationType;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.descriptors.InheritancePolicy;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.descriptors.PersistenceObject;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.internal.weaving.PersistenceWeavedFetchGroups;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.platform.database.*;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.persistence.OneToOne;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EclipseLinkSessionEventListener extends SessionEventAdapter {

    private Environment environment = AppBeans.get(Environment.class);

    private Metadata metadata = AppBeans.get(Metadata.NAME);

    private MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);

    private EntityStates entityStates = AppBeans.get(EntityStates.NAME);

    private DescriptorEventListener descriptorEventListener = AppBeans.get(EclipseLinkDescriptorEventListener.NAME);

    private static final Logger log = LoggerFactory.getLogger(EclipseLinkSessionEventListener.class);

    @Override
    public void preLogin(SessionEvent event) {

        Session session = event.getSession();
        setPrintInnerJoinOnClause(session);

        List<String> wrongFetchTypes = new ArrayList<>();
        List<Pair<Class, String>> missingEnhancements = new ArrayList<>();

        Map<Class, ClassDescriptor> descriptorMap = session.getDescriptors();
        boolean hasMultipleTableConstraintDependency = hasMultipleTableConstraintDependency();
        for (Map.Entry<Class, ClassDescriptor> entry : descriptorMap.entrySet()) {
            MetaClass metaClass = metadata.getSession().getClass(entry.getKey());
            ClassDescriptor desc = entry.getValue();

            enhancementCheck(entry.getKey(), missingEnhancements);

            setCacheable(metaClass, desc, session);

            if (hasMultipleTableConstraintDependency) {
                setMultipleTableConstraintDependency(metaClass, desc);
            }

            if (JmixEntity.class.isAssignableFrom(desc.getJavaClass())) {
                // set DescriptorEventManager that doesn't invoke listeners for base classes
                desc.setEventManager(new DescriptorEventManagerWrapper(desc.getDescriptorEventManager()));
                desc.getEventManager().addListener(descriptorEventListener);


                Class<? extends JmixEntity> entityClass = desc.getJavaClass();

                if (metadataTools.isSoftDeleted(entityClass)) {
                    String fieldName = metadataTools.getDeletedDateProperty(entityClass);
                    desc.getQueryManager().setAdditionalCriteria("this." + fieldName + " is null");

                    desc.setDeletePredicate(entity -> {
                        if (((JmixEntity) entity).__getEntityEntry() instanceof EntityEntrySoftDelete) {
                            EntityEntrySoftDelete entityEntry = (EntityEntrySoftDelete) ((JmixEntity) entity).__getEntityEntry();
                            return entityStates.isLoaded(entity, fieldName) && entityEntry.isDeleted();
                        }
                        return false;
                    });
                }

            }

            List<DatabaseMapping> mappings = desc.getMappings();
            for (DatabaseMapping mapping : mappings) {
                //Fetch type check
                fetchTypeCheck(mapping, entry.getKey(), wrongFetchTypes);

                // support UUID
                String attributeName = mapping.getAttributeName();
                MetaProperty metaProperty = metaClass.getProperty(attributeName);
                if (metaProperty.getRange().isDatatype()) {
                    if (metaProperty.getJavaType().equals(UUID.class)) {
                        ((DirectToFieldMapping) mapping).setConverter(UuidConverter.getInstance());
                        setDatabaseFieldParameters(session, mapping.getField());
                    }
                } else if (metaProperty.getRange().isClass() && !metaProperty.getRange().getCardinality().isMany()) {
                    MetaClass refMetaClass = metaProperty.getRange().asClass();
                    MetaProperty refPkProperty = metadataTools.getPrimaryKeyProperty(refMetaClass);
                    if (refPkProperty != null && refPkProperty.getJavaType().equals(UUID.class)) {
                        for (DatabaseField field : ((OneToOneMapping) mapping).getForeignKeyFields()) {
                            setDatabaseFieldParameters(session, field);
                        }
                    }
                }
                // embedded attributes
                if (mapping instanceof AggregateObjectMapping) {
                    EmbeddedParameters embeddedParameters =
                            metaProperty.getAnnotatedElement().getAnnotation(EmbeddedParameters.class);
                    if (embeddedParameters != null && !embeddedParameters.nullAllowed())
                        ((AggregateObjectMapping) mapping).setIsNullAllowed(false);
                }

                if (mapping.isOneToManyMapping()) {
                    OneToManyMapping oneToManyMapping = (OneToManyMapping) mapping;
                    Class referenceClass = oneToManyMapping.getReferenceClass();
                    if (metadataTools.isSoftDeleted(referenceClass)) {
                        oneToManyMapping.setAdditionalJoinCriteria(new ExpressionBuilder().get(metadataTools.getDeletedDateProperty(referenceClass)).isNull());
                    }
                }

                if (mapping.isOneToOneMapping()) {
                    OneToOneMapping oneToOneMapping = (OneToOneMapping) mapping;
                    if (metadataTools.isSoftDeleted(oneToOneMapping.getReferenceClass())) {
                        if (mapping.isManyToOneMapping()) {
                            oneToOneMapping.setSoftDeletionForBatch(false);
                            oneToOneMapping.setSoftDeletionForValueHolder(false);
                        } else {
                            OneToOne oneToOne = metaProperty.getAnnotatedElement().getAnnotation(OneToOne.class);
                            if (oneToOne != null) {
                                if (Strings.isNullOrEmpty(oneToOne.mappedBy())) {
                                    oneToOneMapping.setSoftDeletionForBatch(false);
                                    oneToOneMapping.setSoftDeletionForValueHolder(false);
                                } else {
                                    oneToOneMapping.setAdditionalJoinCriteria(
                                            new ExpressionBuilder().get(metadataTools.getDeletedDateProperty(oneToOneMapping.getReferenceClass())).isNull());
                                }
                            }
                        }
                    }
                }
            }
        }
        logCheckResult(wrongFetchTypes, missingEnhancements);
    }

    protected void enhancementCheck(Class entityClass, List<Pair<Class, String>> missingEnhancements) {
        boolean jmixEnhanced = ArrayUtils.contains(entityClass.getInterfaces(), JmixSettersEnhanced.class)
                || !(JmixEntity.class.isAssignableFrom(entityClass))
                || ArrayUtils.contains(entityClass.getDeclaredAnnotations(), DisableEnhancing.class);
        boolean persistenceObject = ArrayUtils.contains(entityClass.getInterfaces(), PersistenceObject.class);
        boolean persistenceWeaved = ArrayUtils.contains(entityClass.getInterfaces(), PersistenceWeaved.class);
        boolean persistenceWeavedFetchGroups = ArrayUtils.contains(entityClass.getInterfaces(), PersistenceWeavedFetchGroups.class);
        if (!jmixEnhanced || !persistenceObject || !persistenceWeaved || !persistenceWeavedFetchGroups) {
            String message = String.format("Entity class %s is missing some of enhancing interfaces:%s%s%s%s",
                    entityClass.getSimpleName(),
                    jmixEnhanced ? "" : " JmixEnhanced;",
                    persistenceObject ? "" : " PersistenceObject;",
                    persistenceWeaved ? "" : " PersistenceWeaved;",
                    persistenceWeavedFetchGroups ? "" : " PersistenceWeavedFetchGroups;");
            missingEnhancements.add(new Pair<>(entityClass, message));
        }
    }

    protected void fetchTypeCheck(DatabaseMapping mapping, Class entityClass, List<String> wrongFetchTypes) {
        if ((mapping.isOneToOneMapping() || mapping.isOneToManyMapping()
                || mapping.isManyToOneMapping() || mapping.isManyToManyMapping())) {
            if (!mapping.isLazy()) {
                mapping.setIsLazy(true);
                wrongFetchTypes.add(String.format("EAGER fetch type detected for reference field %s of entity %s; Set to LAZY",
                        mapping.getAttributeName(), entityClass.getSimpleName()));
            }
        } else {
            if (mapping.isLazy()) {
                mapping.setIsLazy(false);
                wrongFetchTypes.add(String.format("LAZY fetch type detected for basic field %s of entity %s; Set to EAGER",
                        mapping.getAttributeName(), entityClass.getSimpleName()));
            }
        }
    }

    protected void logCheckResult(List<String> wrongFetchTypes, List<Pair<Class, String>> missingEnhancements) {
        if (!wrongFetchTypes.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("\n=================================================================");
            message.append("\nIncorrectly defined fetch types detected:");
            for (String wft : wrongFetchTypes) {
                message.append("\n");
                message.append(wft);
            }
            message.append("\n=================================================================");
            log.warn(message.toString());
        }
        if (!missingEnhancements.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("\n=================================================================");
            message.append("\nProblems with entity enhancement detected:");
            for (Pair me : missingEnhancements) {
                message.append("\n");
                message.append(me.getSecond());
            }
            message.append("\n=================================================================");
            log.error(message.toString());
            if (!Boolean.parseBoolean(environment.getProperty("jmix.data.disableEntityEnhancementCheck"))) {
                StringBuilder exceptionMessage = new StringBuilder();
                for (Pair me : missingEnhancements) {
                    exceptionMessage.append(me.getFirst());
                    exceptionMessage.append("; ");
                }
                throw new EntityNotEnhancedException(exceptionMessage.toString());
            }
        }
    }

    private void setCacheable(MetaClass metaClass, ClassDescriptor desc, Session session) {
        String property = (String) session.getProperty("eclipselink.cache.shared.default");
        boolean defaultCache = property == null || Boolean.valueOf(property);

        if ((defaultCache && !desc.isIsolated())
                || desc.getCacheIsolation() == CacheIsolationType.SHARED
                || desc.getCacheIsolation() == CacheIsolationType.PROTECTED) {
            metaClass.getAnnotations().put("cacheable", true);
            desc.getCachePolicy().setCacheCoordinationType(CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS);
        }
    }

    private void setMultipleTableConstraintDependency(MetaClass metaClass, ClassDescriptor desc) {
        InheritancePolicy policy = desc.getInheritancePolicyOrNull();
        if (policy != null && policy.isJoinedStrategy() && policy.getParentClass() != null) {
            boolean hasOneToMany = metaClass.getOwnProperties().stream().anyMatch(metaProperty ->
                    metadataTools.isPersistent(metaProperty)
                            && metaProperty.getRange().isClass()
                            && metaProperty.getRange().getCardinality() == Range.Cardinality.ONE_TO_MANY);
            if (hasOneToMany) {
                desc.setHasMultipleTableConstraintDependecy(true);
            }
        }
    }

    private boolean hasMultipleTableConstraintDependency() {
        String value = environment.getProperty("cuba.hasMultipleTableConstraintDependency");
        return value == null || BooleanUtils.toBoolean(value);
    }

    private void setDatabaseFieldParameters(Session session, DatabaseField field) {
        if (session.getPlatform() instanceof PostgreSQLPlatform) {
            field.setSqlType(Types.OTHER);
            field.setType(UUID.class);
            field.setColumnDefinition("UUID");
        } else if (session.getPlatform() instanceof MySQLPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar(32)");
        } else if (session.getPlatform() instanceof HSQLPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar(36)");
        } else if (session.getPlatform() instanceof SQLServerPlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("uniqueidentifier");
        } else if (session.getPlatform() instanceof OraclePlatform) {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
            field.setColumnDefinition("varchar2(32)");
        } else {
            field.setSqlType(Types.VARCHAR);
            field.setType(String.class);
        }
    }

    private void setPrintInnerJoinOnClause(Session session) {
        boolean useInnerJoinOnClause = BooleanUtils.toBoolean(
                environment.getProperty("cuba.useInnerJoinOnClause"));
        session.getPlatform().setPrintInnerJoinInWhereClause(!useInnerJoinOnClause);
    }
}
