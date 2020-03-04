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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.TextInputField;
import io.jmix.core.entity.annotation.CaseConversion;
import io.jmix.core.entity.annotation.ConversionType;
import io.jmix.ui.components.HasConversionErrorMessage;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.Map;

public class AbstractTextFieldLoader<T extends TextInputField> extends AbstractFieldLoader<T> {

    @Override
    public void createComponent() {
        super.loadComponent();

        loadBuffered(resultComponent, element);
        loadTabIndex(resultComponent, element);
    }

    protected void loadTrimming(io.jmix.ui.components.TextInputField.TrimSupported component, Element element) {
        String trim = element.attributeValue("trim");
        if (StringUtils.isNotEmpty(trim)) {
            component.setTrimming(Boolean.parseBoolean(trim));
        }
    }

    protected void loadMaxLength(io.jmix.ui.components.TextInputField.MaxLengthLimited component, Element element) {
        String maxLength = element.attributeValue("maxLength");
        if (StringUtils.isNotEmpty(maxLength)) {
            component.setMaxLength(Integer.parseInt(maxLength));
        }
    }

    protected void loadCaseConversion(io.jmix.ui.components.TextInputField.CaseConversionSupported component, Element element) {
        String caseConversion = element.attributeValue("caseConversion");
        if (StringUtils.isNotEmpty(caseConversion)) {
            component.setCaseConversion(io.jmix.ui.components.TextInputField.CaseConversion.valueOf(caseConversion));
            return;
        }

        if (resultComponent.getMetaPropertyPath() != null) {
            Map<String, Object> annotations = resultComponent.getMetaPropertyPath().getMetaProperty().getAnnotations();

            //noinspection unchecked
            Map<String, Object> conversion = (Map<String, Object>) annotations.get(CaseConversion.class.getName());
            if (MapUtils.isNotEmpty(conversion)) {
                ConversionType conversionType = (ConversionType) conversion.get("type");
                io.jmix.ui.components.TextInputField.CaseConversion tfCaseConversion = io.jmix.ui.components.TextInputField.CaseConversion.valueOf(conversionType.name());
                component.setCaseConversion(tfCaseConversion);
            }
        }
    }

    protected void loadTextChangeEventProperties(io.jmix.ui.components.TextInputField.TextChangeNotifier component, Element element) {
        String textChangeEventMode = element.attributeValue("textChangeEventMode");
        if (StringUtils.isNotEmpty(textChangeEventMode)) {
            component.setTextChangeEventMode(io.jmix.ui.components.TextInputField.TextChangeEventMode.valueOf(textChangeEventMode));
        }

        String textChangeTimeout = element.attributeValue("textChangeTimeout");
        if (StringUtils.isNotEmpty(textChangeTimeout)) {
            component.setTextChangeTimeout(Integer.parseInt(textChangeTimeout));
        }
    }

    protected void loadConversionErrorMessage(HasConversionErrorMessage component, Element element) {
        String conversionErrorMessage = element.attributeValue("conversionErrorMessage");
        if (StringUtils.isNotEmpty(conversionErrorMessage)) {
            component.setConversionErrorMessage(loadResourceString(conversionErrorMessage));
        }
    }
}
