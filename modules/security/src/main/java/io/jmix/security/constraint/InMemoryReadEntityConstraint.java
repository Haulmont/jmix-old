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

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.impl.context.InMemoryReadEntityContext;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component(InMemoryReadEntityConstraint.NAME)
public class InMemoryReadEntityConstraint extends AbstractRowLevelReadConstraint<InMemoryReadEntityContext> {
    public static final String NAME = "sec_InMemoryReadEntityConstraint";

    protected Metadata metadata;

    @Autowired
    public InMemoryReadEntityConstraint(CurrentAuthentication currentAuthentication,
                                        Metadata metadata) {
        super(currentAuthentication);
        this.metadata = metadata;
    }

    @Override
    public Class<InMemoryReadEntityContext> getContextType() {
        return InMemoryReadEntityContext.class;
    }

    @Override
    public void applyTo(InMemoryReadEntityContext context) {
        for (Entity entity : context.getEntities()) {
            //TODO: think about actions enum
            if (isPermitted(entity, RowLevelPolicyAction.READ.getId())) {
                context.addPermittedEntity(entity);
            }
        }
    }

    protected boolean isPermitted(Entity entity, String action) {
        MetaClass entityClass = metadata.getClass(entity.getClass());
        boolean permitted = true;
        for (RowLevelPolicy policy : getRowLevelPolicies(entityClass)) {
            if (Objects.equals(policy.getAction(), action) && policy.getPredicate() != null) {
                permitted = permitted && policy.getPredicate().test(entity);
            }
        }
        return permitted;
    }
}
