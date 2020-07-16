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

import com.haulmont.cuba.gui.xml.DeclarativeTrackingAction;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Facet;
import io.jmix.ui.component.Window;
import io.jmix.ui.xml.FacetLoader;
import io.jmix.ui.xml.layout.loader.WindowLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@ParametersAreNonnullByDefault
public class CubaWindowLoader extends WindowLoader {

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTimers(resultComponent, element);
    }

    @Deprecated
    protected void loadTimers(Window resultComponent, Element windowElement) {
        Element timersElement = windowElement.element("timers");
        if (timersElement != null) {
            List<Element> facetElements = timersElement.elements("timer");

            for (Element facetElement : facetElements) {
                FacetLoader loader = beanLocator.get(FacetLoader.NAME);
                Facet facet = loader.load(facetElement, getComponentContext());

                resultComponent.addFacet(facet);
            }
        }
    }

    @Override
    protected Action loadDeclarativeActionDefault(ActionsHolder actionsHolder, Element element) {
        Action action = ComponentLoaderHelper.loadDeclarativeAction(
                actionsHolder,
                element,
                loadActionId(element),
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))))
                .orElse(super.loadDeclarativeActionDefault(actionsHolder, element));

        if (action instanceof DeclarativeTrackingAction) {
            loadActionConstraint(action, element);
        }

        return action;
    }
}
