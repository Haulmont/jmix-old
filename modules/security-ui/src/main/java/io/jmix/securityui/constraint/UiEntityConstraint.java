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
import io.jmix.ui.context.UiEntityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(UiEntityAttributeConstraint.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiEntityConstraint implements EntityOperationConstraint<UiEntityContext> {
    public static final String NAME = "sec_UiEntityConstraint";

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
    public Class<UiEntityContext> getContextType() {
        return UiEntityContext.class;
    }

    @Override
    public void applyTo(UiEntityContext context) {
        if (!entityOperations.isEntityCreatePermitted(context.getEntityClass(), policyStore)) {
            context.setCreateDenied();
        }
        if (!entityOperations.isEntityReadPermitted(context.getEntityClass(), policyStore)) {
            context.setViewDenied();
        }
        if (!entityOperations.isEntityUpdatePermitted(context.getEntityClass(), policyStore)) {
            context.setEditDenied();
        }
        if (!entityOperations.isEntityDeletePermitted(context.getEntityClass(), policyStore)) {
            context.setDeleteDenied();
        }
    }
}
