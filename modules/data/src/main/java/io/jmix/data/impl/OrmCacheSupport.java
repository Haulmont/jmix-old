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

import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.EntityAccessor;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.Persistence;
import org.eclipse.persistence.jpa.JpaCache;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

@Component(OrmCacheSupport.NAME)
public class OrmCacheSupport {

    public static final String NAME = "cuba_OrmCacheSupport";

    @Inject
    protected Metadata metadata;

    // todo data stores
    @Inject
    protected EntityManagerFactory entityManagerFactory;

    @Inject
    protected EntityStates entityStates;

    @Inject
    protected MetadataTools metadataTools;

    /**
     * Evicts an entity from cache if it has the given entity as an element of collection.
     *
     * @param entity  which is being updated and can potentially be an element of a collection
     * @param changes changes in the entity. Null when creating and removing the entity.
     */
    public void evictMasterEntity(Entity<?> entity, @Nullable EntityAttributeChanges changes) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (MetaProperty property : metaClass.getProperties()) {
            if (!property.getRange().isClass() || property.getRange().getCardinality().isMany())
                continue;
            MetaProperty inverseProp = property.getInverse();
            if (inverseProp == null || !inverseProp.getRange().getCardinality().isMany())
                continue;
            // the inverse property is a collection
            if (metadataTools.isCacheable(property.getRange().asClass())) {
                if (changes != null) {
                    for (String attributeName : changes.getOwnAttributes()) {
                        if (property.getName().equals(attributeName)) {
                            evictEntity(changes.getOldValue(attributeName));
                            break;
                        }
                    }
                } else {
                    Object masterEntity = EntityAccessor.getEntityValue(entity, property.getName());
                    evictEntity(masterEntity);
                }
            }
        }
    }

    private void evictEntity(Object entity) {
        if (entity != null && !entityStates.isNew(entity)) {
            JpaCache cache = (JpaCache) entityManagerFactory.getCache();
            cache.evict(entity, true);
        }
    }
}
