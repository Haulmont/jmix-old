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

package io.jmix.securityui.constraint;

import io.jmix.core.constraint.EntityOperationConstraint;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.security.constraint.EntityPolicyStore;
import io.jmix.ui.context.UiEntityAttributeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(UiEntityAttributeConstraint.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiEntityAttributeConstraint implements EntityOperationConstraint<UiEntityAttributeContext> {
    public static final String NAME = "sec_UiEntityAttributeConstraint";

    protected EntityPolicyStore policyStore;
    protected SecureOperations entityOperations;

    @Autowired
    public void setPolicyStore(EntityPolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Autowired
    public void setEntityOperations(SecureOperations entityOperations) {
        this.entityOperations = entityOperations;
    }

    @Override
    public Class<UiEntityAttributeContext> getContextType() {
        return UiEntityAttributeContext.class;
    }

    @Override
    public void applyTo(UiEntityAttributeContext context) {
        if (!entityOperations.isEntityAttrUpdatePermitted(context.getPropertyPath(), policyStore)) {
            context.setModifyDenied();
        }
        if (!entityOperations.isEntityAttrReadPermitted(context.getPropertyPath(), policyStore)) {
            context.setViewDenied();
        }
    }
}
