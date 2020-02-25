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

import io.jmix.core.BeanLocator;
import io.jmix.core.entity.ManagedEntity;
import io.jmix.core.entity.ManagedEntityEntry;
import io.jmix.core.impl.CorePersistentAttributesLoadChecker;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.Persistence;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;

import javax.persistence.EntityManagerFactory;

public class DataPersistentAttributesLoadChecker extends CorePersistentAttributesLoadChecker {

    private BeanLocator beanLocator;

    public DataPersistentAttributesLoadChecker(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Override
    protected PropertyLoadedState isLoadedByFetchGroup(Object entity, String property) {
        if (entity instanceof FetchGroupTracker) {
            FetchGroup fetchGroup = ((FetchGroupTracker) entity)._persistence_getFetchGroup();
            if (fetchGroup != null) {
                boolean inFetchGroup = fetchGroup.containsAttributeInternal(property);
                if (!inFetchGroup) {
                    // definitely not loaded
                    return PropertyLoadedState.NO;
                } else {
                    // requires additional check specific for the tier
                    return PropertyLoadedState.UNKNOWN;
                }
            }
        }
        return PropertyLoadedState.UNKNOWN;
    }

    @Override
    protected boolean isLoadedSpecificCheck(Object entity, String property, MetaClass metaClass, MetaProperty metaProperty) {
        if (metadataTools.isEmbeddable(metaClass)
                || (entity instanceof ManagedEntity && ((ManagedEntity) entity).getEntityEntry().isNew())) {
            // this is a workaround for unexpected EclipseLink behaviour when PersistenceUnitUtil.isLoaded
            // throws exception if embedded entity refers to persistent entity
            return checkIsLoadedWithGetter(entity, property);
        }
        if (!metadataTools.isPersistent(metaClass)) {
            return checkIsLoadedWithGetter(entity, property);
        }

        Persistence persistence = beanLocator.get(Persistence.class);
        EntityManagerFactory jpaEmf = ((PersistenceImpl) persistence).getJpaEmf(metadataTools.getStoreName(metaClass));
        return jpaEmf.getPersistenceUnitUtil().isLoaded(entity, property);
    }
}
