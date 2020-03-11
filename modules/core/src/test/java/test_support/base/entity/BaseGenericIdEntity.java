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
package test_support.base.entity;

import io.jmix.core.entity.Entity;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
@io.jmix.core.metamodel.annotations.MetaClass(name = "base$BaseGenericIdEntity")
public abstract class BaseGenericIdEntity<T> implements Entity<T> {

    private static final long serialVersionUID = -8400641366148656528L;

    public abstract void setId(T id);

    public abstract T getId();
}
