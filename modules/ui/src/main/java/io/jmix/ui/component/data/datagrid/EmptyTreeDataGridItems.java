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

import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.TreeDataGridItems;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class EmptyTreeDataGridItems<E extends JmixEntity> extends EmptyDataGridItems<E> implements TreeDataGridItems<E> {

    public EmptyTreeDataGridItems(MetaClass metaClass) {
        super(metaClass);
    }

    @Override
    public int getChildCount(E parent) {
        return 0;
    }

    @Override
    public Stream<E> getChildren(@Nullable E item) {
        return Stream.empty();
    }

    @Override
    public boolean hasChildren(E item) {
        return false;
    }

    @Nullable
    @Override
    public E getParent(E item) {
        return null;
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        // do nothing
    }

    @Override
    public void resetSortOrder() {
        // do nothing
    }
}
