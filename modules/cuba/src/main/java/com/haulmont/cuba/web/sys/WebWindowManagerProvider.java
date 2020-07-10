/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import io.jmix.core.BeanLocator;
import io.jmix.ui.AppUI;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
@Component(WindowManagerProvider.NAME)
public class WebWindowManagerProvider implements WindowManagerProvider {
    @Autowired
    protected BeanLocator beanLocator;

    @Override
    public WindowManager get() {
        AppUI ui = AppUI.getCurrent();
        return (WindowManager) ui.getScreens();
    }
}
