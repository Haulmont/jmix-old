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

package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import io.jmix.ui.component.CapsLockIndicator;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixPasswordField;
import com.vaadin.shared.ui.ValueChangeMode;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

public class WebPasswordField extends WebV8AbstractField<JmixPasswordField, String, String>
        implements PasswordField, InitializingBean {

    protected CapsLockIndicator capsLockIndicator;

    public WebPasswordField() {
        this.component = createComponent();

        attachValueChangeListener(component);
    }

    @Override
    protected void valueBindingConnected(ValueSource<String> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = beanLocator.get(DataAwareComponentsTools.class);
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;

            dataAwareComponentsTools.setupMaxLength(this, entityValueSource);
        }
    }

    protected JmixPasswordField createComponent() {
        return new JmixPasswordField();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent(component);
    }

    protected void initComponent(JmixPasswordField component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
    }

    @Override
    protected String convertToPresentation(@Nullable String modelValue) throws ConversionException {
        return nullToEmpty(super.convertToPresentation(modelValue));
    }

    @Nullable
    @Override
    protected String convertToModel(@Nullable String componentRawValue) throws ConversionException {
        String value = emptyToNull(componentRawValue);
        return super.convertToModel(value);
    }

    @Override
    public boolean isEmpty() {
        return Strings.isNullOrEmpty(getValue());
    }

    @Override
    public int getMaxLength() {
        return component.getMaxLength();
    }

    @Override
    public void setMaxLength(int value) {
        component.setMaxLength(value);
    }

    @Override
    public boolean isAutocomplete() {
        return component.isAutocomplete();
    }

    @Override
    public void setAutocomplete(Boolean value) {
        component.setAutocomplete(value);
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

    @Override
    public void setCapsLockIndicator(@Nullable CapsLockIndicator capsLockIndicator) {
        this.capsLockIndicator = capsLockIndicator;

        if (capsLockIndicator != null) {
            component.setCapsLockIndicator(capsLockIndicator.unwrap(com.vaadin.ui.Component.class));
        } else {
            component.setCapsLockIndicator(null);
        }
    }

    @Nullable
    @Override
    public CapsLockIndicator getCapsLockIndicator() {
        return capsLockIndicator;
    }

    @Nullable
    @Override
    public String getInputPrompt() {
        return component.getPlaceholder();
    }

    @Override
    public void setInputPrompt(@Nullable String inputPrompt) {
        component.setPlaceholder(inputPrompt);
    }

    @Override
    public void setHtmlName(@Nullable String htmlName) {
        component.setHtmlName(htmlName);
    }

    @Nullable
    @Override
    public String getHtmlName() {
        return component.getHtmlName();
    }
}
