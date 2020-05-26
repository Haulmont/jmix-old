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
package io.jmix.ui.component;

import com.google.common.reflect.TypeToken;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;

/**
 * A component for editing textual data that fits on a single line. For a multi-line textarea, see the
 * {@link TextArea} component.
 *
 * @param <V> type of value
 */
public interface TextField<V>
        extends
            TextInputField<V>,
            HasDatatype<V>,
            HasFormatter<V>,
            TextInputField.MaxLengthLimited,
            TextInputField.TrimSupported,
            TextInputField.TextSelectionSupported,
            TextInputField.TextChangeNotifier,
            TextInputField.EnterPressNotifier,
            TextInputField.CursorPositionSupported,
            TextInputField.CaseConversionSupported,
            HasInputPrompt,
            HasConversionErrorMessage,
            TextInputField.HtmlNameSupported {

    String NAME = "textField";

    TypeToken<TextField<String>> TYPE_DEFAULT = new TypeToken<TextField<String>>(){};
    TypeToken<TextField<String>> TYPE_STRING = new TypeToken<TextField<String>>(){};

    TypeToken<TextField<Integer>> TYPE_INTEGER = new TypeToken<TextField<Integer>>(){};
    TypeToken<TextField<Long>> TYPE_LONG = new TypeToken<TextField<Long>>(){};
    TypeToken<TextField<Double>> TYPE_DOUBLE = new TypeToken<TextField<Double>>(){};
    TypeToken<TextField<BigDecimal>> TYPE_BIGDECIMAL = new TypeToken<TextField<BigDecimal>>(){};

    TypeToken<TextField<Date>> TYPE_DATE = new TypeToken<TextField<Date>>(){};
    TypeToken<TextField<java.util.Date>> TYPE_DATETIME = new TypeToken<TextField<java.util.Date>>(){};
    TypeToken<TextField<LocalDate>> TYPE_LOCALDATE = new TypeToken<TextField<LocalDate>>(){};
    TypeToken<TextField<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<TextField<LocalDateTime>>(){};
    TypeToken<TextField<Time>> TYPE_TIME = new TypeToken<TextField<Time>>(){};
    TypeToken<TextField<OffsetTime>> TYPE_OFFSETTIME = new TypeToken<TextField<OffsetTime>>(){};

    /**
     * Returns a string representation of the value.
     */
    String getRawValue();
}