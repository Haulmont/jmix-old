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
package io.jmix.ui.presentations;

import io.jmix.ui.presentations.model.PresentationStub;
import io.jmix.ui.presentations.model.Presentation;
import org.dom4j.Element;

import java.util.Collection;

/**
 * Provide the workflow with presentations (visual settings of a component).
 * <br><br> A component must implement {@link io.jmix.ui.components.HasPresentations} interface
 *
 * @see Presentation
 */
public interface Presentations {

    String NAME = "jmix_Presentations";

    /**
     * Returns the current active presentation or <code>null</code> if a current presentation didn't set
     */
    Presentation getCurrent();

    /**
     * Sets current active presentation for a component
     */
    void setCurrent(Presentation p);

    /**
     * Returns user settings for the selected presentation or <code>null</code>
     * if the presentation doesn't exist or if the presentation doesn't contain any settings
     */
    Element getSettings(Presentation p);

    /**
     * Sets user settings for the selected presentation
     */
    void setSettings(Presentation p, Element e);

    /**
     * Returns presentation by its id or <code>null</code> if a presentation doesn't exist
     */
    Presentation getPresentation(Object id);

    /**
     * Returns presentation caption by its id
     */
    String getCaption(Object id);

    /**
     * Returns a collection of the component presentations
     */
    Collection<Object> getPresentationIds();

    /**
     * Returns a default presentation or <code>null</code> if it didn't set
     */
    Presentation getDefault();

    /**
     * Sets a default presentation
     */
    void setDefault(Presentation p);

    /**
     * Adds a new presentation
     */
    void add(Presentation p);

    /**
     * Removes a presentation from the list of available presentations
     */
    void remove(Presentation p);

    /**
     * Modifies the selected presentation
     */
    void modify(Presentation p);

    /**
     * Returns <code>true</code> if the selected presentation has an <code>autoSave</code> settings else returns <code>false</code>
     */
    boolean isAutoSave(Presentation p);

    /**
     * Returns <code>true</code> if the selected presentation is marked as global else returns <code>false</code>
     */
    boolean isGlobal(Presentation p);

    /**
     * Commits all changes into the database
     */
    void commit();

    /**
     * Returns a presentation by its name with ignored case.
     * It returns <code>null</code> if a presentation with such name doesn't exist
     */
    Presentation getPresentationByName(String name);

    /**
     * Adds listener
     */
    void addListener(PresentationsChangeListener listener);

    /**
     * Removes listener
     */
    void removeListener(PresentationsChangeListener listener);

    /**
     * @return Presentation instance or stub if ui-persistence add-on is not added to the project
     */
    default Presentation create() {
        return new PresentationStub();
    }

    /**
     * @return true if ui-persistence add-on is added to the project
     */
    default boolean isPresentationsAvailable() {
        return false;
    }
}
