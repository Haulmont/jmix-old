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
package com.haulmont.cuba.core.listener;

import com.haulmont.cuba.core.global.impl.EntityListenerUtils;
import com.haulmont.cuba.core.EntityManager;
import io.jmix.core.Entity;

/**
 * Defines the contract for handling of entities before they have been deleted or
 * marked as deleted in DB.
 *
 */
public interface BeforeDeleteEntityListener<T extends Entity> extends io.jmix.data.listener.BeforeDeleteEntityListener<T> {

    /**
     * Executes before the object has been deleted or marked as deleted in DB.
     *
     * @param entity        deleted entity instance
     * @param entityManager EntityManager that owns the entity instance
     */
    void onBeforeDelete(T entity, EntityManager entityManager);

    @Override
    default void onBeforeDelete(T entity) {
        onBeforeDelete(entity, EntityListenerUtils.getCurrentEntityManager(entity));
    }
}
