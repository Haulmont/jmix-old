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
package io.jmix.core.metamodel.model.utils;

import io.jmix.core.entity.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.EntityPropertyPath;

import javax.annotation.Nullable;

/**
 * Utility class to work with {@link Entity}s.
 */
public final class InstanceUtils {

    private InstanceUtils() {
    }

    /**
     * Converts a string of identifiers separated by dots to an array. A part of the given string, enclosed in square
     * brackets, treated as single identifier. For example:
     * <pre>
     *     car.driver.name
     *     [car.field].driver.name
     * </pre>
     * @param path value path as string
     * @return value path as array or empty array if the input is null
     * @deprecated replaced by {@link EntityPaths#parseValuePath(String)}
     */
    @Deprecated
    public static String[] parseValuePath(@Nullable String path) {
        return EntityPaths.parseValuePath(path);
    }

    /**
     * Converts an array of identifiers to a dot-separated string, enclosing identifiers, containing dots, in square
     * brackets.
     * @param path value path as array
     * @return value path as string or empty string if the input is null
     * @deprecated replaced by {@link EntityPaths#formatValuePath(String[])}
     */
    @Deprecated
    public static String formatValuePath(String[] path) {
        return EntityPaths.formatValuePath(path);
    }

    /**
     * Get value of an attribute according to the rules described in {@link EntityValues#getAttributeValueEx(Entity, String)}.
     *
     * @param entity     instance
     * @param propertyPath attribute path
     * @return attribute value
     * @deprecated replaced by {@link EntityValues#getAttributeValueEx(Entity, String)}
     */
    @Deprecated
    public static <T> T getValueEx(Entity<?> entity, String propertyPath) {
        return EntityValues.getAttributeValueEx(entity, propertyPath);
    }

    /**
     * Get value of an attribute according to the rules described in {@link EntityValues#getAttributeValueEx(Entity, EntityPropertyPath)}.
     *
     * @param entity     entity
     * @param propertyPath attribute path
     * @return attribute value
     * @deprecated replaced by {@link EntityValues#getAttributeValueEx(Entity, EntityPropertyPath)}
     */
    @Deprecated
    public static <T> T getValueEx(Entity<?> entity, EntityPropertyPath propertyPath) {
        return EntityValues.getAttributeValueEx(entity, propertyPath);
    }

    /**
     * Get value of an attribute according to the rules described in {@link EntityValues#getAttributeValueEx(Entity, String[])}.
     *
     * @param entity   entity
     * @param properties path to the attribute
     * @return attribute value
     * @deprecated replaced by {@link EntityValues#getAttributeValueEx(Entity, String[])}
     */
    @Deprecated
    public static <T> T getValueEx(Entity<?> entity, String[] properties) {
        return EntityValues.getAttributeValueEx(entity, properties);
    }

    /**
     * Set value of an attribute according to the rules described in {@link EntityValues#setAttributeValueEx(Entity, String, Object)}.
     *
     * @param entity     entity
     * @param propertyPath path to the attribute
     * @param value        attribute value
     * @deprecated replaced by {@link EntityValues#setAttributeValueEx(Entity, String, Object)}
     */
    @Deprecated
    public static void setValueEx(Entity<?> entity, String propertyPath, Object value) {
        EntityValues.setAttributeValueEx(entity, propertyPath, value);
    }

    /**
     * Set value of an attribute according to the rules described in {@link EntityValues#setAttributeValueEx(Entity, EntityPropertyPath, Object)}.
     *
     * @param entity     entity
     * @param propertyPath path to the attribute
     * @param value        attribute value
     * @deprecated replaced by {@link EntityValues#setAttributeValueEx(Entity, EntityPropertyPath, Object)}
     */
    @Deprecated
    public static void setValueEx(Entity entity, EntityPropertyPath propertyPath, Object value) {
        EntityValues.setAttributeValueEx(entity, propertyPath, value);
    }

    /**
     * Set value of an attribute according to the rules described in {@link EntityValues#setAttributeValueEx(Entity, String[], Object)}.
     *
     * @param entity     entity
     * @param properties path to the attribute
     * @param value      attribute value
     * @deprecated replaces by {@link EntityValues#setAttributeValueEx(Entity, String[], Object)}
     */
    @Deprecated
    public static void setValueEx(Entity<?> entity, String[] properties, Object value) {
        EntityValues.setAttributeValueEx(entity, properties, value);
    }

    /**
     * Used by {@link } to check whether a property value has been changed.
     *
     * @param a an object
     * @param b an object
     * @return true if {@code a} equals to {@code b}, but in case of {@code a} is {@link } or {@code Collection} returns
     * true only if {@code a} is the same instance as {@code b}
     * @deprecated replaced by {@link EntityValues#propertyValueEquals(Object, Object)}
     */
    @Deprecated
    public static boolean propertyValueEquals(Object a, Object b) {
       return EntityValues.propertyValueEquals(a, b);
    }
}
