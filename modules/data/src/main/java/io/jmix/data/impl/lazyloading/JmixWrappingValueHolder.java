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

package io.jmix.data.impl.lazyloading;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.internal.expressions.ExpressionIterator;
import org.eclipse.persistence.internal.expressions.ParameterExpression;
import org.eclipse.persistence.internal.indirection.QueryBasedValueHolder;
import org.eclipse.persistence.internal.indirection.UnitOfWorkQueryValueHolder;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class JmixWrappingValueHolder extends JmixAbstractValueHolder {
    protected volatile UnitOfWorkQueryValueHolder originalValueHolder;

    public JmixWrappingValueHolder(UnitOfWorkQueryValueHolder originalValueHolder) {
        this.originalValueHolder = originalValueHolder;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                if (originalValueHolder.isInstantiated()) {
                    this.value = originalValueHolder.getValue();
                } else {
                    DataManager dataManager = AppBeans.get(DataManager.NAME);
                    Metadata metadata = AppBeans.get(Metadata.NAME);
                    MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);
                    Class refClass = ((ForeignReferenceMapping) originalValueHolder.getMapping()).getReferenceClass();
                    MetaClass metaClass = metadata.getClass(refClass);
                    MetaProperty idProperty = metadataTools.getPrimaryKeyProperty(metaClass);
                    LoadContext lc = new LoadContext(metaClass);
                    AtomicReference<String> fieldName = new AtomicReference<>();
                    ExpressionIterator iterator = new ExpressionIterator() {
                        @Override
                        public void iterate(Expression each) {
                            if (each instanceof ParameterExpression) {
                                fieldName.set(((ParameterExpression) each).getField().getQualifiedName());
                            }
                        }
                    };
                    QueryBasedValueHolder wrappedValueHolder = (QueryBasedValueHolder) originalValueHolder.getWrappedValueHolder();
                    iterator.iterateOn(wrappedValueHolder.getQuery().getSelectionCriteria());
                    Object id = originalValueHolder.getRow().get(fieldName.get());
                    // Since UUID is stored as String
                    if (idProperty.getJavaType() == UUID.class) {
                        id = UUID.fromString((String) id);
                    }
                    lc.setId(id);
                    value = dataManager.load(lc);
                }
                isInstantiated = true;
            }
        }
        return value;
    }

    @Override
    public Object clone() {
        return new JmixWrappingValueHolder(originalValueHolder);
    }
}
