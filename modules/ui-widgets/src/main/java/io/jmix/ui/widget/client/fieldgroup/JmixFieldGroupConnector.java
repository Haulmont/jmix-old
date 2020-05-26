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

package io.jmix.ui.widget.client.fieldgroup;

import io.jmix.ui.widget.JmixFieldGroup;
import io.jmix.ui.widget.client.groupbox.JmixGroupBoxConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;

@Connect(JmixFieldGroup.class)
public class JmixFieldGroupConnector extends JmixGroupBoxConnector {

    @Override
    public JmixFieldGroupWidget getWidget() {
        return (JmixFieldGroupWidget) super.getWidget();
    }

    @Override
    public JmixFieldGroupState getState() {
        return (JmixFieldGroupState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("borderVisible")) {
            getWidget().setBorderVisible(getState().borderVisible);
        }
    }
}
