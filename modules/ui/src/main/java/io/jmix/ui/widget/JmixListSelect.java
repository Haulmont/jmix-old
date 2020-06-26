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

package io.jmix.ui.widget;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.CompositeErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.ListSelect;
import io.jmix.ui.widget.client.listselect.JmixListSelectServerRpc;

import java.util.function.Consumer;

public class JmixListSelect<V> extends ListSelect<V> {

    protected Consumer<V> doubleClickHandler;

    protected JmixListSelectServerRpc listSelectServerRpc = new JmixListSelectServerRpc() {

        @SuppressWarnings("unchecked")
        @Override
        public void onDoubleClick(Integer itemIndex) {
            if (doubleClickHandler != null && itemIndex >= 0) {
                ListDataProvider<V> container = (ListDataProvider<V>) getDataProvider();
                if (container != null && itemIndex < container.size(new Query<>())) {
                    int count = 0;
                    for (V item : container.getItems()) {
                        if (count == itemIndex) {
                            doubleClickHandler.accept(item);
                            break;
                        }
                        count++;
                    }
                }
            }
        }
    };

    public JmixListSelect() {
        registerRpc(listSelectServerRpc);
    }

    public Consumer<V> getDoubleClickHandler() {
        return doubleClickHandler;
    }

    public void setDoubleClickHandler(Consumer<V> doubleClickHandler) {
        this.doubleClickHandler = doubleClickHandler;
    }

    @Override
    public ErrorMessage getErrorMessage() {
        ErrorMessage superError = super.getErrorMessage();
        if (!isReadOnly() && isRequiredIndicatorVisible() && isEmpty()) {
            ErrorMessage error = AbstractErrorMessage.getErrorMessageForException(
                    new com.vaadin.v7.data.Validator.EmptyValueException(getRequiredError()));
            if (error != null) {
                return new CompositeErrorMessage(superError, error);
            }
        }
        return superError;
    }
}
