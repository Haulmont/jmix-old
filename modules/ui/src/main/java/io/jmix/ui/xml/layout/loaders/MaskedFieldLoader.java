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

import io.jmix.ui.components.MaskedField;
import io.jmix.ui.components.MaskedField.ValueMode;
import org.apache.commons.lang3.StringUtils;

public class MaskedFieldLoader extends AbstractTextFieldLoader<MaskedField> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(MaskedField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        String mask = element.attributeValue("mask");
        if (StringUtils.isNotEmpty(mask)) {
            resultComponent.setMask(loadResourceString(mask));
        }
        String valueModeStr = element.attributeValue("valueMode");
        if (StringUtils.isNotEmpty(valueModeStr)) {
            resultComponent.setValueMode(ValueMode.valueOf(valueModeStr.toUpperCase()));
        }

        loadDatatype(resultComponent, element);
    }
}