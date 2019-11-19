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

package io.jmix.ui.web.components.datepicker.screens;

import io.jmix.ui.components.DatePicker;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@UiController
@UiDescriptor("datepicker-datatype-screen.xml")
public class DatePickerDatatypeScreen extends Screen {
    @Inject
    private DatePicker<java.sql.Date> datePicker;
    @Inject
    private DatePicker<Date> dateTimePicker;
    @Inject
    private DatePicker<LocalDate> localDatePicker;
    @Inject
    private DatePicker<LocalDateTime> localDateTimePicker;
    @Inject
    private DatePicker<OffsetDateTime> offsetDateTimePicker;

    @Subscribe
    protected void onInit(InitEvent event) {
        Date now = new Date();
        datePicker.setValue(new java.sql.Date(now.getTime()));
        dateTimePicker.setValue(now);
        localDatePicker.setValue(LocalDate.now());
        localDateTimePicker.setValue(LocalDateTime.now());
        offsetDateTimePicker.setValue(OffsetDateTime.now());
    }
}
