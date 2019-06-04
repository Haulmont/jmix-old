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

package io.jmix.ui.sys;

import io.jmix.ui.generic.*;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.ScreenOptions;

public class ScreenContextImpl implements ScreenContext {

    protected final ScreenOptions options;
    protected final WindowInfo windowInfo;

    protected final Screens screens;
    protected final Dialogs dialogs;
    protected final Notifications notifications;
    protected final Fragments fragments;

    // todo navigation
    // protected final UrlRouting urlRouting;

    public ScreenContextImpl(WindowInfo windowInfo, ScreenOptions options, ScreenContext hostScreenContext) {
        this.windowInfo = windowInfo;
        this.options = options;

        this.dialogs = hostScreenContext.getDialogs();
        this.fragments = hostScreenContext.getFragments();
        this.notifications = hostScreenContext.getNotifications();
        // todo navigation
//        this.urlRouting = hostScreenContext.getUrlRouting();
        this.screens = hostScreenContext.getScreens();
    }

    public ScreenContextImpl(WindowInfo windowInfo, ScreenOptions options,
                             Screens screens,
                             Dialogs dialogs,
                             Notifications notifications,
                             Fragments fragments/*,  // todo navigation
                             UrlRouting urlRouting*/) {
        this.windowInfo = windowInfo;
        this.options = options;

        this.screens = screens;
        this.dialogs = dialogs;
        this.notifications = notifications;
        this.fragments = fragments;
        // todo navigation
//        this.urlRouting = urlRouting;
    }

    @Override
    public ScreenOptions getScreenOptions() {
        return options;
    }

    @Override
    public WindowInfo getWindowInfo() {
        return windowInfo;
    }

    @Override
    public Screens getScreens() {
        return screens;
    }

    @Override
    public Dialogs getDialogs() {
        return dialogs;
    }

    @Override
    public Notifications getNotifications() {
        return notifications;
    }

    @Override
    public Fragments getFragments() {
        return fragments;
    }

    // todo navigation
    /*@Override
    public UrlRouting getUrlRouting() {
        return urlRouting;
    }*/
}