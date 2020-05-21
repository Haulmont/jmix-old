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
 * A text field that supports multi line editing.
 *
 * @param <V> type of value
 */
public interface TextArea<V> extends TextInputField<V>,
                                  HasDatatype<V>,
                                  TextInputField.MaxLengthLimited,
                                  TextInputField.CursorPositionSupported,
                                  TextInputField.TrimSupported,
                                  TextInputField.TextChangeNotifier,
                                  TextInputField.TextSelectionSupported,
                                  TextInputField.CaseConversionSupported,
                                  HasInputPrompt,
                                  HasConversionErrorMessage {

    String NAME = "textArea";

    TypeToken<TextArea<String>> TYPE_DEFAULT = new TypeToken<TextArea<String>>(){};
    TypeToken<TextArea<String>> TYPE_STRING = new TypeToken<TextArea<String>>(){};

    TypeToken<TextArea<Integer>> TYPE_INTEGER = new TypeToken<TextArea<Integer>>(){};
    TypeToken<TextArea<Long>> TYPE_LONG = new TypeToken<TextArea<Long>>(){};
    TypeToken<TextArea<Double>> TYPE_DOUBLE = new TypeToken<TextArea<Double>>(){};
    TypeToken<TextArea<BigDecimal>> TYPE_BIGDECIMAL = new TypeToken<TextArea<BigDecimal>>(){};

    TypeToken<TextArea<Date>> TYPE_DATE = new TypeToken<TextArea<Date>>(){};
    TypeToken<TextArea<java.util.Date>> TYPE_DATETIME = new TypeToken<TextArea<java.util.Date>>(){};
    TypeToken<TextArea<LocalDate>> TYPE_LOCALDATE = new TypeToken<TextArea<LocalDate>>(){};
    TypeToken<TextArea<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<TextArea<LocalDateTime>>(){};
    TypeToken<TextArea<Time>> TYPE_TIME = new TypeToken<TextArea<Time>>(){};
    TypeToken<TextArea<OffsetTime>> TYPE_OFFSETTIME = new TypeToken<TextArea<OffsetTime>>(){};
    
    int getRows();
    void setRows(int rows);

    /**
     * @return the number of columns
     * @deprecated Use {@link #getWidth()} instead
     */
    @Deprecated
    int getColumns();

    /**
     * Sets width according to the number of columns.
     *
     * @param columns the number of columns to set
     * @deprecated Use {@link #setWidth(String)} instead
     */
    @Deprecated
    void setColumns(int columns);

    /**
     * @return whether word wrapping is enabled or not
     */
    boolean isWordWrap();

    /**
     * Sets whether word wrapping is enabled or not.
     *
     * @param wordWrap wordWrap
     */
    void setWordWrap(boolean wordWrap);

    /**
     * Returns a string representation of the value.
     */
    String getRawValue();
}