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

package io.jmix.ui.components;

import com.haulmont.bali.events.Subscription;

import java.util.function.Consumer;

public interface VisibilityChangeNotifier {

    Subscription addVisibilityChangeListener(Consumer<VisibilityChangeEvent> listener);

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeVisibilityChangeListener(Consumer<VisibilityChangeEvent> listener);

    class VisibilityChangeEvent {
        private Component component;
        private boolean visible;

        public VisibilityChangeEvent(Component component, boolean visible) {
            this.component = component;
            this.visible = visible;
        }

        public Component getComponent() {
            return component;
        }

        public boolean isVisible() {
            return visible;
        }
    }
}