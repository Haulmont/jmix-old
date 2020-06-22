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

package io.jmix.core.impl;

import io.jmix.core.Entity;
import io.jmix.core.EntityInitializer;
import io.jmix.core.Metadata;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.JmixGeneratedId;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component(GeneratedIdEntityInitializer.NAME)
public class GeneratedIdEntityInitializer implements EntityInitializer, Ordered {
    public static final String NAME = "core_GeneratedIdEntityInitializer";

    @Autowired
    private Metadata metadata;

    @Override
    public void initEntity(Entity entity) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        metaClass.getProperties().stream()
                .filter(property -> property.getRange().isDatatype()
                        && property.getRange().asDatatype().getJavaClass().equals(UUID.class)
                        && property.getAnnotations().get(JmixGeneratedId.class.getName()) != null)
                .findFirst()
                .ifPresent(property -> {
                    if (EntityValues.getValue(entity, property.getName()) == null) {
                        EntityValues.setValue(entity, property.getName(), UuidProvider.createUuid());
                    }
                });
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE;
    }
}
