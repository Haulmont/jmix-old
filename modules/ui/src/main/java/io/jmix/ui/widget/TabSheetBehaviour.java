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

package io.jmix.ui.widget;

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface TabSheetBehaviour extends Serializable {

    void addTab(Component component, String tabId);

    String getTab(Component component);

    String getTab(int position);

    void replaceComponent(Component oldComponent, Component newComponent);

    void removeComponent(Component component);

    @Nullable
    Component getPreviousTab(Component tab);

    void setTabTestId(String tabId, String testId);

    void setTabCubaId(String tabId, String id);

    void setTabCloseHandler(Component tabContent, BiConsumer<HasTabSheetBehaviour, Component> closeHandler);

    int getTabPosition(String tabId);

    int getComponentCount();

    void moveTab(Component c, int position);

    void focus();

    void setTabCaption(String tabId, String caption);

    void setTabDescription(String tabId, @Nullable String description);

    Component getTabComponent(String tabId);

    void setTabIcon(String tabId, @Nullable Resource icon);

    void setTabClosable(String tabId, boolean closable);

    void setContentSwitchMode(String tabId, ContentSwitchMode contentSwitchMode);

    void setSelectedTab(String tabId);

    void setSelectedTab(Component component);

    @Nullable
    Component getSelectedTab();

    Iterator<Component> getTabComponents();

    Stream<Component> getTabComponentsStream();

    void closeTab(Component target);

    void closeOtherTabs(Component target);

    void closeAllTabs();

    void silentCloseTabAndSelectPrevious(Component tab);
}