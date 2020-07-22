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

import io.jmix.core.constraint.RowLevelConstraint;
import io.jmix.core.context.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.authentication.SecuredAuthentication;
import io.jmix.security.model.RowLevelPolicy;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractRowLevelConstraint<T extends AccessContext> implements RowLevelConstraint<T> {

    protected CurrentAuthentication currentAuthentication;

    public AbstractRowLevelConstraint(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    protected Collection<RowLevelPolicy> getRowLevelPolicies(MetaClass metaClass) {
        if (currentAuthentication.getAuthentication() instanceof SecuredAuthentication) {
            SecuredAuthentication authentication = (SecuredAuthentication) currentAuthentication.getAuthentication();
            return authentication.getRowLevelPoliciesByEntity(metaClass);
        }
        return Collections.emptyList();
    }
}