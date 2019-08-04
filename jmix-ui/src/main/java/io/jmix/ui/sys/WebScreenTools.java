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

import io.jmix.ui.ScreenTools;
import io.jmix.ui.Screens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(ScreenTools.NAME)
public class WebScreenTools implements ScreenTools {

    private static final Logger log = LoggerFactory.getLogger(WebScreenTools.class);

    /*@Inject
    protected WebConfig webConfig;
    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected UserSettingService userSettingService;*/

    @Override
    public void openDefaultScreen(Screens screens) {
        // todo settings
        /*String defaultScreenId = webConfig.getDefaultScreenId();

        if (webConfig.getUserCanChooseDefaultScreen()) {
            String userDefaultScreen = userSettingService.loadSetting(ClientType.WEB, "userDefaultScreen");

            defaultScreenId = StringUtils.isEmpty(userDefaultScreen)
                    ? defaultScreenId
                    : userDefaultScreen;
        }

        if (StringUtils.isEmpty(defaultScreenId)) {
            return;
        }

        if (!windowConfig.hasWindow(defaultScreenId)) {
            log.info("Can't find default screen: {}", defaultScreenId);
            return;
        }

        Screen screen = screens.create(defaultScreenId, OpenMode.NEW_TAB);

        screen.show();

        Window window = screen.getWindow();

        WebWindow webWindow;
        if (window instanceof Window.Wrapper) {
            webWindow = (WebWindow) ((Window.Wrapper) window).getWrappedWindow();
        } else {
            webWindow = (WebWindow) window;
        }
        webWindow.setDefaultScreenWindow(true);

        if (!webConfig.getDefaultScreenCanBeClosed()) {
            window.setCloseable(false);
        }*/
    }
}
