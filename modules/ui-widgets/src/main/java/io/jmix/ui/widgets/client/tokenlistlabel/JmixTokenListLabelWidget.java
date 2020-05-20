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

package io.jmix.ui.widgets.client.tokenlistlabel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.vaadin.client.ui.VCssLayout;

public class JmixTokenListLabelWidget extends VCssLayout {

    protected static final String CLOSE_STYLENAME = "c-tokenlist-label-close";

    private Label label = new Label();
    private Element closeDiv = DOM.createDiv();

    private boolean editable;
    private boolean canOpen;

    protected TokenListLabelHandler handler;

    public JmixTokenListLabelWidget() {
        add(label);
        label.setStyleName("content");
        label.addClickHandler(event -> {
            if (canOpen) {
                handler.click();
            }
        });

        closeDiv.setClassName(CLOSE_STYLENAME);
        getElement().appendChild(closeDiv);
        DOM.sinkEvents(closeDiv, Event.ONCLICK);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if (!editable) {
            getElement().addClassName("noedit");
        } else {
            getElement().removeClassName("noedit");
        }
    }

    public void setCanOpen(boolean canOpen) {
        this.canOpen = canOpen;
        if (canOpen)
            getElement().addClassName("open");
        else
            getElement().removeClassName("open");
    }

    public void setText(String text) {
        label.setText(text);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONCLICK && handler != null) {
            if (DOM.eventGetTarget(event) == closeDiv && editable) {
                handler.remove();
            }
        }
    }

    public interface TokenListLabelHandler {
        void remove();

        void click();
    }
}
