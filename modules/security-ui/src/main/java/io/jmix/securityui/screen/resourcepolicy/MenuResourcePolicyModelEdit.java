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

package io.jmix.securityui.screen.resourcepolicy;

import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.menu.MenuConfig;
import io.jmix.ui.menu.MenuItem;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.Map;
import java.util.TreeMap;

@UiController("sec_MenuResourcePolicyModel.edit")
@UiDescriptor("menu-resource-policy-model-edit.xml")
@EditedEntityContainer("resourcePolicyModelDc")
public class MenuResourcePolicyModelEdit extends StandardEditor<ResourcePolicyModel> {

    @Autowired
    private ComboBox<String> menuField;

    @Inject
    private MenuConfig menuConfig;

    @Autowired
    private MessageBundle messageBundle;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        menuField.setOptionsMap(getMenuItemsOptionsMap());
    }

    private Map<String, String> getMenuItemsOptionsMap() {
        Map<String, String> collectedMenus = new TreeMap<>();
        for (MenuItem rootItem : menuConfig.getRootItems()) {
            walkMenuItem(rootItem, collectedMenus);
        }
        collectedMenus.put(messageBundle.getMessage("allMenus"), "*");
        return collectedMenus;
    }

    private void walkMenuItem(MenuItem menuItem, Map<String, String> collectedMenus) {
        collectedMenus.put(getMenuCaption(menuItem), menuItem.getId());
        if (menuItem.getChildren() != null) {
            menuItem.getChildren().forEach(childMenuItem -> walkMenuItem(childMenuItem, collectedMenus));
        }
    }

    private String getMenuCaption(MenuItem menuItem) {
        StringBuilder caption = new StringBuilder(menuConfig.getItemCaption(menuItem));
        MenuItem parent = menuItem.getParent();
        while (parent != null) {
            caption.insert(0, menuConfig.getItemCaption(parent) + " > ");
            parent = parent.getParent();
        }
        return String.format("%s (%s)", caption.toString(), menuItem.getId());
    }
}