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

import com.haulmont.cuba.web.widgets.addons.popupbutton.PopupButton;
import io.jmix.ui.components.PopupButton;
import io.jmix.ui.widgets.client.popupbutton.CubaPopupButtonState;

public class CubaPopupButton extends PopupButton {

    @Override
    public CubaPopupButtonState getState() {
        return (CubaPopupButtonState) super.getState();
    }

    @Override
    protected CubaPopupButtonState getState(boolean markAsDirty) {
        return (CubaPopupButtonState) super.getState(markAsDirty);
    }

    public boolean isAutoClose() {
        return getState(false).autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        if (getState(false).autoClose != autoClose) {
            getState().autoClose = autoClose;
        }
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (!(getContent() instanceof CubaPopupButtonLayout) && !getState(false).customLayout) {
            getState().customLayout = true;
        }
    }
}
