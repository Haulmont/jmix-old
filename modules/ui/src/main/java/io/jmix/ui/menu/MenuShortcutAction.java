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
package io.jmix.ui.menu;

import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Component;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.mainwindow.AppMenu;
import io.jmix.ui.AppUI;
import org.slf4j.LoggerFactory;

public class MenuShortcutAction extends ShortcutListener {

    private static final long serialVersionUID = -5416777300893219886L;

    protected AppMenu.MenuItem menuItem;

    public MenuShortcutAction(AppMenu.MenuItem menuItem, String caption, int kc, int... m) {
        super(caption, kc, m);
        this.menuItem = menuItem;
    }

    public MenuShortcutAction(AppMenu.MenuItem menuItem, String caption, KeyCombination key) {
        this(menuItem, caption, key.getKey().getCode(), KeyCombination.getShortcutModifiers(key.getModifiers()));
    }

    @Override
    public void handleAction(Object sender, Object target) {
        Component menuImpl = menuItem.getMenu().unwrap(Component.class);
        AppUI ui = (AppUI) menuImpl.getUI();
        if (ui.isAccessibleForUser(menuImpl)) {
            menuItem.getCommand().accept(menuItem);
        } else {
            LoggerFactory.getLogger(MenuShortcutAction.class)
                    .debug("Ignoring shortcut action because menu is inaccessible for user");
        }
    }
}
