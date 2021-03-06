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

package io.jmix.core.context;

import io.jmix.core.metamodel.model.MetaClass;

import java.util.HashSet;
import java.util.Set;

public class ImportEntityContext implements AccessContext {
    protected final MetaClass entityClass;
    protected Set<String> deniedAttributes;

    public ImportEntityContext(MetaClass entityClass) {
        this.entityClass = entityClass;
    }

    public MetaClass getEntityClass() {
        return entityClass;
    }

    public boolean isImportPermitted(String attribute) {
        return deniedAttributes == null || !deniedAttributes.contains(attribute);
    }

    public void addDeniedAttribute(String name) {
        if (deniedAttributes == null) {
            deniedAttributes = new HashSet<>();
        }
        deniedAttributes.add(name);
    }
}
