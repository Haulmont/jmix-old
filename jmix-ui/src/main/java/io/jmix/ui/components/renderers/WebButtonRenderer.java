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

package io.jmix.ui.components.renderers;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.DataGrid;
import com.vaadin.ui.renderers.ButtonRenderer;

import java.util.function.Consumer;

/**
 * A Renderer that displays a button with a textual caption. The value of the
 * corresponding property is used as the caption. Click listeners can be added
 * to the renderer, invoked when any of the rendered buttons is clicked.
 */
public class WebButtonRenderer<T extends Entity>
        extends WebAbstractClickableRenderer<T, String>
        implements DataGrid.ButtonRenderer<T> {

    public WebButtonRenderer() {
        this("");
    }

    public WebButtonRenderer(String nullRepresentation) {
        this(null, nullRepresentation);
    }

    public WebButtonRenderer(Consumer<DataGrid.RendererClickEvent<T>> listener) {
        this(listener, "");
    }

    public WebButtonRenderer(Consumer<DataGrid.RendererClickEvent<T>> listener, String nullRepresentation) {
        super(listener);
        this.nullRepresentation = nullRepresentation;
    }

    @Override
    public ButtonRenderer<T> getImplementation() {
        return (ButtonRenderer<T>) super.getImplementation();
    }

    @Override
    protected ButtonRenderer<T> createImplementation() {
        if (listener != null) {
            return new ButtonRenderer<>(createClickListenerWrapper(listener), getNullRepresentation());
        } else {
            return new ButtonRenderer<>(getNullRepresentation());
        }
    }

    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    public void setNullRepresentation(String nullRepresentation) {
        super.setNullRepresentation(nullRepresentation);
    }
}
