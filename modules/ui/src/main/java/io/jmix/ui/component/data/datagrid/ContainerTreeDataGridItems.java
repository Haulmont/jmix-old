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

package io.jmix.ui.component.data.datagrid;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.data.TreeDataGridItems;
import io.jmix.ui.gui.data.impl.AggregatableDelegate;
import io.jmix.ui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class ContainerTreeDataGridItems<E extends JmixEntity>
            extends ContainerDataGridItems<E>
            implements TreeDataGridItems<E> {

    private final String hierarchyProperty;

    public ContainerTreeDataGridItems(CollectionContainer<E> container, String hierarchyProperty, AggregatableDelegate aggregatableDelegate) {
        super(container, aggregatableDelegate);
        this.hierarchyProperty = hierarchyProperty;
    }

    @Override
    public int getChildCount(E parent) {
        return Math.toIntExact(getChildren(parent).count());
    }

    @Override
    public Stream<E> getChildren(@Nullable E item) {
        if (item == null) {
            // root items
            return container.getItems().stream()
                    .filter(it -> {
                        E parentItem = EntityValues.getValue(it, hierarchyProperty);
                        return parentItem == null || (container.getItemOrNull(EntityValues.getId(parentItem)) == null);
                    });
        } else {
            return container.getItems().stream()
                    .filter(it -> {
                        E parentItem = EntityValues.getValue(it, hierarchyProperty);
                        return parentItem != null && parentItem.equals(item);
                    });
        }
    }

    @Override
    public boolean hasChildren(E item) {
        return container.getItems().stream().anyMatch(it -> {
            E parentItem = EntityValues.getValue(it, hierarchyProperty);
            return parentItem != null && parentItem.equals(item);
        });
    }

    @Nullable
    @Override
    public E getParent(E item) {
        Preconditions.checkNotNullArgument(item);
        return EntityValues.getValue(item, hierarchyProperty);
    }
}
