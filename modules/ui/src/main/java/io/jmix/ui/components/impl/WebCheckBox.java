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

import io.jmix.ui.components.CheckBox;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Form;
import io.jmix.ui.widgets.CubaCheckBox;

public class WebCheckBox extends WebV8AbstractField<com.vaadin.ui.CheckBox, Boolean, Boolean> implements CheckBox {
    public WebCheckBox() {
        this.component = createComponent();
        this.internalValue = false;

        attachValueChangeListener(component);
    }

    protected CubaCheckBox createComponent() {
        return new CubaCheckBox();
    }

    @Override
    public void setValue(Boolean value) {
        if (value == null) {
            super.setValue(Boolean.FALSE);
        } else {
            super.setValue(value);
        }
    }

    @Override
    public boolean isChecked() {
        return Boolean.TRUE.equals(getValue());
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public void setParent(Component parent) {
        super.setParent(parent);

        if (parent instanceof Form
                // TODO: gg, legacu-ui
//                parent instanceof FieldGroup
        ) {
            ((CubaCheckBox) component).setCaptionManagedByLayout(true);
        }
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }
}
