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

package io.jmix.ui.components.valueproviders;

import com.vaadin.data.ValueProvider;
import io.jmix.core.MetadataTools;
import io.jmix.core.Entity;

public class EntityNameValueProvider<E extends Entity> implements ValueProvider<E, String> {

    protected MetadataTools metadataTools;

    public EntityNameValueProvider(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public String apply(E entity) {
        return entity != null
                ? metadataTools.getInstanceName(entity)
                : "";
    }
}
