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

package io.jmix.ui.component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Base interface for actions aware dialog facets.
 *
 * @see OptionDialogFacet
 * @see InputDialogFacet
 *
 * @param <T> dialog facet type
 */
public interface ActionsAwareDialogFacet<T> {

    /**
     * Sets dialog actions.
     *
     * @param actions actions
     */
    void setActions(Collection<DialogAction<T>> actions);

    /**
     * @return dialog actions
     */
    Collection<DialogAction<T>> getActions();

    /**
     * The event that is fired when {@link DialogAction#actionHandler} is triggered.
     */
    class DialogActionPerformedEvent<T> {

        protected T dialog;
        protected DialogAction dialogAction;

        public DialogActionPerformedEvent(T dialog, DialogAction dialogAction) {
            this.dialog = dialog;
            this.dialogAction = dialogAction;
        }

        public T getDialog() {
            return dialog;
        }

        public DialogAction getDialogAction() {
            return dialogAction;
        }
    }

    /**
     * Immutable POJO that stores dialog action settings.
     */
    class DialogAction<T> {

        protected final String id;
        protected final String caption;
        protected final String description;
        protected final String icon;
        protected final boolean primary;

        protected Consumer<DialogActionPerformedEvent<T>> actionHandler;

        public DialogAction(String id, @Nullable String caption, @Nullable String description, @Nullable String icon, boolean primary) {
            this.id = id;
            this.caption = caption;
            this.description = description;
            this.icon = icon;
            this.primary = primary;
        }

        public String getId() {
            return id;
        }

        @Nullable
        public String getCaption() {
            return caption;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        @Nullable
        public String getIcon() {
            return icon;
        }

        public boolean isPrimary() {
            return primary;
        }

        public Consumer<DialogActionPerformedEvent<T>> getActionHandler() {
            return actionHandler;
        }

        /**
         * INTERNAL.
         * <p>
         * Intended to set handlers via {@code @Install} annotation.
         *
         * @param actionHandler action handler
         */
        public void setActionHandler(Consumer<DialogActionPerformedEvent<T>> actionHandler) {
            this.actionHandler = actionHandler;
        }
    }
}
