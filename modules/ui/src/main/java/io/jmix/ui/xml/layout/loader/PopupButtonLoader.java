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
package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.Actions;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.PopupButton.PopupOpenDirection;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.List;

public class PopupButtonLoader extends AbstractComponentLoader<PopupButton> {

    protected ComponentLoader popupComponentLoader;

    @Override
    public void createComponent() {
        resultComponent = factory.create(PopupButton.NAME);
        loadId(resultComponent, element);

        createContent();
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);
        loadAlign(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);
        loadIcon(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadTabIndex(resultComponent, element);

        loadShowActionIcons(resultComponent, element);
        loadActions(resultComponent, element);

        loadPopupComponent();

        loadAutoClose(resultComponent, element);
        loadTogglePopupVisibilityOnClick(resultComponent, element);
        loadClosePopupOnOutsideClick(resultComponent, element);
        loadPopupOpenDirection(resultComponent, element);
        loadMenuWidth(resultComponent, element);

        String menuWidth = element.attributeValue("menuWidth");
        if (StringUtils.isNotEmpty(menuWidth)) {
            resultComponent.setMenuWidth(menuWidth);
        }

        loadFocusable(resultComponent, element);
    }

    @Override
    protected void loadActions(ActionsHolder actionsHolder, Element element) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        for (Element actionEl : actionsEl.elements("action")) {
            Action action = loadDeclarativeAction(actionsHolder, actionEl);
            actionsHolder.addAction(action);

            ComponentContext componentContext = getComponentContext();

            componentContext.addPostInitTask(new ActionHolderAssignActionPostInitTask(actionsHolder,
                    action.getId(), componentContext.getFrame()));
        }
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        String type = element.attributeValue("type");
        if (StringUtils.isNotEmpty(type)) {
            Actions actions = beanLocator.get(Actions.NAME);

            String id = loadActionId(element);
            Action action = actions.create(type, id);
            initAction(element, action);

            return action;
        }

        return loadDeclarativeActionDefault(actionsHolder, element);
    }

    protected void loadAutoClose(PopupButton component, Element element) {
        String autoClose = element.attributeValue("autoClose");
        if (StringUtils.isNotEmpty(autoClose)) {
            component.setAutoClose(Boolean.parseBoolean(autoClose));
        }
    }

    protected void loadMenuWidth(PopupButton component, Element element) {
        String menuWidth = element.attributeValue("menuWidth");
        if (StringUtils.isNotEmpty(menuWidth)) {
            if ("auto".equalsIgnoreCase(menuWidth)) {
                component.setMenuWidth(Component.AUTO_SIZE);
            } else {
                component.setMenuWidth(loadThemeString(menuWidth));
            }
        }
    }

    protected void loadTogglePopupVisibilityOnClick(PopupButton component, Element element) {
        String togglePopupVisibilityOnClick = element.attributeValue("togglePopupVisibilityOnClick");
        if (StringUtils.isNotEmpty(togglePopupVisibilityOnClick)) {
            component.setTogglePopupVisibilityOnClick(Boolean.parseBoolean(togglePopupVisibilityOnClick));
        }
    }

    protected void loadClosePopupOnOutsideClick(PopupButton component, Element element) {
        String closePopupOnOutsideClick = element.attributeValue("closePopupOnOutsideClick");
        if (StringUtils.isNotEmpty(closePopupOnOutsideClick)) {
            component.setClosePopupOnOutsideClick(Boolean.parseBoolean(closePopupOnOutsideClick));
        }
    }

    protected void loadPopupOpenDirection(PopupButton component, Element element) {
        String popupOpenDirection = element.attributeValue("popupOpenDirection");
        if (StringUtils.isNotEmpty(popupOpenDirection)) {
            component.setPopupOpenDirection(PopupOpenDirection.valueOf(popupOpenDirection));
        }
    }

    protected void loadShowActionIcons(PopupButton component, Element element) {
        String showActionIcons = element.attributeValue("showActionIcons");
        if (StringUtils.isNotEmpty(showActionIcons)) {
            component.setShowActionIcons(Boolean.parseBoolean(showActionIcons));
        }
    }

    protected void createContent() {
        if (element != null && element.element("popup") != null) {
            LayoutLoader loader = getLayoutLoader();

            List<Element> elements = element.element("popup").elements();
            if (elements.size() != 0) {
                Element innerElement = elements.get(0);
                if (innerElement != null) {
                    popupComponentLoader = loader.createComponent(innerElement);
                    resultComponent.setPopupContent(popupComponentLoader.getResultComponent());
                    resultComponent.setAutoClose(false);
                }
            }
        }
    }

    protected void loadPopupComponent() {
        if (popupComponentLoader != null) {
            popupComponentLoader.loadComponent();
        }
    }
}
