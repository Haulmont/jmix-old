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

package io.jmix.ui.settings.impl;

import io.jmix.core.ClientType;
import io.jmix.ui.settings.UserSettingService;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Set;

@Service(UserSettingService.NAME)
public class EmptyUserSettingServiceImpl implements UserSettingService {

    @Override
    public String loadSetting(String name) {
        return null;
    }

    @Override
    public String loadSetting(ClientType clientType, String name) {
        return null;
    }

    @Override
    public void saveSetting(String name, String value) {
        // do nothing
    }

    @Override
    public void saveSetting(ClientType clientType, String name, @Nullable String value) {
        // do nothing
    }

    @Override
    public void deleteSettings(ClientType clientType, String name) {
        // do nothing
    }

    @Override
    public void deleteScreenSettings(ClientType clientType, Set<String> screens) {
        // do nothing
    }
}
