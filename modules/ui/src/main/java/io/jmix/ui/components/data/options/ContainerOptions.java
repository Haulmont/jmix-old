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

package io.jmix.ui.components.data.options;

import io.jmix.core.commons.events.EventHub;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.components.data.BindingState;
import io.jmix.ui.components.data.meta.ContainerDataUnit;
import io.jmix.ui.components.data.meta.EntityOptions;
import io.jmix.ui.components.data.Options;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Options based on a data container.
 *
 * @param <E> entity type
 * @param <K> entity key type
 */
public class ContainerOptions<E extends Entity<K>, K> implements Options<E>, EntityOptions<E>, ContainerDataUnit<E> {

    protected CollectionContainer<E> container;
    protected DataLoader loader;

    protected EventHub events = new EventHub();

    protected E deferredSelectedItem;

    public ContainerOptions(CollectionContainer<E> container) {
        this.container = container;
        if (container instanceof HasLoader) {
            this.loader = ((HasLoader) container).getLoader();
        }

        this.container.addCollectionChangeListener(this::containerCollectionChanged);
        this.container.addItemPropertyChangeListener(this::containerItemPropertyChanged);
    }

    protected void containerCollectionChanged(@SuppressWarnings("unused") CollectionContainer.CollectionChangeEvent<E> e) {
        if (deferredSelectedItem != null) {
            // UI components (e.g. LookupField) can have value that does not exist in container
            if (container.containsItem(deferredSelectedItem)) {
                container.setItem(deferredSelectedItem);
            }
            deferredSelectedItem = null;
        }
        events.publish(OptionsChangeEvent.class, new OptionsChangeEvent<>(this));
    }

    @SuppressWarnings("unchecked")
    protected void containerItemPropertyChanged(@SuppressWarnings("unused") CollectionContainer.ItemPropertyChangeEvent<E> e) {
        events.publish(OptionsChangeEvent.class, new OptionsChangeEvent(this));
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public void setSelectedItem(E item) {
        if (item == null) {
            container.setItem(null);
        } else {
            if (container.getItems().size() > 0) {
                // UI components (e.g. LookupField) can have value that does not exist in container
                if (container.containsItem(item)) {
                    container.setItem(item);
                }
            } else {
                this.deferredSelectedItem = item;
            }
        }
    }

    @Override
    public boolean containsItem(E item) {
        return item != null && container.containsItem(EntityValues.<K>getEntityId(item));
    }

    @Override
    public void updateItem(E item) {
        container.replaceItem(item);
    }

    @Override
    public void refresh() {
        if (loader != null) {
            loader.load();
        }
    }

    @Override
    public Stream<E> getOptions() {
        return container.getItems().stream();
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
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
    public Subscription addOptionsChangeListener(Consumer<OptionsChangeEvent<E>> listener) {
        return events.subscribe(OptionsChangeEvent.class, (Consumer) listener);
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }
}
