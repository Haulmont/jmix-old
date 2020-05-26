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

import io.jmix.ui.widget.JmixTextArea;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.ui.textarea.TextAreaConnector;
import com.vaadin.shared.ui.Connect;

@Connect(JmixTextArea.class)
public class JmixTextAreaConnector extends TextAreaConnector {

    @Override
    public JmixTextAreaWidget getWidget() {
        return (JmixTextAreaWidget) super.getWidget();
    }

    @Override
    public JmixTextAreaState getState() {
        return (JmixTextAreaState) super.getState();
    }

    @OnStateChange("caseConversion")
    void updateCaseConversion() {
        getWidget().setCaseConversion(getState().caseConversion);
    }
}
