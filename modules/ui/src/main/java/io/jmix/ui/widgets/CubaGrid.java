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

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.Editor;
import com.vaadin.ui.components.grid.GridSelectionModel;
import com.vaadin.ui.renderers.AbstractRenderer;
import io.jmix.ui.widgets.client.grid.CubaGridServerRpc;
import io.jmix.ui.widgets.client.grid.CubaGridState;
import io.jmix.ui.widgets.client.grid.CubsGridClientRpc;
import io.jmix.ui.widgets.grid.CubaEditorField;
import io.jmix.ui.widgets.grid.CubaEditorImpl;
import io.jmix.ui.widgets.grid.CubaGridColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CubaGrid<T> extends Grid<T> implements CubaEnhancedGrid<T> {

    protected CubaGridEditorFieldFactory<T> editorFieldFactory;

    protected Runnable emptyStateLinkClickHandler;

    protected boolean aggregatable = false;
    protected AggregationPosition aggregationPosition = AggregationPosition.TOP;
    protected Collection<String> aggregationPropertyIds;

    public CubaGrid() {
        registerRpc((CubaGridServerRpc) () -> {
            if (emptyStateLinkClickHandler != null) {
                emptyStateLinkClickHandler.run();
            }
        });
    }

    @Override
    public void setGridSelectionModel(GridSelectionModel<T> model) {
        setSelectionModel(model);
    }

    @Override
    protected CubaGridState getState() {
        return (CubaGridState) super.getState();
    }

    @Override
    protected CubaGridState getState(boolean markAsDirty) {
        return (CubaGridState) super.getState(markAsDirty);
    }

    @Override
    public Map<String, String> getColumnIds() {
        return getState().columnIds;
    }

    @Override
    public void setColumnIds(Map<String, String> ids) {
        getState().columnIds = ids;
    }

    @Override
    public void addColumnId(String column, String value) {
        if (getState().columnIds == null) {
            getState().columnIds = new HashMap<>();
        }

        getState().columnIds.put(column, value);
    }

    @Override
    public void removeColumnId(String column) {
        if (getState().columnIds != null) {
            getState().columnIds.remove(column);
        }
    }

    @Override
    public void repaint() {
        markAsDirtyRecursive();
        getDataCommunicator().reset();
    }

    @Override
    protected <V, P> Column<T, V> createColumn(ValueProvider<T, V> valueProvider,
                                               ValueProvider<V, P> presentationProvider,
                                               AbstractRenderer<? super T, ? super P> renderer) {
        return new CubaGridColumn<>(valueProvider, presentationProvider, renderer);
    }

    @Override
    public CubaGridEditorFieldFactory<T> getCubaEditorFieldFactory() {
        return editorFieldFactory;
    }

    @Override
    public void setCubaEditorFieldFactory(CubaGridEditorFieldFactory<T> editorFieldFactory) {
        this.editorFieldFactory = editorFieldFactory;
    }

    @Override
    protected Editor<T> createEditor() {
        return new CubaEditorImpl<>(getPropertySet());
    }

    @Override
    public CubaEditorField<?> getColumnEditorField(T bean, Column<T, ?> column) {
        return editorFieldFactory.createField(bean, column);
    }

    @Override
    public void setBeforeRefreshHandler(Consumer<T> beforeRefreshHandler) {
        getDataCommunicator().setBeforeRefreshHandler(beforeRefreshHandler);
    }

    @Override
    public void setShowEmptyState(boolean show) {
        if (getState(false).showEmptyState != show) {
            getState().showEmptyState = show;
        }
    }

    @Override
    public String getEmptyStateMessage() {
        return getState(false).emptyStateMessage;
    }

    @Override
    public void setEmptyStateMessage(String message) {
        getState().emptyStateMessage = message;
    }

    @Override
    public String getEmptyStateLinkMessage() {
        return getState(false).emptyStateLinkMessage;
    }

    @Override
    public void setEmptyStateLinkMessage(String linkMessage) {
        getState().emptyStateLinkMessage = linkMessage;
    }

    @Override
    public void setEmptyStateLinkClickHandler(Runnable handler) {
        this.emptyStateLinkClickHandler = handler;
    }

    @Override
    public void updateFooterVisibility() {
        getRpcProxy(CubsGridClientRpc.class).updateFooterVisibility();
    }

    @Override
    public String getSelectAllLabel() {
        return getState().selectAllLabel;
    }

    @Override
    public void setSelectAllLabel(String selectAllLabel) {
        getState(true).selectAllLabel = selectAllLabel;
    }

    @Override
    public String getDeselectAllLabel() {
        return getState().deselectAllLabel;
    }

    @Override
    public void setDeselectAllLabel(String deselectAllLabel) {
        getState(true).deselectAllLabel = deselectAllLabel;
    }

    @Override
    public boolean isAggregatable() {
        return aggregatable;
    }

    @Override
    public void setAggregatable(boolean aggregatable) {
        this.aggregatable = aggregatable;
    }

    @Override
    public AggregationPosition getAggregationPosition() {
        return aggregationPosition;
    }

    @Override
    public void setAggregationPosition(AggregationPosition position) {
        this.aggregationPosition = position;
    }

    @Override
    public Collection<String> getAggregationPropertyIds() {
        if (aggregationPropertyIds == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(aggregationPropertyIds);
    }

    @Override
    public void addAggregationPropertyId(String propertyId) {
        if (aggregationPropertyIds == null) {
            aggregationPropertyIds = new ArrayList<>();
        } else if (aggregationPropertyIds.contains(propertyId)) {
            throw new IllegalStateException(String.format("Aggregation property %s already exists", propertyId));
        }
        aggregationPropertyIds.add(propertyId);
    }

    @Override
    public void removeAggregationPropertyId(String propertyId) {
        if (aggregationPropertyIds != null) {
            aggregationPropertyIds.remove(propertyId);
            if (aggregationPropertyIds.isEmpty()) {
                aggregationPropertyIds = null;
            }
        }
    }
}
