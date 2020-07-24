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

package io.jmix.ui.icon;

import com.vaadin.server.Resource;

import javax.annotation.Nullable;

/**
 * Marker interface for beans that can provide {@link Resource} by some icon path.
 * <p>
 * The {@link IconResolver} bean obtains all beans that implements {@link IconProvider} interface and iterates over them
 * to find the one that can provide {@link Resource} for icon. So you should use this interface to mark new icon provider
 * to use your custom icon set.
 */
public interface IconProvider {

    /**
     * Defines the highest precedence for {@link org.springframework.core.Ordered} or
     * {@link org.springframework.core.annotation.Order} icon providers.
     */
    int HIGHEST_PLATFORM_PRECEDENCE = 100;

    /**
     * Defines the lowest precedence for {@link org.springframework.core.Ordered} or
     * {@link org.springframework.core.annotation.Order} icon providers.
     */
    int LOWEST_PLATFORM_PRECEDENCE = 1000;

    /**
     * @return an instance of {@link Resource} by the given {@code iconPath}.
     */
    Resource getIconResource(String iconPath);

    /**
     * @return true if icon provider can return an instance of {@link Resource} by the given {@code iconPath}
     * or false otherwise
     */
    boolean canProvide(@Nullable String iconPath);
}
