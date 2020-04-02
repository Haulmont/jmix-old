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

package io.jmix.core;

import io.jmix.core.impl.JavaClassLoader;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Component(HotDeployManager.NAME)
public class HotDeployManager {
    public static final String NAME = "jmix_HotDeployManager";

    @Inject
    protected JavaClassLoader javaClassLoader;

    /**
     * Loads class by name
     *
     * @param className fully qualified class name
     * @return class or null if not found
     */
    @Nullable
    public Class<?> loadClass(String className) {
        try {
            return javaClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Loads a class by name
     *
     * @param className fully qualified class name
     * @return class
     * @throws IllegalStateException if the class is not found
     */
    public Class<?> loadClassNN(String className) {
        try {
            return javaClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load class", e);
        }
    }

    /**
     * Remove compiled class from cache
     *
     * @return true if class removed from cache
     */
    public boolean removeClass(String className) {
        return javaClassLoader.removeClass(className);
    }

    /**
     * Reloads class by name
     *
     * @param className fully qualified class name
     * @return class or null if not found
     */
    public Class<?> reloadClass(String className) {
        javaClassLoader.removeClass(className);
        return loadClass(className);
    }

    /**
     * Clears compiled classes cache
     */
    public void clearCache() {
        javaClassLoader.clearCache();
    }

    public JavaClassLoader getJavaClassLoader() {
        return javaClassLoader;
    }
}
