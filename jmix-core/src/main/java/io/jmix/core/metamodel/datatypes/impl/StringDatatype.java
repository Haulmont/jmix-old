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

import java.text.ParseException;
import java.util.Locale;

@JavaClass(String.class)
public class StringDatatype implements Datatype<String> {

    @Override
	public String format(Object value) {
		return value == null ? "" : (String) value;
	}

    @Override
    public String format(Object value, Locale locale) {
        return format(value);
    }

    @Override
	public String parse(String value) {
		return value;
	}

    @Override
    public String parse(String value, Locale locale) throws ParseException {
        return value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Deprecated
    public final static String NAME = "string";
}