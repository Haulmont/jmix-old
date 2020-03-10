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

import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import com.haulmont.cuba.web.components.Label;
import io.jmix.ui.xml.layout.loaders.LabelLoader;
import org.dom4j.Element;

public class CubaLabelLoader extends LabelLoader {

    @Override
    public void createComponent() {
        resultComponent = factory.create(Label.NAME);
        loadId(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void loadData(io.jmix.ui.components.Label component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper.loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, getContext(),
                (ComponentLoaderContext) getComponentContext());
    }
}
