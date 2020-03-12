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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.xml.layout.loaders.GroupTableLoader;
import org.dom4j.Element;

@SuppressWarnings("rawtypes")
public class CubaGroupTableLoader extends GroupTableLoader {

    @Override
    protected TableDataHolder createTableDataHolder() {
        return new CubaGroupTableDataHolder();
    }

    @Override
    protected boolean initDataContainer(TableDataHolder holder) {
        Element rowsElement = element.element("rows");
        if (rowsElement == null) {
            return false;
        }

        CollectionDatasource datasource = DatasourceLoaderHelper.loadTableDatasource(
                element, rowsElement, context, (ComponentLoaderContext) getComponentContext()
        );

        ((CubaGroupTableDataHolder) holder).setDatasource(datasource);
        holder.setMetaClass(datasource.getMetaClass());
        holder.setFetchPlan(datasource.getView());
        return true;
    }

    @Override
    protected boolean setupDataContainer(TableDataHolder holder) {
        CollectionDatasource datasource = ((CubaGroupTableDataHolder) holder).getDatasource();
        if (datasource == null) {
            return false;
        }
        // todo dynamic attributes
        // addDynamicAttributes(resultComponent, metaClass, datasource, null, availableColumns);
        ((GroupTable) resultComponent).setDatasource(datasource);
        return true;
    }

    protected static class CubaGroupTableDataHolder extends TableDataHolder {

        protected CollectionDatasource datasource;

        public CollectionDatasource getDatasource() {
            return datasource;
        }

        public void setDatasource(CollectionDatasource datasource) {
            this.datasource = datasource;
        }
    }
}