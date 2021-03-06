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

package io.jmix.ui.component;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A filtering dropdown single-select component. Items are filtered based on user input.
 *
 * @param <V> type of options and value
 */
public interface ComboBox<V> extends OptionsField<V, V>, HasInputPrompt, Buffered, LookupComponent,
        Component.Focusable, HasOptionsStyleProvider<V> {

    String NAME = "comboBox";

    TypeToken<ComboBox<String>> TYPE_STRING = new TypeToken<ComboBox<String>>(){};

    static <T> TypeToken<ComboBox<T>> of(Class<T> valueClass) {
        return new TypeToken<ComboBox<T>>() {};
    }

    /**
     * @return the null selection caption, not {@code null}
     */
    String getNullSelectionCaption();

    /**
     * Sets the null selection caption.
     * <p>
     * The empty string {@code ""} is the default null selection caption.
     * <p>
     * If null selection is allowed then the null item will be shown with the given caption.
     *
     * @param nullOption the caption to set, not {@code null}
     */
    void setNullSelectionCaption(String nullOption);

    FilterMode getFilterMode();
    void setFilterMode(FilterMode mode);

    /**
     * @return true if text input allowed
     */
    boolean isTextInputAllowed();
    /**
     * Sets whether it is possible to input text into the field or whether the field area of the component is just used
     * to show what is selected.
     */
    void setTextInputAllowed(boolean textInputAllowed);

    /**
     * When enabled popup automatically opens on focus.
     */
    void setAutomaticPopupOnFocus(boolean automaticPopupOnFocus);

    /**
     * @return whether popup is automatically shows on focus.
     */
    boolean isAutomaticPopupOnFocus();

    /**
     * @return current handler
     */
    @Nullable
    Consumer<String> getNewOptionHandler();

    /**
     * Sets the handler that is called when user types a new item.
     *
     * @param newOptionHandler handler instance
     */
    void setNewOptionHandler(@Nullable Consumer<String> newOptionHandler);

    /**
     * @return the page length of the suggestion popup.
     */
    int getPageLength();
    /**
     * Sets the page length for the suggestion popup. Setting the page length to
     * 0 will disable suggestion popup paging (all items visible).
     *
     * @param pageLength the pageLength to set
     */
    void setPageLength(int pageLength);

    /**
     * Sets visibility for first null element in suggestion popup.
     */
    void setNullOptionVisible(boolean nullOptionVisible);
    /**
     * @return true if first null element is visible.
     */
    boolean isNullOptionVisible();

    /**
     * Set the icon provider for the LookupField.
     *
     * @param optionIconProvider provider which provides icons for options
     */
    void setOptionIconProvider(@Nullable Function<? super V, String> optionIconProvider);

    /**
     * @return icon provider of the LookupField.
     */
    @Nullable
    Function<? super V, String> getOptionIconProvider();

    /**
     * Sets a function that provides option images.
     *
     * @see Resource
     * @param optionImageProvider options image provider
     */
    void setOptionImageProvider(@Nullable Function<? super V, Resource> optionImageProvider);

    /**
     * @return options image provider.
     */
    @Nullable
    Function<? super V, Resource> getOptionImageProvider();

    /**
     * Enables to setup how items should be filtered.
     *
     * @param filterPredicate items filter predicate
     */
    void setFilterPredicate(@Nullable FilterPredicate filterPredicate);

    /**
     * @return items filter predicate
     */
    @Nullable
    FilterPredicate getFilterPredicate();

    /**
     * Returns the suggestion pop-up's width as a string. By default this
     * width is set to {@code null}.
     *
     * @return explicitly set popup width as size string or null if not set
     */
    @Nullable
    String getPopupWidth();

    /**
     * Sets the suggestion pop-up's width as a string. By using relative
     * units (e.g. "50%") it's possible to set the popup's width relative to the
     * LookupField itself.
     * <p>
     * By default this width is set to {@code null} so that the popup's width
     * can be greater than a component width to fit the content of all displayed items.
     * By setting width to "100%" the pop-up's width will be equal to the width of the LookupField.
     *
     * @param width the width
     */
    void setPopupWidth(@Nullable String width);

    /**
     * A predicate that tests whether an item with the given caption matches to the given search string.
     */
    @FunctionalInterface
    interface FilterPredicate {

        /**
         * @param itemCaption  a caption of item
         * @param searchString search string as is
         * @return true if item with the given caption matches to the given search string or false otherwise
         */
        boolean test(String itemCaption, String searchString);
    }

    enum FilterMode {
        NO,
        STARTS_WITH,
        CONTAINS
    }
}