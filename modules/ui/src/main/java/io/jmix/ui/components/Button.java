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

import io.jmix.core.commons.events.Subscription;

import java.util.EventObject;
import java.util.function.Consumer;

public interface Button extends Component, Component.HasCaption, Component.BelongToFrame, ActionOwner,
                                Component.HasIcon, Component.Focusable, HasHtmlCaption, HasHtmlDescription,
                                HasHtmlSanitizer {
    String NAME = "button";

    /**
     * Determines if a button is automatically disabled when clicked. If this is
     * set to true the button will be automatically disabled when clicked,
     * typically to prevent (accidental) extra clicks on a button.
     *
     * @param disableOnClick disable on click option.
     */
    void setDisableOnClick(boolean disableOnClick);
    /**
     * @return true if the button is disabled when clicked.
     */
    boolean isDisableOnClick();

    /**
     * Enable or disable HTML mode for caption.
     *
     * @param captionAsHtml pass true to enable HTML mode for caption.
     */
    void setCaptionAsHtml(boolean captionAsHtml);
    /**
     * @return true if caption is inserted to DOM as HTML
     */
    boolean isCaptionAsHtml();

    /**
     * @return action's shortcut
     */
    KeyCombination getShortcutCombination();

    /**
     * Sets shortcut combination.
     *
     * @param shortcut key combination
     */
    void setShortcutCombination(KeyCombination shortcut);

    /**
     * Sets shortcut from string representation.
     *
     * @param shortcut string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     */
    void setShortcut(String shortcut);

    Subscription addClickListener(Consumer<ClickEvent> listener);

    /**
     * Event sent when the button is clicked.
     */
    class ClickEvent extends EventObject {
        public ClickEvent(Button source) {
            super(source);
        }

        @Override
        public Button getSource() {
            return (Button) super.getSource();
        }

        public Button getButton() {
            return (Button) super.getSource();
        }
    }
}