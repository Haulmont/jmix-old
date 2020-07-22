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
import io.jmix.core.constraint.AccessConstraintsRegistry;
import io.jmix.core.context.AccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class AccessManager {

    @Autowired
    private AccessConstraintsRegistry registry;

    public class ConstraintsBuilder {

        List<AccessConstraint<?>> constraints;

        public ConstraintsBuilder withAllRegistered() {
            constraints.addAll(registry.getConstraints());
            return this;
        }

        public ConstraintsBuilder withRegistered(Class<?> accessConstraintClass) {
            constraints.addAll(registry.getConstraintsOfType(accessConstraintClass));
            return this;
        }

        public ConstraintsBuilder withConstraint(Class<? extends AccessConstraint<?>> accessConstraintClass) {
            constraints.add(instantiateConstraint(accessConstraintClass));
            return this;
        }

        public ConstraintsBuilder withConstraintInstance(AccessConstraint<?> constraint) {
            constraints.add(constraint);
            return this;
        }

        public <C extends AccessContext> ConstraintsBuilder withConstraint(Class<C> contextClass, Function<C, Boolean> constraintFunction) {
            constraints.add(instantiateConstraint(contextClass, constraintFunction));
            return this;
        }

        public List<AccessConstraint<?>> build() {
            return constraints;
        }
    }

    private AccessConstraint instantiateConstraint(Class<?> accessConstraintClass) {
        try {
            return (AccessConstraint) accessConstraintClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends AccessContext> AccessConstraint<T> instantiateConstraint(Class<T> contextClass, Function<T, Boolean> constraintFunction) {
        return null;
    }

    public ConstraintsBuilder constraintsBuilder() {
        return new ConstraintsBuilder();
    }

    public <T extends AccessContext> T applyConstraints(T context, Collection<AccessConstraint<?>> constraints) {
        constraints.stream()
                .filter(constraint -> Objects.equals(constraint.getContextType(), context.getClass()))
                .map(constraint -> (AccessConstraint<T>) constraint)
                .forEach(constraint -> constraint.applyTo(context));
        return context;
    }

    public <T extends AccessContext> T applyRegisteredConstraints(T context) {
        return applyConstraints(context, constraintsBuilder().withAllRegistered().build());
    }
}