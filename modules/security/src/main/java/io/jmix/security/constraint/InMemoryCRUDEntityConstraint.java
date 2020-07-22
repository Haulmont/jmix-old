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

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.EntityOp;
import io.jmix.data.impl.context.InMemoryCRUDEntityContext;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.model.RowLevelPolicyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(InMemoryCRUDEntityConstraint.NAME)
public class InMemoryCRUDEntityConstraint extends AbstractRowLevelConstraint<InMemoryCRUDEntityContext> {
    public static final String NAME = "sec_InMemoryCRUDEntityConstraint";

    @Autowired
    public InMemoryCRUDEntityConstraint(CurrentAuthentication currentAuthentication) {
        super(currentAuthentication);
    }

    @Override
    public Class<InMemoryCRUDEntityContext> getContextType() {
        return InMemoryCRUDEntityContext.class;
    }

    @Override
    public void applyTo(InMemoryCRUDEntityContext context) {
        for (RowLevelPolicy policy : getRowLevelPolicies(context.getEntityClass())) {
            if (policy.getAction() == RowLevelPolicyAction.READ && policy.getType() == RowLevelPolicyType.PREDICATE) {
                context.addReadPredicate(policy.getPredicate());
            }
        }
    }

    protected RowLevelPolicyAction getRowLevelPolicyAction(EntityOp entityOp) {
        switch (entityOp) {
            case READ:
                return RowLevelPolicyAction.READ;
            case CREATE:
                return RowLevelPolicyAction.CREATE;
            case UPDATE:
                return RowLevelPolicyAction.UPDATE;
            case DELETE:
                return RowLevelPolicyAction.DELETE;
        }
        return null;
    }
}
