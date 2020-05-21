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

package io.jmix.ui.component.datagrid;

import io.jmix.ui.component.data.DataGridItems;

/**
 * {@link DataGridDataProvider} delegates event handling to an implementation of this interface.
 *
 * @param <I> items type
 */
public interface DataGridItemsEventsDelegate<I> {

    void dataGridSourceItemSetChanged(DataGridItems.ItemSetChangeEvent<I> event);

    void dataGridSourcePropertyValueChanged(DataGridItems.ValueChangeEvent<I> event);

    void dataGridSourceStateChanged(DataGridItems.StateChangeEvent event);

    void dataGridSourceSelectedItemChanged(DataGridItems.SelectedItemChangeEvent<I> event);
}
