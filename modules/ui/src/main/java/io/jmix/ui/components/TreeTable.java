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

import com.google.common.reflect.TypeToken;
import io.jmix.core.entity.Entity;
import io.jmix.ui.components.data.TableItems;

/**
 * TreeTable extends the {@link Table} component so that it can also visualize a hierarchy of its Items in a similar
 * manner that {@link Tree} does. The tree hierarchy is always displayed in the first actual column of the TreeTable.
 *
 * @param <E> row item type
 */
public interface TreeTable<E extends Entity> extends Table<E> {

    String NAME = "treeTable";

    static <T extends Entity> TypeToken<TreeTable<T>> of(@SuppressWarnings("unused") Class<T> itemClass) {
        return new TypeToken<TreeTable<T>>() {};
    }

    void expandAll();
    void expand(Object itemId);

    void collapseAll();
    void collapse(Object itemId);

    /**
     * Expand tree table including specified level
     *
     * @param level level of TreeTable nodes to expand, if passed level = 1 then root items will be expanded
     * @throws IllegalArgumentException if level &lt; 1
     */
    void expandUpTo(int level);

    int getLevel(Object itemId);

    boolean isExpanded(Object itemId);

    /*
    TODO: legacy-ui
    @Override
    @Deprecated
    default HierarchicalDatasource getDatasource() {
        TableItems<E> tableItems = getItems();
        if (tableItems == null) {
            return null;
        }

        if (tableItems instanceof DatasourceTreeTableItems) {
            DatasourceTreeTableItems adapter = (DatasourceTreeTableItems) tableItems;
            return (HierarchicalDatasource) adapter.getDatasource();
        }

        return null;
    }

    @Override
    default void setDatasource(CollectionDatasource datasource) {
        if (datasource == null) {
            setItems(null);
        } else {
            if (!(datasource instanceof HierarchicalDatasource)) {
                throw new IllegalArgumentException("TreeTable supports only HierarchicalDatasource");
            }

            setItems(new DatasourceTreeTableItems((HierarchicalDatasource) datasource));
        }
    }

    @Deprecated
    default void setDatasource(HierarchicalDatasource datasource) {
        setDatasource((CollectionDatasource) datasource);
    }*/
}