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

package io.jmix.ui.widgets.client.aggregation;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TableCellElement;

public class AggregationInputFieldInfo {

    protected String oldValue;
    protected String columnKey;
    protected InputElement inputElement;
    protected boolean isFocused = false;

    protected TableCellElement td;

    public AggregationInputFieldInfo(String oldValue, String columnKey, InputElement inputElement, TableCellElement td) {
        this(oldValue, columnKey, inputElement);
        this.td = td;
    }

    public AggregationInputFieldInfo(String oldValue, String columnKey, InputElement inputElement) {
        this.oldValue = oldValue;
        this.inputElement = inputElement;
        this.columnKey = columnKey;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public InputElement getInputElement() {
        return inputElement;
    }

    public void setInputElement(InputElement inputElement) {
        this.inputElement = inputElement;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        this.isFocused = focused;
    }

    public TableCellElement getTd() {
        return td;
    }

    public void setTd(TableCellElement td) {
        this.td = td;
    }
}
