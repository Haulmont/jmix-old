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

package io.jmix.ui.widget.client.listselect.single;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.vaadin.shared.data.DataCommunicatorConstants;
import com.vaadin.shared.ui.ListingJsonConstants;
import elemental.js.json.JsJsonObject;
import elemental.json.JsonObject;
import io.jmix.ui.widget.client.listselect.JmixAbstractListSelectWidget;

import java.util.List;
import java.util.function.Consumer;

public class JmixSingleListSelectWidget extends JmixAbstractListSelectWidget {

    protected Consumer<Boolean> selectionHandler;

    protected boolean nullOptionVisible = true;
    protected boolean nullOptionSelected = false;

    public JmixSingleListSelectWidget() {
        select.setMultipleSelect(false);
        select.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (isNullOptionVisible() && selectionHandler != null) {
                    nullOptionSelected = getSelectedItemKeys().contains("nullItem");
                }
            }
        });
    }

    @Override
    public void setDoubleClickListener(Consumer<Integer> doubleClickListener) {
        super.setDoubleClickListener(new Consumer<Integer>() {
            @Override
            public void accept(Integer index) {
                if (isNullOptionVisible()) {
                    index--;

                    // don't fire event for null item
                    if (index < 0) {
                        return;
                    }
                }

                if (doubleClickListener != null) {
                    doubleClickListener.accept(index);
                }
            }
        });
    }

    public Consumer<Boolean> getNullSelectHandler() {
        return selectionHandler;
    }

    public void setSelectionHandler(Consumer<Boolean> selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    @Override
    public void setItems(List<JsonObject> items) {
        if (isNullOptionVisible()) {
            JsonObject nullObject = JsJsonObject.create();
            nullObject.put(DataCommunicatorConstants.KEY, "nullItem");
            nullObject.put(ListingJsonConstants.JSONKEY_ITEM_VALUE, "");

            if (nullOptionSelected) {
                nullObject.put(ListingJsonConstants.JSONKEY_ITEM_SELECTED, true);
            }

            items.add(0, nullObject);
        }

        super.setItems(items);
    }

    public boolean isNullOptionVisible() {
        return nullOptionVisible;
    }

    public void setNullOptionVisible(boolean nullOptionVisible) {
        this.nullOptionVisible = nullOptionVisible;
    }
}
