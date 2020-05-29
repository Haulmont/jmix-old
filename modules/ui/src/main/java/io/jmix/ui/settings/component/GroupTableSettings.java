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

package io.jmix.ui.settings.component;

import java.util.List;

public class GroupTableSettings extends TableSettings {

    protected List<String> groupProperties;

    public GroupTableSettings() {
    }

    public List<String> getGroupProperties() {
        return groupProperties;
    }

    public void setGroupProperties(List<String> groupProperties) {
        this.groupProperties = groupProperties;
    }
}
