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

package io.jmix.data;

import io.jmix.core.View;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;

import java.util.Collection;

/**
 * Enforces entity attribute permissions on the data access layer.
 */
public interface PersistenceAttributeSecurity {

    String NAME = "jmix_PersistenceAttributeSecurity";

    View createRestrictedView(View view);

    void afterLoad(Entity entity);

    void afterLoad(Collection<? extends Entity> entities);

    void beforePersist(Entity entity);

    void afterPersist(Entity entity, View view);

    void beforeMerge(Entity entity);

    void afterMerge(Entity entity);

    void afterCommit(Entity entity);

    void onLoad(Collection<? extends Entity> entities, View view);

    void onLoad(Entity entity, View view);

    <T extends Entity> void setupAttributeAccess(T entity);

    boolean isAttributeAccessEnabled(MetaClass metaClass);

    boolean isAttributeAccessEnabled();
}
