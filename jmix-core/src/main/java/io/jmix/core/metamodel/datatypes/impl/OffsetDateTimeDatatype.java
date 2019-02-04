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

package io.jmix.core.metamodel.datatypes.impl;

import io.jmix.core.metamodel.annotations.JavaClass;
import io.jmix.core.metamodel.datatypes.FormatStrings;
import io.jmix.core.metamodel.datatypes.TimeZoneAwareDatatype;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.TimeZone;

@JavaClass(OffsetDateTime.class)
public class OffsetDateTimeDatatype extends AbstractTemporalDatatype<OffsetDateTime>
        implements TimeZoneAwareDatatype {

    public OffsetDateTimeDatatype(Element element) {
        super(element);
    }

    @Override
    public String format(@Nullable Object value, Locale locale, TimeZone timeZone) {
        if (timeZone == null || value == null) {
            return format(value, locale);
        }
        OffsetDateTime offsetDateTime = (OffsetDateTime) value;
        LocalDateTime localDateTime = offsetDateTime.atZoneSameInstant(timeZone.toZoneId()).toLocalDateTime();
        return format(localDateTime, locale);
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter(FormatStrings formatStrings, Locale locale) {
        return DateTimeFormatter.ofPattern(formatStrings.getDateTimeFormat(), locale).withZone(ZoneId.systemDefault());
    }

    @Override
    protected TemporalQuery<OffsetDateTime> newInstance() {
        return OffsetDateTime::from;
    }
}