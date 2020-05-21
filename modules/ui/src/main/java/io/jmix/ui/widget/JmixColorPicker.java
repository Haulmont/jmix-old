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

import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.Component;
import io.jmix.ui.widget.client.colorpicker.JmixColorPickerState;

public class JmixColorPicker extends ColorPicker implements Component.Focusable {

    protected String confirmButtonCaption;
    protected String cancelButtonCaption;
    protected String swatchesTabCaption;

    protected String lookupAllCaption;
    protected String lookupRedCaption;
    protected String lookupGreenCaption;
    protected String lookupBlueCaption;

    protected String redSliderCaption;
    protected String greenSliderCaption;
    protected String blueSliderCaption;
    protected String hueSliderCaption;
    protected String saturationSliderCaption;
    protected String valueSliderCaption;

    @Override
    protected void createPopupWindow(Color color) {
        window = new JmixColorPickerPopup(color);
        ((JmixColorPickerPopup) window).setConfirmButtonCaption(confirmButtonCaption);
        ((JmixColorPickerPopup) window).setCancelButtonCaption(cancelButtonCaption);
        ((JmixColorPickerPopup) window).setSwatchesTabCaption(swatchesTabCaption);
        ((JmixColorPickerPopup) window).setLookupAllCaption(lookupAllCaption);
        ((JmixColorPickerPopup) window).setLookupRedCaption(lookupRedCaption);
        ((JmixColorPickerPopup) window).setLookupGreenCaption(lookupGreenCaption);
        ((JmixColorPickerPopup) window).setLookupBlueCaption(lookupBlueCaption);
        window.setRedSliderCaption(redSliderCaption);
        window.setGreenSliderCaption(greenSliderCaption);
        window.setBlueSliderCaption(blueSliderCaption);
        window.setHueSliderCaption(hueSliderCaption);
        window.setSaturationSliderCaption(saturationSliderCaption);
        window.setValueSliderCaption(valueSliderCaption);
        window.setModal(true);
    }

    @Override
    protected ValueChangeListener<Color> createColorValueChangeListener() {
        return event -> {
            setValue(event.getValue(), true);
            rpc.changeColor(event.getValue().getCSS());
        };
    }

    @Override
    protected void showPopup(boolean open) {
        super.showPopup(open);

        if (window != null) {
            window.center();
        }
    }

    @Override
    public void setValue(Color color) {
        if (color == null) {
            color = new Color(0,0,0);
        }
        super.setValue(color);
    }

    public void setWindowCaption(String windowCaption) {
        this.popupCaption = windowCaption;
        if (window != null) {
            window.setCaption(windowCaption);
        }
    }

    public String getWindowCaption() {
        return this.popupCaption;
    }

    public void setConfirmButtonCaption(String confirmButtonCaption) {
        this.confirmButtonCaption = confirmButtonCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setConfirmButtonCaption(confirmButtonCaption);
        }
    }

    public String getConfirmButtonCaption() {
        return this.confirmButtonCaption;
    }

    public void setCancelButtonCaption(String cancelButtonCaption) {
        this.cancelButtonCaption = cancelButtonCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setCancelButtonCaption(cancelButtonCaption);
        }
    }

    public String getCancelButtonCaption() {
        return this.cancelButtonCaption;
    }

    public void setSwatchesTabCaption(String swatchesTabCaption) {
        this.swatchesTabCaption = swatchesTabCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setSwatchesTabCaption(swatchesTabCaption);
        }
    }

    public String getSwatchesTabCaption() {
        return this.swatchesTabCaption;
    }

    public void setLookupAllCaption(String lookupAllCaption) {
        this.lookupAllCaption = lookupAllCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setLookupAllCaption(lookupAllCaption);
        }
    }

    public String getLookupAllCaption() {
        return this.lookupAllCaption;
    }

    public void setLookupRedCaption(String lookupRedCaption) {
        this.lookupRedCaption = lookupRedCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setLookupRedCaption(lookupRedCaption);
        }
    }

    public String getLookupRedCaption() {
        return this.lookupRedCaption;
    }

    public void setLookupGreenCaption(String lookupGreenCaption) {
        this.lookupGreenCaption = lookupGreenCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setLookupGreenCaption(lookupGreenCaption);
        }
    }

    public String getLookupGreenCaption() {
        return this.lookupGreenCaption;
    }

    public void setLookupBlueCaption(String lookupBlueCaption) {
        this.lookupBlueCaption = lookupBlueCaption;
        if (window != null) {
            ((JmixColorPickerPopup) window).setLookupBlueCaption(lookupBlueCaption);
        }
    }

    public String getLookupBlueCaption() {
        return this.lookupBlueCaption;
    }

    public String getBlueSliderCaption() {
        return blueSliderCaption;
    }

    public void setBlueSliderCaption(String blueSliderCaption) {
        this.blueSliderCaption = blueSliderCaption;
        if (window != null) {
            window.setBlueSliderCaption(blueSliderCaption);
        }
    }

    public String getGreenSliderCaption() {
        return greenSliderCaption;
    }

    public void setGreenSliderCaption(String greenSliderCaption) {
        this.greenSliderCaption = greenSliderCaption;
        if (window != null) {
            window.setGreenSliderCaption(greenSliderCaption);
        }
    }

    public String getRedSliderCaption() {
        return redSliderCaption;
    }

    public void setRedSliderCaption(String redSliderCaption) {
        this.redSliderCaption = redSliderCaption;
        if (window != null) {
            window.setRedSliderCaption(redSliderCaption);
        }
    }

    public String getHueSliderCaption() {
        return hueSliderCaption;
    }

    public void setHueSliderCaption(String hueSliderCaption) {
        this.hueSliderCaption = hueSliderCaption;
        if (window != null) {
            window.setHueSliderCaption(hueSliderCaption);
        }
    }

    public String getSaturationSliderCaption() {
        return saturationSliderCaption;
    }

    public void setSaturationSliderCaption(String saturationSliderCaption) {
        this.saturationSliderCaption = saturationSliderCaption;
        if (window != null) {
            window.setSaturationSliderCaption(saturationSliderCaption);
        }
    }

    public String getValueSliderCaption() {
        return valueSliderCaption;
    }

    public void setValueSliderCaption(String valueSliderCaption) {
        this.valueSliderCaption = valueSliderCaption;
        if (window != null) {
            window.setValueSliderCaption(valueSliderCaption);
        }
    }

    @Override
    protected JmixColorPickerState getState() {
        return (JmixColorPickerState) super.getState();
    }

    @Override
    protected JmixColorPickerState getState(boolean markAsDirty) {
        return (JmixColorPickerState) super.getState(markAsDirty);
    }
}