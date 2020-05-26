/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.components.calendar.screens;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.model.common.ScheduledTask;
import io.jmix.ui.component.Calendar;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;

@SuppressWarnings("unchecked")
@UiController
@UiDescriptor("calendar-screen.xml")
public class CalendarScreen extends Screen {

    @Autowired
    protected Metadata metadata;

    @Autowired
    public CollectionContainer<ScheduledTask> tasksDc;

    @Autowired
    protected Calendar calendarDefault;
    @Autowired
    protected Calendar<Date> calendarDate;
    @Autowired
    protected Calendar<Date> calendarDateTime;
    @Autowired
    protected Calendar<LocalDate> calendarLocalDate;
    @Autowired
    protected Calendar<LocalDateTime> calendarLocalDateTime;
    @Autowired
    protected Calendar<OffsetDateTime> calendarOffsetDateTime;
    @Autowired
    protected Calendar calendarWithContainer;

    @Subscribe
    protected void onInit(InitEvent event) {
        initDataContainer();
        initFields();
    }

    protected void initDataContainer() {
        ScheduledTask task = metadata.create(ScheduledTask.class);
        tasksDc.setItems(Collections.singletonList(task));
    }

    protected void initFields() {
        calendarDefault.setStartDate(new Date());
        calendarDefault.setEndDate(new Date());

        calendarDate.setStartDate(new Date());
        calendarDate.setEndDate(new Date());

        calendarDateTime.setStartDate(new Date());
        calendarDateTime.setEndDate(new Date());

        calendarLocalDate.setEndDate(LocalDate.now());
        calendarLocalDate.setStartDate(LocalDate.now());

        calendarLocalDateTime.setStartDate(LocalDateTime.now());
        calendarLocalDateTime.setEndDate(LocalDateTime.now());

        calendarOffsetDateTime.setStartDate(OffsetDateTime.now());
        calendarOffsetDateTime.setEndDate(OffsetDateTime.now());
    }
}
