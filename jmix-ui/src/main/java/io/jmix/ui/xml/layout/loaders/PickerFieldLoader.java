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

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.actions.picker.ClearAction;
import com.haulmont.cuba.gui.actions.picker.LookupAction;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.GuiActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class PickerFieldLoader extends AbstractFieldLoader<PickerField> {
    @Override
    public void createComponent() {
        resultComponent = factory.create(PickerField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            resultComponent.setCaptionMode(CaptionMode.PROPERTY);
            resultComponent.setCaptionProperty(captionProperty);
        }

        final String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            resultComponent.setMetaClass(getMetadata().getClassNN(metaClass));
        }

        loadActions(resultComponent, element);
        if (resultComponent.getActions().isEmpty()) {
            GuiActionSupport guiActionSupport = getGuiActionSupport();

            boolean actionsByMetaAnnotations = guiActionSupport.createActionsByMetaAnnotations(resultComponent);
            if (!actionsByMetaAnnotations) {

                if (isLegacyFrame()) {
                    resultComponent.addLookupAction();
                    resultComponent.addClearAction();
                } else {
                    Actions actions = getActions();

                    resultComponent.addAction(actions.create(LookupAction.ID));
                    resultComponent.addAction(actions.create(ClearAction.ID));
                }
            }
        }

        loadBuffered(resultComponent, element);
    }

    protected Actions getActions() {
        return beanLocator.get(Actions.NAME);
    }

    protected GuiActionSupport getGuiActionSupport() {
        return beanLocator.get(GuiActionSupport.NAME);
    }

    protected Metadata getMetadata() {
        return beanLocator.get(Metadata.NAME);
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadPickerDeclarativeAction(actionsHolder, element);
    }
}