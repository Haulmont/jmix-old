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

package io.jmix.ui.widgets;

import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.ItemCaptionGenerator;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class JmixSearchSelectPickerField<T> extends JmixPickerField<T> {

    protected static final String SEARCHSELECT_PICKERFIELD_STYLENAME = "c-searchselect-pickerfield";
    protected static final String SEARCHSELECT_FIELD_STYLENAME = "c-pickerfield-searchselect";

    @Override
    protected void init() {
        super.init();

        addStyleName(SEARCHSELECT_PICKERFIELD_STYLENAME);
        fieldReadOnly = false;
    }

    @Override
    protected void initField() {
        JmixSearchSelect<T> field = new JmixSearchSelect<>();
        field.addStyleName(SEARCHSELECT_FIELD_STYLENAME);

        this.field = field;

        (getFieldInternal()).addValueChangeListener(this::onFieldValueChange);
    }

    protected JmixSearchSelect<T> getFieldInternal() {
        //noinspection unchecked
        return (JmixSearchSelect<T>) field;
    }

    @Override
    protected void doSetValue(T value) {
        getFieldInternal().setValue(value);
    }

    @Override
    public T getValue() {
        return getFieldInternal().getValue();
    }

    @Override
    protected void updateFieldReadOnlyFocusable() {
        // do nothing
    }

    @Override
    public Registration addValueChangeListener(HasValue.ValueChangeListener<T> listener) {
        return getFieldInternal().addValueChangeListener(listener);
    }

    public void setItems(T... items) {
        getFieldInternal().setItems(items);
    }

    public void setItems(Stream<T> streamOfItems) {
        getFieldInternal().setItems(streamOfItems);
    }

    public void setItems(Collection<T> items) {
        getFieldInternal().setItems(items);
    }

    public void setItems(ComboBox.CaptionFilter captionFilter, Collection<T> items) {
        getFieldInternal().setItems(captionFilter, items);
    }

    public void setItems(ComboBox.CaptionFilter captionFilter, T... items) {
        getFieldInternal().setItems(captionFilter, items);
    }

    public String getEmptySelectionCaption() {
        return getFieldInternal().getEmptySelectionCaption();
    }

    public void setEmptySelectionCaption(String caption) {
        getFieldInternal().setEmptySelectionCaption(caption);
    }

    public boolean isTextInputAllowed() {
        return getFieldInternal().isTextInputAllowed();
    }

    public void setTextInputAllowed(boolean textInputAllowed) {
        getFieldInternal().setTextInputAllowed(textInputAllowed);
    }

    public int getPageLength() {
        return getFieldInternal().getPageLength();
    }

    public void setPageLength(int pageLength) {
        getFieldInternal().setPageLength(pageLength);
    }

    public boolean isEmptySelectionAllowed() {
        return getFieldInternal().isEmptySelectionAllowed();
    }

    public void setEmptySelectionAllowed(boolean emptySelectionAllowed) {
        getFieldInternal().setEmptySelectionAllowed(emptySelectionAllowed);
    }

    public IconGenerator<T> getItemIconGenerator() {
        return getFieldInternal().getItemIconGenerator();
    }

    public void setItemIconGenerator(IconGenerator<T> itemIconGenerator) {
        getFieldInternal().setItemIconGenerator(itemIconGenerator);
    }

    public String getPlaceholder() {
        return getFieldInternal().getPlaceholder();
    }

    public void setPlaceholder(String placeholder) {
        getFieldInternal().setPlaceholder(placeholder);
    }

    public ItemCaptionGenerator<T> getItemCaptionGenerator() {
        return getFieldInternal().getItemCaptionGenerator();
    }

    public void setItemCaptionGenerator(ItemCaptionGenerator<T> itemCaptionGenerator) {
        getFieldInternal().setItemCaptionGenerator(itemCaptionGenerator);
    }

    public String getPopupWidth() {
        return getFieldInternal().getPopupWidth();
    }

    public void setPopupWidth(String width) {
        getFieldInternal().setPopupWidth(width);
    }

    public void setFilterHandler(Consumer<String> filterHandler) {
        getFieldInternal().setFilterHandler(filter -> {
            if (filterHandler != null) {
                filterHandler.accept(filter);
            }
        });
    }

    public void setOptionsStyleProvider(Function<T, String> styleProvider) {
        getFieldInternal().setStyleGenerator(item ->
                styleProvider != null ?
                        styleProvider.apply(item) : "");
    }
}