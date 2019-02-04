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
package io.jmix.core.metamodel.annotations;

import io.jmix.core.entity.annotation.MetaAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an instance name format pattern in the form {0}|{1}, where
 * <ul>
 *     <li>{0} - format string as for {@link String#format}, or a name of this object method, returning string,
 *     with {@code #} symbol in the beginning.</li>
 *     <li>{1} - comma-separated list of field names, corresponding to format {0}. These fields are also used for
 *     defining a {@code _minimal} view of this entity.</li>
 * </ul>
 * Extra spaces between parts are not allowed.
 *
 * <br> Format string example: {@code @NamePattern("%s : %s|name,address")}
 *
 * <br> Method example:
 * {@code @NamePattern("#getCaption|login,name")}
 * <pre>
 * public class User extends StandardEntity {
 * ...
 *     public String getCaption() {
 *         String pattern = AppContext.getProperty("cuba.user.namePattern");
 *         if (StringUtils.isBlank(pattern)) {
 *             pattern = "{1} [{0}]";
 *         }
 *         MessageFormat fmt = new MessageFormat(pattern);
 *         return fmt.format(new Object[] {login, name});
 *     }
 * }
 * </pre>
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MetaAnnotation
public @interface NamePattern {
    String value();
}