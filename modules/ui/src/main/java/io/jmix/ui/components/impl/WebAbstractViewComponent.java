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

package io.jmix.ui.components.impl;

import io.jmix.core.commons.events.Subscription;
import io.jmix.core.entity.EntityAccessor;
import io.jmix.ui.AppUI;
import io.jmix.ui.components.HasValue;
import io.jmix.ui.components.data.*;
import io.jmix.ui.components.data.meta.ValueBinding;
import io.jmix.ui.components.data.value.ValueBinder;

import java.util.function.Consumer;

public abstract class WebAbstractViewComponent<T extends com.vaadin.ui.Component, P, V>
        extends WebAbstractComponent<T> implements HasValue<V>, HasValueSource<V> {

    protected V internalValue;
    protected ValueBinding<V> valueBinding;

    @Override
    public ValueSource<V> getValueSource() {
        return valueBinding != null ? valueBinding.getSource() : null;
    }

    @Override
    public void setValueSource(ValueSource<V> valueSource) {
        if (this.valueBinding != null) {
            valueBinding.unbind();

            this.valueBinding = null;
        }

        if (valueSource != null) {
            ValueBinder binder = beanLocator.get(ValueBinder.NAME, ValueBinder.class);

            this.valueBinding = binder.bind(this, valueSource);

            valueBindingConnected(valueSource);

            this.valueBinding.activate();

            valueBindingActivated(valueSource);

            setUiTestId(valueSource);
        }
    }

    protected void setUiTestId(ValueSource<V> valueSource) {
        AppUI ui = AppUI.getCurrent();

        if (ui != null && ui.isTestMode()
                && getComponent().getCubaId() == null) {

            String testId = UiTestIds.getInferredTestId(valueSource);
            if (testId != null) {
                getComponent().setCubaId(testId);
            }
        }
    }

    protected void valueBindingConnected(ValueSource<V> valueSource) {
        // hook
    }

    protected void valueBindingActivated(ValueSource<V> valueSource) {
        // hook
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public V getValue() {
        return internalValue;
    }

    @Override
    public void setValue(V value) {
        setValueToPresentation(convertToPresentation(value));

        V oldValue = internalValue;
        this.internalValue = value;

        if (!fieldValueEquals(value, oldValue)) {
            ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, false);
            publish(ValueChangeEvent.class, event);
        }
    }

    protected abstract void setValueToPresentation(P value);

    @SuppressWarnings("unchecked")
    protected P convertToPresentation(V modelValue) throws ConversionException {
        return (P) modelValue;
    }

    protected boolean fieldValueEquals(V value, V oldValue) {
        return EntityAccessor.propertyValueEquals(oldValue, value);
    }
}
