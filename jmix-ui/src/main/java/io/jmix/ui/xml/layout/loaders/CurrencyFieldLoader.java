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

package io.jmix.ui.xml.layout.loaders;

import com.haulmont.cuba.core.entity.annotation.CurrencyLabelPosition;
import com.haulmont.cuba.core.entity.annotation.CurrencyValue;
import com.haulmont.cuba.gui.components.CurrencyField;
import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.HasConversionErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.Map;

public class CurrencyFieldLoader extends AbstractFieldLoader<CurrencyField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(CurrencyField.class);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadCurrency(resultComponent, element);
        loadShowCurrencyLabel(resultComponent, element);
        loadCurrencyLabelPosition(resultComponent, element);
        loadDatatype(resultComponent, element);
        loadConversionErrorMessage(resultComponent, element);
        loadBuffered(resultComponent, element);
    }

    protected void loadCurrencyLabelPosition(CurrencyField resultComponent, Element element) {
        String currencyLabelPosition = element.attributeValue("currencyLabelPosition");
        if (StringUtils.isNotEmpty(currencyLabelPosition)) {
            resultComponent.setCurrencyLabelPosition(CurrencyField.CurrencyLabelPosition.valueOf(currencyLabelPosition));
        }
    }

    protected void loadShowCurrencyLabel(CurrencyField resultComponent, Element element) {
        String showCurrency = element.attributeValue("showCurrencyLabel");
        if (StringUtils.isNotEmpty(showCurrency)) {
            resultComponent.setShowCurrencyLabel(Boolean.parseBoolean(showCurrency));
        }
    }

    protected void loadCurrency(CurrencyField resultComponent, Element element) {
        String currency = element.attributeValue("currency");
        if (StringUtils.isNotEmpty(currency)) {
            resultComponent.setCurrency(currency);
        }
    }

    @Override
    protected void loadDatasource(DatasourceComponent component, Element element) {
        super.loadDatasource(component, element);

        if (component.getDatasource() == null)
            return;

        Map<String, Object> annotations = component.getMetaPropertyPath().getMetaProperty().getAnnotations();
        Object currencyValueAnnotation = annotations.get(CurrencyValue.class.getName());
        if (currencyValueAnnotation == null)
            return;

        //noinspection unchecked
        Map<String, Object> annotationProperties = (Map<String, Object>) currencyValueAnnotation;

        String currencyName = (String) annotationProperties.get("currency");
        if (StringUtils.isNotEmpty(currencyName)) {
            ((CurrencyField) component).setCurrency(currencyName);
        }

        String labelPosition = ((CurrencyLabelPosition) annotationProperties.get("labelPosition")).name();
        ((CurrencyField) component).setCurrencyLabelPosition(CurrencyField.CurrencyLabelPosition.valueOf(labelPosition));
    }

    protected void loadConversionErrorMessage(HasConversionErrorMessage component, Element element) {
        String conversionErrorMessage = element.attributeValue("conversionErrorMessage");
        if (StringUtils.isNotEmpty(conversionErrorMessage)) {
            component.setConversionErrorMessage(loadResourceString(conversionErrorMessage));
        }
    }
}
