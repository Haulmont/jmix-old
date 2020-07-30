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

import com.haulmont.cuba.gui.components.CaptionMode;
import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.HasCaptionMode;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.component.TokenList;
import io.jmix.ui.xml.layout.loader.TokenListLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

@SuppressWarnings({"unchecked", "rawtypes"})
public class CubaTokenListLoader extends TokenListLoader {

    @SuppressWarnings("rawtypes")
    @Override
    public void loadComponent() {
        super.loadComponent();

        ComponentLoaderHelper.loadValidators((Field) resultComponent, element, context, getClassManager(), getMessages());
    }

    @Override
    protected void loadData(io.jmix.ui.component.TokenList component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    @Override
    protected void loadOptionsContainer(io.jmix.ui.component.TokenList component, Element lookupElement) {
        super.loadOptionsContainer(component, lookupElement);

        if (component.getOptions() == null) {
            DatasourceLoaderHelper
                    .loadOptionsDatasource(lookupElement, (ComponentLoaderContext) getComponentContext())
                    .ifPresent(component::setOptions);
        }
    }

    @Override
    protected void loadCaptionProperty(TokenList component, Element element) {
        ComponentLoaderHelper.loadCaptionProperty((HasCaptionMode) component, element);
    }

    @Override
    protected void loadLookupCaptionProperty(TokenList component, Element lookupElement) {
        String optionsCaptionProperty = lookupElement.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(optionsCaptionProperty)) {
            ((com.haulmont.cuba.gui.components.TokenList) component).setOptionsCaptionMode(CaptionMode.PROPERTY);
            ((com.haulmont.cuba.gui.components.TokenList) component).setOptionsCaptionProperty(optionsCaptionProperty);
        }
    }
}
