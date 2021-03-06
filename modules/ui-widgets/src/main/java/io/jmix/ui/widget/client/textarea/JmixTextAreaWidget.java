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

package io.jmix.ui.widget.client.textarea;

import com.google.gwt.dom.client.Element;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ui.VTextArea;

public class JmixTextAreaWidget extends VTextArea {
    protected static final String DISABLED_OR_READONLY_CLASSNAME = "c-disabled-or-readonly";
    protected static final String PROMPT_STYLE = "prompt";
    protected static final String EMPTY_VALUE_CLASSNAME = "c-empty-value";

    protected static final String CASE_CONVERSION_MODE_NONE = "NONE";

    protected String caseConversion = CASE_CONVERSION_MODE_NONE;

    protected JmixTextAreaWidget() {
        super();

        addInputHandler(getElement());

        disableAutocompletion();
    }

    protected void disableAutocompletion() {
        if (BrowserInfo.get().isChrome()) {
            // Chrome supports "off" and random number does not work with Chrome
            getElement().setAttribute("autocomplete", "off");
        } else {
            getElement().setAttribute("autocomplete", Math.random() + "");
        }
    }

    protected native void addInputHandler(Element elementID)/*-{
        var temp = this;  // hack to hold on to 'this' reference

        var listener = $entry(function (e) {
            temp.@io.jmix.ui.widget.client.textarea.JmixTextAreaWidget::handleInput()();
        });

        if (elementID.addEventListener) {
            elementID.addEventListener("input", listener, false);
        } else {
            elementID.attachEvent("input", listener);
        }
    }-*/;

    public void handleInput() {
        if (CASE_CONVERSION_MODE_NONE.equals(caseConversion))
            return;

        String text = applyCaseConversion(getText());

        int cursorPos = getCursorPos();
        setText(text);
        setCursorPos(cursorPos);
    }

    protected String applyCaseConversion(String text) {
        if ("UPPER".equals(caseConversion)) {
            return text.toUpperCase();
        } else if ("LOWER".equals(caseConversion)) {
            return text.toLowerCase();
        } else {
            return text;
        }
    }

/*  vaadin8
    @Override
    public void setText(String text) {
        String styleName = getStyleName();
        if (prompting) {
            super.setText(text);
        } else {
            String convertedText = applyCaseConversion(text);

            super.setText(convertedText);

            if (!convertedText.equals(text)) {
                valueChange(false);
            }
        }

        if ("".equals(text) || text == null) {
            addStyleName(CUBA_EMPTY_VALUE);
        } else {
            if (styleName.contains(PROMPT_STYLE)) {
                addStyleName(CUBA_EMPTY_VALUE);
            } else {
                removeStyleName(CUBA_EMPTY_VALUE);
            }
        }
    }*/

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        refreshEnabledOrReadonly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        refreshEnabledOrReadonly();
    }

    protected void refreshEnabledOrReadonly() {
        if (!isEnabled() || isReadOnly()) {
            addStyleName(DISABLED_OR_READONLY_CLASSNAME);
        } else {
            removeStyleName(DISABLED_OR_READONLY_CLASSNAME);
        }
    }

    public void setCaseConversion(String caseConversion) {
        this.caseConversion = caseConversion;
    }
}
