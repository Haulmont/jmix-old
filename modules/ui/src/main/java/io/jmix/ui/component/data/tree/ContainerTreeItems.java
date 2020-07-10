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

package io.jmix.ui.component.data.tree;

import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityTreeItems;
import io.jmix.ui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ContainerTreeItems<E extends JmixEntity> implements EntityTreeItems<E>, ContainerDataUnit<E> {

    protected final CollectionContainer<E> container;

    protected final String hierarchyProperty;

    protected EventHub events = new EventHub();

    public ContainerTreeItems(CollectionContainer<E> container, String hierarchyProperty) {
        this.container = container;
        this.hierarchyProperty = hierarchyProperty;
        this.container.addItemChangeListener(this::containerItemChanged);
        this.container.addCollectionChangeListener(this::containerCollectionChanged);
        this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);
    }

    protected void containerItemChanged(CollectionContainer.ItemChangeEvent<E> event) {
        events.publish(SelectedItemChangeEvent.class, new SelectedItemChangeEvent<>(this, event.getItem()));
    }

    protected void containerCollectionChanged(@SuppressWarnings("unused") CollectionContainer.CollectionChangeEvent<E> e) {
        events.publish(ItemSetChangeEvent.class, new ItemSetChangeEvent<>(this));
    }

    @SuppressWarnings("unchecked")
    protected void containerItemPropertyChanged(CollectionContainer.ItemPropertyChangeEvent<E> e) {
        events.publish(ValueChangeEvent.class, new ValueChangeEvent(this,
                e.getItem(), e.getProperty(), e.getPrevValue(), e.getValue()));
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Nullable
    @Override
    public E getSelectedItem() {
        return container.getItemOrNull();
    }

    @Override
    public void setSelectedItem(@Nullable E item) {
        container.setItem(item);
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Object getItemId(E item) {
        return EntityValues.getId(item);
    }

    @Override
    public E getItem(@Nullable Object itemId) {
        return itemId == null ? null : container.getItemOrNull(itemId);
    }

    @Override
    public Stream<E> getItems() {
        return container.getItems().stream();
    }

    @Override
    public boolean containsItem(E item) {
        return container.getItemOrNull(EntityValues.getId(item)) != null;
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Override
    public int getChildCount(E parent) {
        return Math.toIntExact(getChildren(parent).count());
    }

    @Override
    public Stream<E> getChildren(E item) {
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
        Preconditions.checkNotNullArgument(item, "item is null");
        return EntityValues.getValue(item, hierarchyProperty);
    }

    @Override
    public String getHierarchyPropertyName() {
        return hierarchyProperty;
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent> listener) {
        return events.subscribe(StateChangeEvent.class, listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<E>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemSetChangeListener(Consumer<ItemSetChangeEvent<E>> listener) {
        return events.subscribe(ItemSetChangeEvent.class, (Consumer) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addSelectedItemChangeListener(Consumer<SelectedItemChangeEvent<E>> listener) {
        return events.subscribe(SelectedItemChangeEvent.class, (Consumer) listener);
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }
}
