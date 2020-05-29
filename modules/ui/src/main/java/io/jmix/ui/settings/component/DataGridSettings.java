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

package io.jmix.ui.settings.component;

import io.jmix.ui.component.DataGrid;

import java.io.Serializable;
import java.util.List;

public class DataGridSettings implements ComponentSettings {

    protected String id;

    protected String sortColumnId;
    protected DataGrid.SortDirection sortDirection;

    protected List<ColumnSettings> columns;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getSortColumnId() {
        return sortColumnId;
    }

    public void setSortColumnId(String sortColumnId) {
        this.sortColumnId = sortColumnId;
    }

    public DataGrid.SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(DataGrid.SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public List<ColumnSettings> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnSettings> columns) {
        this.columns = columns;
    }

    public static class ColumnSettings implements Serializable {

        protected String id;
        protected Double width;
        protected Boolean collapsed;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getWidth() {
            return width;
        }

        public void setWidth(Double width) {
            this.width = width;
        }

        public Boolean getCollapsed() {
            return collapsed;
        }

        public void setCollapsed(Boolean collapsed) {
            this.collapsed = collapsed;
        }
    }
}
