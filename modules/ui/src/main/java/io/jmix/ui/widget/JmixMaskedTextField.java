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

import io.jmix.ui.widget.client.textfield.JmixMaskedTextFieldState;

public class JmixMaskedTextField extends JmixTextField {

    public JmixMaskedTextField() {
    }

    public boolean isMaskedMode() {
       return getState(false).maskedMode;
    }

    public void setMaskedMode(boolean maskedMode) {
        getState(true).maskedMode = maskedMode;
    }

    public boolean isSendNullRepresentation() {
        return getState(false).sendNullRepresentation;
    }

    public void setSendNullRepresentation(boolean sendNullRepresentation) {
        getState(true).sendNullRepresentation = sendNullRepresentation;
    }

    @Override
    protected JmixMaskedTextFieldState getState() {
        return (JmixMaskedTextFieldState) super.getState();
    }

    @Override
    protected JmixMaskedTextFieldState getState(boolean markAsDirty) {
        return (JmixMaskedTextFieldState) super.getState(markAsDirty);
    }

    public void setMask(String mask) {
        getState(true).mask = mask;
    }

    public String getMask(){
        return getState(false).mask;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (readOnly == isReadOnly())
            return;
        super.setReadOnly(readOnly);
    }
}