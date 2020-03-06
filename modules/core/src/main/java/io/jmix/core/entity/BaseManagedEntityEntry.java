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

package io.jmix.core.entity;

import io.jmix.core.metamodel.model.utils.MethodsCache;
import io.jmix.core.metamodel.model.utils.RelatedPropertiesCache;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

public abstract class BaseManagedEntityEntry<K> implements ManagedEntityEntry<K>, Cloneable {
    protected byte state = NEW;
    protected SecurityState securityState = new SecurityState();
    protected transient Collection<WeakReference<EntityPropertyChangeListener>> propertyChangeListeners;
    protected ManagedEntity<K> source;

    public static final int NEW = 1;
    public static final int DETACHED = 2;
    public static final int MANAGED = 4;
    public static final int REMOVED = 8;

    protected static final int PROPERTY_CHANGE_LISTENERS_INITIAL_CAPACITY = 4;

    public BaseManagedEntityEntry(ManagedEntity<K> source) {
        this.source = source;
    }

    @Override
    public ManagedEntity<K> getSource() {
        return source;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttributeValue(String name) {
        return (T) MethodsCache.getOrCreate(getSource().getClass()).getGetterNN(name).apply(getSource());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setAttributeValue(String name, Object value, boolean checkEquals) {
        Object oldValue = getAttributeValue(name);
        if ((!checkEquals) || (!EntityValues.propertyValueEquals(oldValue, value))) {
            BiConsumer setter = MethodsCache.getOrCreate(getSource().getClass()).getSetterNN(name);
            setter.accept(getSource(), value);
        }
    }

    @Override
    public boolean isNew() {
        return (state & NEW) == NEW;
    }

    @Override
    public boolean isManaged() {
        return (state & MANAGED) == MANAGED;
    }

    @Override
    public boolean isDetached() {
        return (state & DETACHED) == DETACHED;
    }

    @Override
    public boolean isRemoved() {
        return (state & REMOVED) == REMOVED;
    }

    @Override
    public void setNew(boolean _new) {
        state = (byte) (_new ? state | NEW : state & ~NEW);
    }

    @Override
    public void setManaged(boolean managed) {
        state = (byte) (managed ? state | MANAGED : state & ~MANAGED);
    }

    @Override
    public void setDetached(boolean detached) {
        state = (byte) (detached ? state | DETACHED : state & ~DETACHED);
    }

    @Override
    public void setRemoved(boolean removed) {
        state = (byte) (removed ? state | REMOVED : state & ~REMOVED);
    }

    @Override
    public SecurityState getSecurityState() {
        return securityState;
    }

    @Override
    public void setSecurityState(SecurityState securityState) {
        this.securityState = securityState;
    }

    @Override
    public void addPropertyChangeListener(EntityPropertyChangeListener listener) {
        if (propertyChangeListeners == null) {
            propertyChangeListeners = new ArrayList<>(PROPERTY_CHANGE_LISTENERS_INITIAL_CAPACITY);
        }
        propertyChangeListeners.add(new WeakReference<>(listener));
    }

    @Override
    public void removePropertyChangeListener(EntityPropertyChangeListener listener) {
        if (propertyChangeListeners != null) {
            for (Iterator<WeakReference<EntityPropertyChangeListener>> it = propertyChangeListeners.iterator(); it.hasNext(); ) {
                EntityPropertyChangeListener iteratorListener = it.next().get();
                if (iteratorListener == null || iteratorListener.equals(listener)) {
                    it.remove();
                }
            }
        }
    }

    public void removeAllListeners() {
        if (propertyChangeListeners != null) {
            propertyChangeListeners.clear();
        }
    }

    public void firePropertyChanged(String propertyName, Object prev, Object curr) {
        if (propertyChangeListeners != null) {

            for (Object referenceObject : propertyChangeListeners.toArray()) {
                @SuppressWarnings("unchecked")
                WeakReference<EntityPropertyChangeListener> reference = (WeakReference<EntityPropertyChangeListener>) referenceObject;

                EntityPropertyChangeListener listener = reference.get();
                if (listener == null) {
                    propertyChangeListeners.remove(reference);
                } else {
                    listener.propertyChanged(new EntityPropertyChangeEvent(getSource(), propertyName, prev, curr));

                    Collection<String> related = RelatedPropertiesCache.getOrCreate(getSource().getClass())
                            .getRelatedReadOnlyProperties(propertyName);
                    if (related != null) {
                        for (String property : related) {
                            listener.propertyChanged(
                                    new EntityPropertyChangeEvent(getSource(), property, null, getAttributeValue(property)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void copy(ManagedEntityEntry<?> entry) {
        if (entry != null) {
            setNew(entry.isNew());
            setDetached(entry.isDetached());
            setManaged(entry.isManaged());
            setRemoved(entry.isRemoved());

            setSecurityState(entry.getSecurityState());
        }
    }
}
