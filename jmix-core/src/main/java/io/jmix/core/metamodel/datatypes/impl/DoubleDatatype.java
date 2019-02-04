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
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.FormatStrings;
import io.jmix.core.metamodel.datatypes.FormatStringsRegistry;
import io.jmix.core.AppBeans;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@JavaClass(Double.class)
public class DoubleDatatype extends NumberDatatype implements Datatype<Double> {

    public DoubleDatatype(Element element) {
        super(element);
    }

    @Override
    public String format(Object value) {
        return value == null ? "" : createFormat().format(value);
    }

    @Override
    public String format(Object value, Locale locale) {
        if (value == null) {
            return "";
        }

        FormatStrings formatStrings = AppBeans.get(FormatStringsRegistry.class).getFormatStrings(locale);
        if (formatStrings == null) {
            return format(value);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        NumberFormat format = new DecimalFormat(formatStrings.getDoubleFormat(), formatSymbols);
        return format.format(value);
    }

    @Override
    public Double parse(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return parse(value, createFormat()).doubleValue();
    }

    @Override
    public Double parse(String value, Locale locale) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        FormatStrings formatStrings = AppBeans.get(FormatStringsRegistry.class).getFormatStrings(locale);
        if (formatStrings == null) {
            return parse(value);
        }

        DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();
        NumberFormat format = new DecimalFormat(formatStrings.getDoubleFormat(), formatSymbols);
        return parse(value, format).doubleValue();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Deprecated
    public final static String NAME = "double";
}