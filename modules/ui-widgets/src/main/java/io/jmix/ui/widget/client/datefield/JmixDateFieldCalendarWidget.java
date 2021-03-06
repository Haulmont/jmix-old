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

package io.jmix.ui.widget.client.datefield;

import com.vaadin.client.Focusable;
import com.vaadin.client.ui.VDateFieldCalendar;

import java.util.Date;

public class JmixDateFieldCalendarWidget extends VDateFieldCalendar implements Focusable {

    @Override
    public void focus() {
        calendarPanel.focus();
    }

    @Override
    public void updateBufferedValues() {
        Date date = calendarPanel.getDate();
        // Workaround for vaadin/framework#11821
        // if 'calendarPanel.getDate()' value is null then
        // 'super.updateBufferedValues()' throws an exception.
        if (date != null) {
            super.updateBufferedValues();
        }
    }
}
