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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.SuggestionPickerField;
import io.jmix.core.JmixEntity;
import io.jmix.ui.component.impl.WebEntitySuggestionField;

import java.util.function.Consumer;

@Deprecated
public class WebSuggestionPickerField<V extends JmixEntity> extends WebEntitySuggestionField<V>
        implements SuggestionPickerField<V> {

    @Deprecated
    @Override
    public PickerField.LookupAction addLookupAction() {
        PickerField.LookupAction action = PickerField.LookupAction.create(this);
        addAction(action);
        return action;
    }

    @Override
    @Deprecated
    public PickerField.ClearAction addClearAction() {
        PickerField.ClearAction action = PickerField.ClearAction.create(this);
        addAction(action);
        return action;
    }

    @Deprecated
    @Override
    public PickerField.OpenAction addOpenAction() {
        PickerField.OpenAction action = PickerField.OpenAction.create(this);
        addAction(action);
        return action;
    }

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }
}
