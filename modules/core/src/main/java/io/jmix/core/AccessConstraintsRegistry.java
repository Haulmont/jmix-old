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

package io.jmix.core;

import io.jmix.core.constraint.AccessConstraint;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component(AccessConstraintsRegistry.NAME)
public class AccessConstraintsRegistry {

    public static final String NAME = "core_AccessConstraintsRegistry";

    protected List<AccessConstraint<?>> accessConstraints = new CopyOnWriteArrayList<>();

    public void register(AccessConstraint accessConstraint) {
        accessConstraints.add(accessConstraint);
    }

    public List<AccessConstraint<?>> getConstraints() {
        return accessConstraints;
    }

    public Collection<? extends AccessConstraint<?>> getConstraintsOfType(Class<?> accessConstraintClass) {
        return accessConstraints.stream()
                .filter(constraint -> accessConstraintClass.isAssignableFrom(constraint.getClass()))
                .collect(Collectors.toList());
    }
}
