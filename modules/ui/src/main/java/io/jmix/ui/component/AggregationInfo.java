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
package io.jmix.ui.component;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.data.aggregation.AggregationStrategy;
import io.jmix.ui.component.formatter.Formatter;

import javax.annotation.Nullable;

public class AggregationInfo {

    public enum Type {
        SUM,
        AVG,
        COUNT,
        MIN,
        MAX,

        /**
         * Enables us to use custom {@link AggregationStrategy} implementation.
         */
        CUSTOM
    }

    private MetaPropertyPath propertyPath;
    private Type type;
    private Formatter<Object> formatter;
    private AggregationStrategy strategy;
    protected boolean editable = false;

    @Nullable
    public MetaPropertyPath getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(MetaPropertyPath propertyPath) {
        this.propertyPath = propertyPath;
    }

    @Nullable
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Nullable
    public Formatter<Object> getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter<Object> formatter) {
        this.formatter = formatter;
    }

    @Nullable
    public AggregationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(@Nullable AggregationStrategy strategy) {
        if (strategy != null) {
            setType(Type.CUSTOM);
        }
        this.strategy = strategy;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
