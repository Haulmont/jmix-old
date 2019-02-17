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

package io.jmix.core;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Describes a Jmix component which the current application depends on.
 */
public class JmixComponentDescriptor implements Comparable<JmixComponentDescriptor> {

    private final String id;
    private final List<JmixComponentDescriptor> dependencies = new ArrayList<>();
    private Properties properties;
    private Set<String> additiveProperties;

    public JmixComponentDescriptor(String id) {
        this.id = id;
    }

    /**
     * @return app component Id
     */
    public String getId() {
        return id;
    }

    /**
     * INTERNAL.
     * Add a dependency to the component.
     */
    public void addDependency(JmixComponentDescriptor other) {
        if (dependencies.contains(other))
            return;
        if (other.dependsOn(this))
            throw new RuntimeException("Circular dependency between app components '" + this + "' and '" + other + "'");

        dependencies.add(other);
    }

    /**
     * Check if this component depends on the given component.
     */
    public boolean dependsOn(JmixComponentDescriptor other) {
        for (JmixComponentDescriptor dependency : dependencies) {
            if (dependency.equals(other) || dependency.dependsOn(other))
                return true;
        }
        return false;
    }

    /**
     * INTERNAL.
     * Set a file-based app property defined in this app component.
     */
    public void setProperty(String name, String value, boolean additive) {
        if (properties == null)
            properties = new Properties();

        if (additive) {
            if (additiveProperties == null) {
                additiveProperties = new HashSet<>();
            }
            additiveProperties.add(name);
        } else if (additiveProperties != null) {
            additiveProperties.remove(name);
        }

        properties.setProperty(name, value);
    }

    /**
     * @return a file-based app property defined in this app component or null if not found
     */
    @Nullable
    public String getProperty(String property) {
        return properties == null ? null : properties.getProperty(property);
    }

    public boolean isAdditiveProperty(String property) {
        return additiveProperties != null && additiveProperties.contains(property);
    }

    /**
     * @return names of properties exported by this app component, sorted in natural order
     */
    public List<String> getPropertyNames() {
        List<String> list = new ArrayList<>(properties.stringPropertyNames());
        list.sort(Comparator.naturalOrder());
        return list;
    }

    @Override
    public int compareTo(JmixComponentDescriptor other) {
        if (this.dependsOn(other))
            return 1;
        if (other.dependsOn(this)) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JmixComponentDescriptor that = (JmixComponentDescriptor) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
