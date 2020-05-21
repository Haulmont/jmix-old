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

package io.jmix.ui.widget.client.sourcecodeeditor;

import com.google.gwt.core.client.GWT;
import io.jmix.ui.widget.addon.aceeditor.SuggestionExtension;
import io.jmix.ui.widget.client.addon.aceeditor.SuggestPopup;
import io.jmix.ui.widget.client.addon.aceeditor.SuggesterConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(value = SuggestionExtension.class, loadStyle = Connect.LoadStyle.LAZY)
public class JmixSuggesterConnector extends SuggesterConnector {

    @Override
    protected SuggestPopup createSuggestionPopup() {
        SuggestPopup sp = GWT.create(JmixSuggestPopup.class);
        sp.setOwner(widget);
        setPopupPosition(sp);
        sp.setSuggestionSelectedListener(this);
        sp.show();
        return sp;
    }
}
