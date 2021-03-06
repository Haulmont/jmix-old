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

package io.jmix.ui.widget.client.timefield;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.shared.DateTimeFormat;
import io.jmix.ui.widget.client.textfield.JmixMaskedFieldWidget;

import java.util.Locale;
import java.util.Objects;

public class JmixTimeFieldWidget extends JmixMaskedFieldWidget {

    public static final String CLASSNAME = "c-timefield";

    protected static final String EMPTY_FIELD_CLASSNAME = "c-timefield-empty";

    protected TimeResolution resolution = TimeResolution.HOUR;
    protected String timeFormat;

    @Override
    public String getValueConsideringMaskedMode() {
        return parseValue(super.getValueConsideringMaskedMode());
    }

    @Override
    public void setMask(String mask) {
        super.setMask(mask);

        // Update value to replace placeholders with default values
        updateValueWithChangeEvent(false);
    }

    @Override
    public void valueChange(boolean blurred) {
        updateValueWithChangeEvent(true);
    }

    protected void updateValueWithChangeEvent(boolean fireEvent) {
        String newText = getValue();
        if (!Objects.equals(newText, valueBeforeEdit)) {
            String parsedValue = parseValue(newText);
            setValue(parsedValue);

            if (!Objects.equals(parsedValue, valueBeforeEdit)) {
                valueBeforeEdit = parsedValue;

                if (fireEvent) {
                    ValueChangeEvent.fire(this, parsedValue);
                }
            }
        }
    }

    protected String parseValue(String newText) {
        if (!newText.equals(valueBeforeEdit)) {
            newText = (newText.endsWith("__") && !newText.startsWith("__"))
                    ? newText.replaceAll("__", "00")
                    : newText;

            if (validateText(newText) && isValueParsable(newText)) {
                return newText;
            }
        }

        return valueBeforeEdit;
    }

    protected boolean isValueParsable(String time) {
        if (nullRepresentation.equals(time)) {
            return true;
        }

        if (timeFormat == null || timeFormat.isEmpty()) {
            // let server side to parse
            return true;
        }

        DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(timeFormat);
        try {
            dateTimeFormat.parseStrict(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    protected String getEmptyFieldClass() {
        return EMPTY_FIELD_CLASSNAME;
    }

    public TimeResolution getResolution() {
        return resolution;
    }

    public void setResolution(TimeResolution resolution) {
        this.resolution = resolution;
    }

    public String resolutionAsString() {
        return resolution.name().toLowerCase(Locale.ROOT);
    }
}
