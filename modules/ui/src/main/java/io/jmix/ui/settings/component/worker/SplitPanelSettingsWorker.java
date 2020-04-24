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

package io.jmix.ui.settings.component.worker;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractSplitPanel;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.SplitPanel;
import io.jmix.ui.components.impl.WebSplitPanel;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.SplitPanelSettings;
import org.apache.commons.lang3.math.NumberUtils;

@org.springframework.stereotype.Component(SplitPanelSettingsWorker.NAME)
public class SplitPanelSettingsWorker implements ComponentSettingsWorker {

    public static final String NAME = "jmix_SplitPanelSettingsWorker";

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebSplitPanel.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return SplitPanelSettings.class;
    }

    @Override
    public void applySettings(Component component, SettingsWrapper wrapper) {
        SplitPanel splitPanel = (SplitPanel) component;
        SplitPanelSettings settings = wrapper.getSettings();

        if (settings.getPositionUnit() != null
                && settings.getPositionValue() != null) {
            Float value = settings.getPositionValue();
            String unit = settings.getPositionUnit();

            Sizeable.Unit convertedUnit;
            if (NumberUtils.isNumber(unit)) {
                convertedUnit = convertLegacyUnit(Integer.parseInt(unit));
            } else {
                convertedUnit = Sizeable.Unit.getUnitFromSymbol(unit);
            }

            AbstractSplitPanel vSplitPanel = getVaadinSplitPanel(splitPanel);
            vSplitPanel.setSplitPosition(value, convertedUnit, vSplitPanel.isSplitPositionReversed());
        }
    }

    @Override
    public void applyDataLoadingSettings(Component component, SettingsWrapper wrapper) {
        // does not have data loading settings
    }

    @Override
    public boolean saveSettings(Component component, SettingsWrapper wrapper) {
        SplitPanel splitPanel = (SplitPanel) component;
        SplitPanelSettings settings = wrapper.getSettings();

        if (isSettingsChanged(splitPanel, settings)) {
            AbstractSplitPanel vSplitPanel = getVaadinSplitPanel(splitPanel);
            settings.setPositionValue(vSplitPanel.getSplitPosition());
            settings.setPositionUnit(vSplitPanel.getSplitPositionUnit().getSymbol());

            return true;
        }
        return false;
    }

    @Override
    public ComponentSettings getSettings(Component component) {
        SplitPanel splitPanel = (SplitPanel) component;
        SplitPanelSettings settings = createSettings();

        AbstractSplitPanel vSplitPanel = getVaadinSplitPanel(splitPanel);

        settings.setPositionValue(vSplitPanel.getSplitPosition());
        settings.setPositionUnit(vSplitPanel.getSplitPositionUnit().getSymbol());

        return settings;
    }

    protected AbstractSplitPanel getVaadinSplitPanel(SplitPanel splitPanel) {
        return splitPanel.unwrap(AbstractSplitPanel.class);
    }

    protected boolean isSettingsChanged(SplitPanel splitPanel, SplitPanelSettings settings) {
        Float value = settings.getPositionValue();
        String settingsUnit = settings.getPositionUnit();
        if (value == null || settingsUnit == null) {
            return true;
        }

        if (!value.equals(splitPanel.getSplitPosition())) {
            return true;
        }

        Sizeable.Unit convertedUnit = NumberUtils.isNumber(settingsUnit) ?
                convertLegacyUnit(Integer.parseInt(settingsUnit)) :
                Sizeable.Unit.getUnitFromSymbol(settingsUnit);

        Sizeable.Unit unit = getVaadinSplitPanel(splitPanel).getSplitPositionUnit();

        return convertedUnit != unit;
    }

    protected Sizeable.Unit convertLegacyUnit(int unit) {
        switch (unit) {
            case 0:
                return Sizeable.Unit.PIXELS;
            case 8:
                return Sizeable.Unit.PERCENTAGE;
            default:
                return Sizeable.Unit.PIXELS;
        }
    }

    protected SplitPanelSettings createSettings() {
        return new SplitPanelSettings();
    }
}
