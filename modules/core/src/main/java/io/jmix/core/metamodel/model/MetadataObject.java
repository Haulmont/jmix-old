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

package io.jmix.core.metamodel.model;

import java.util.Map;

/**
 * Ancestor of main metadata objects: {@link io.jmix.core.metamodel.model.MetaClass} and {@link io.jmix.core.metamodel.model.MetaProperty}
 *
 */
public interface MetadataObject {

    /**
     * MetadataObject unique name.
     */
    String getName();

    /**
     * MetadataObject annotations. Annotations here are simply name-value pairs, not directly correlated with Java annotations.
     */
    Map<String, Object> getAnnotations();
}