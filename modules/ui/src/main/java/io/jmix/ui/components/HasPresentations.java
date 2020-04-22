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

package io.jmix.ui.components;

import io.jmix.ui.presentations.Presentations;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;

import javax.annotation.Nullable;

/**
 * Component having presentations.
 */
public interface HasPresentations extends HasSettings {
    void usePresentations(boolean b);
    boolean isUsePresentations();

    void resetPresentation();
    void loadPresentations();

    Presentations getPresentations();

    void applyPresentation(Object id);
    void applyPresentationAsDefault(Object id);

    Object getDefaultPresentationId();

    void setDefaultSettings(SettingsWrapper wrapper);

    @Nullable
    ComponentSettings getDefaultSettings();
}