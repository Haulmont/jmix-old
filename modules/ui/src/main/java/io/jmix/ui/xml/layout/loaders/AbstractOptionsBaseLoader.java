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

import io.jmix.ui.components.CaptionMode;
import io.jmix.ui.components.OptionsField;
import io.jmix.ui.components.data.options.ContainerOptions;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class AbstractOptionsBaseLoader<T extends OptionsField> extends AbstractFieldLoader<T> {
    @Override
    public void loadComponent() {
        super.loadComponent();
    }

    protected void loadCaptionProperty(T component, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            component.setCaptionProperty(captionProperty);
            component.setCaptionMode(CaptionMode.PROPERTY);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadOptionsEnum(T resultComponent, Element element) {
        String optionsEnumClass = element.attributeValue("optionsEnum");
        if (StringUtils.isNotEmpty(optionsEnumClass)) {
            resultComponent.setOptionsEnum(getScripting().loadClass(optionsEnumClass));
        }
    }

    /*
    TODO: legacy-ui
    @SuppressWarnings("unchecked")
    @Override
    protected void loadDatasource(DatasourceComponent component, Element element) {
        String datasource = element.attributeValue("optionsDatasource");
        if (!StringUtils.isEmpty(datasource)) {
            Datasource ds = getComponentContext().getDsContext().get(datasource);
            ((T) component).setOptionsDatasource((CollectionDatasource) ds);
        }

        super.loadDatasource(component, element);
    }*/

    @SuppressWarnings("unchecked")
    @Override
    protected void loadData(T component, Element element) {
        super.loadData(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));
    }
}
