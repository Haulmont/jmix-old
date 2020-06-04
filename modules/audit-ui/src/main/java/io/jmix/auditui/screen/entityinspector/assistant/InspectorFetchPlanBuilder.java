/*
 * Copyright 2020 Haulmont.
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

package io.jmix.auditui.screen.entityinspector.assistant;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component(InspectorFetchPlanBuilder.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InspectorFetchPlanBuilder {

    public static final String NAME = "jmix_EntityInspectorFetchPlanBuilder";

    @Autowired
    protected Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;

    private FetchPlanBuilder fetchPlanBuilder;

    protected Class<? extends Entity> entityClass;
    private MetaClass metaClass;

    private boolean withCollections = false;
    private boolean withEmbedded = false;
    private boolean withSystemProperties = false;

    public static InspectorFetchPlanBuilder of(Class<? extends Entity> entityClass) {
        return AppBeans.getPrototype(InspectorFetchPlanBuilder.class, entityClass);
    }

    protected InspectorFetchPlanBuilder(Class<? extends Entity> entityClass) {
        this.entityClass = entityClass;
        this.fetchPlanBuilder = FetchPlanBuilder.of(entityClass);
    }

    @PostConstruct
    protected void postConstruct() {
        metaClass = metadata.getClass(entityClass);
    }

    public InspectorFetchPlanBuilder withCollections(boolean withCollections) {
        this.withCollections = withCollections;
        return this;
    }

    public InspectorFetchPlanBuilder withEmbedded(boolean withEmbedded) {
        this.withEmbedded = withEmbedded;
        return this;
    }

    public InspectorFetchPlanBuilder withSystemProperties(boolean withSystemProperties) {
        this.withSystemProperties = withSystemProperties;
        return this;
    }

    public FetchPlan build() {
        if (withSystemProperties) {
            fetchPlanBuilder.addSystem();
        }
        fetchPlanBuilder.addFetchPlan(FetchPlan.LOCAL);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case ASSOCIATION:
                case COMPOSITION:
                    MetaClass metaPropertyClass = metaProperty.getRange().asClass();

                    if (withEmbedded && metadataTools.isEmbedded(metaProperty)) {
                        fetchPlanBuilder.add(metaProperty.getName(), builder -> {
                            createEmbeddedPlan(metaPropertyClass, builder);
                        });
                    } else {
                        boolean many = metaProperty.getRange().getCardinality().isMany();
                        if (withCollections && many) {
                            fetchPlanBuilder.add(metaProperty.getName(),
                                    builder -> builder.addFetchPlan(FetchPlan.LOCAL)
                                            .addSystem());
                        }

                        if (!many) {
                            fetchPlanBuilder.add(metaProperty.getName(),
                                    builder -> builder.addFetchPlan(FetchPlan.MINIMAL));
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }
        return fetchPlanBuilder.build();
    }

    protected void createEmbeddedPlan(MetaClass metaClass, FetchPlanBuilder builder) {
        builder.addFetchPlan(FetchPlan.BASE);
        for (MetaProperty embeddedNestedProperty : metaClass.getProperties()) {
            if (embeddedNestedProperty.getRange().isClass() &&
                    !embeddedNestedProperty.getRange().getCardinality().isMany()) {
                builder.add(embeddedNestedProperty.getName(), FetchPlan.MINIMAL);
            }
        }
    }
}
