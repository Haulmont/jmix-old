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

package io.jmix.data.impl.lazyloading;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.JmixEntity;
import io.jmix.core.LoadContext;
import io.jmix.core.impl.StandardSerialization;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.springframework.beans.factory.BeanFactory;

import java.io.IOException;

public class JmixCollectionValueHolder extends JmixAbstractValueHolder {
    protected Class ownerClass;
    protected Object entityId;
    protected String propertyName;

    protected transient DataManager dataManager;
    protected transient FetchPlanBuilder fetchPlanBuilder;

    public JmixCollectionValueHolder(String propertyName, Class ownerClass, Object entityId, DataManager dataManager,
                                     FetchPlanBuilder fetchPlanBuilder) {
        this.propertyName = propertyName;
        this.ownerClass = ownerClass;
        this.entityId = entityId;
        this.dataManager = dataManager;
        this.fetchPlanBuilder = fetchPlanBuilder;
    }

    @Override
    public Object getValue() {
        if (!isInstantiated) {
            synchronized (this) {
                LoadContext lc = new LoadContext(ownerClass);
                lc.setFetchPlan(fetchPlanBuilder.add(propertyName).build());
                lc.setId(entityId);
                JmixEntity result = dataManager.load(lc);
                this.value = ((IndirectCollection) result.__getEntityEntry().getAttributeValue(propertyName))
                        .getValueHolder()
                        .getValue();
                isInstantiated = true;
            }
        }
        return value;
    }

    @Override
    public Object clone() {
        return new JmixCollectionValueHolder(propertyName, ownerClass, entityId, dataManager, fetchPlanBuilder);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        BeanFactory beanFactory = StandardSerialization.getThreadLocalBeanFactory();
        dataManager = (DataManager) beanFactory.getBean(DataManager.NAME);
        fetchPlanBuilder = beanFactory.getBean(FetchPlanBuilder.class, ownerClass);
    }
}
