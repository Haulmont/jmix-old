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

package io.jmix.dynattr.impl;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.impl.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Component(DynAttrMetadata.NAME)
public class DynAttrMetadataImpl implements DynAttrMetadata {

    @Inject
    protected StoreAwareLocator storeAwareLocator;
    @Inject
    protected ExtendedEntities extendedEntities;
    @Inject
    protected Metadata metadata;

    protected volatile Cache cache;

    protected String dynamicAttributesStore = Stores.MAIN;

    private static final Logger log = LoggerFactory.getLogger(DynAttrMetadataImpl.class);

    @Override
    public Collection<AttributeDefinition> getAttributes(MetaClass metaClass) {
        return getCache().getAttributes(metaClass);
    }

    @Override
    public Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code) {
        return getCache().getAttributeByCode(metaClass, code);
    }

    @Override
    public Collection<CategoryDefinition> getCategories(MetaClass metaCLass) {
        return getCache().getCategories(metaCLass);
    }

    @Override
    public void reload() {
        cache = doLoadCache();
    }

    protected Cache getCache() {
        if (cache == null) {
            Cache newCache = doLoadCache();
            if (cache == null) {
                cache = newCache;
            }
        }
        return cache;
    }

    protected Cache doLoadCache() {
        Multimap<String, CategoryDefinition> categoriesCache = HashMultimap.create();
        Map<String, Map<String, AttributeDefinition>> attributesCache = new LinkedHashMap<>();

        for (CategoryDefinition category : loadCategoryDefinitions()) {
            if (category.getEntityType() != null) {
                MetaClass metaClass = metadata.findClass(category.getEntityType());
                if (metaClass != null) {
                    metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                    categoriesCache.put(metaClass.getName(), category);

                    Map<String, AttributeDefinition> attributes = attributesCache.computeIfAbsent(metaClass.getName(),
                            k -> new LinkedHashMap<>());
                    for (AttributeDefinition attribute : category.getAttributeDefinitions()) {
                        attributes.put(attribute.getCode(), attribute);
                    }
                } else {
                    log.warn("Could not resolve meta class name {} for the category {}.",
                            category.getEntityType(), category.getName());
                }
            }
        }

        return new Cache(categoriesCache, attributesCache);
    }

    protected List<CategoryDefinition> loadCategoryDefinitions() {
        return storeAwareLocator.getTransactionTemplate(dynamicAttributesStore)
                .execute(transactionStatus -> {
                    EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

                    FetchPlan fetchPlan = FetchPlanBuilder.of(Category.class)
                            .addFetchPlan(FetchPlan.LOCAL)
                            .add("categoryAttrs", builder -> {
                                builder.addFetchPlan(FetchPlan.LOCAL);
                                builder.add("category", FetchPlan.LOCAL);
                                builder.add("defaultEntity", FetchPlan.LOCAL);
                            })
                            .build();

                    return entityManager.createQuery("select c from sys$Category c", Category.class)
                            .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                            .getResultList().stream()
                            .map(CommonCategoryDefinition::new)
                            .collect(Collectors.toList());
                });
    }


    protected class Cache {
        protected final Multimap<String, CategoryDefinition> categories;
        protected final Map<String, Map<String, AttributeDefinition>> attributes;

        public Cache(Multimap<String, CategoryDefinition> categories, Map<String, Map<String, AttributeDefinition>> attributes) {
            this.categories = categories;
            this.attributes = attributes;
        }

        public Collection<CategoryDefinition> getCategories(MetaClass metaClass) {
            Collection<CategoryDefinition> targetCategories = categories.get(
                    extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            return Collections.unmodifiableCollection(targetCategories);
        }

        public Collection<AttributeDefinition> getAttributes(MetaClass metaClass) {
            Collection<CategoryDefinition> targetCategories = categories.get(
                    extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            return targetCategories.stream()
                    .flatMap(c -> c.getAttributeDefinitions().stream())
                    .filter(a -> !Strings.isNullOrEmpty(a.getCode()))
                    .collect(Collectors.toList());
        }

        public Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code) {
            Map<String, AttributeDefinition> targetAttributes = attributes.get(
                    extendedEntities.getOriginalOrThisMetaClass(metaClass).getName());
            AttributeDefinition attribute = null;
            if (targetAttributes != null) {
                attribute = targetAttributes.get(code);
            }

            return Optional.ofNullable(attribute);
        }
    }
}
