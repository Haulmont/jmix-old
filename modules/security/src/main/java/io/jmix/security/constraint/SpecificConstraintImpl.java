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

package io.jmix.security.constraint;

import io.jmix.core.constraint.SpecificConstraint;
import io.jmix.core.context.SpecificOperationAccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SpecificConstraintImpl<T extends SpecificOperationAccessContext> implements SpecificConstraint<T> {
    public static final String NAME = "sec_CommonSpecificConstraint";

    protected final Class<T> contextClass;
    protected final String resourceName;

    protected SecureOperations secureOperations;
    protected SpecificPolicyStore policyStore;

    public SpecificConstraintImpl(Class<T> contextClass, String resourceName) {
        this.contextClass = contextClass;
        this.resourceName = resourceName;
    }

    @Autowired
    public void setSecureOperations(SecureOperations secureOperations) {
        this.secureOperations = secureOperations;
    }

    @Autowired
    public void setPolicyStore(SpecificPolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Override
    public Class<T> getContextType() {
        return contextClass;
    }

    @Override
    public void applyTo(T context) {
        if (secureOperations.isSpecificPermitted(resourceName, policyStore)) {
            context.setDenied();
        }
    }
}
