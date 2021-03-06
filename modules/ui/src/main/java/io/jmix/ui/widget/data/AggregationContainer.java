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
package io.jmix.ui.widget.data;

import com.vaadin.v7.data.Container;

import java.util.Collection;
import java.util.Map;

public interface AggregationContainer extends Container {

    enum Type {
        SUM,
        AVG,
        COUNT,
        MIN,
        MAX,
        CUSTOM
    }

    Collection getAggregationPropertyIds();

    void addContainerPropertyAggregation(Object propertyId, Type type);
    void removeContainerPropertyAggregation(Object propertyId);

    /**
     * Perform aggregation and return map with formatted string values.
     *
     * @param context aggregation context
     * @return map with aggregation info and formatted string values
     */
    Map<Object, Object> aggregate(Context context);

    /**
     * Perform aggregation and return map with aggregation info and aggregation column type, i.e. if aggregation was
     * performed for Long type column it will return pair: AggregationInfo - Long.
     *
     * @param context aggregation context
     * @return map with aggregation info and aggregation column type
     */
    Map<Object, Object> aggregateValues(Context context);

    class Context {
        private Collection itemIds;

        public Context(Collection itemIds) {
            this.itemIds = itemIds;
        }

        public Collection getItemIds() {
            return itemIds;
        }
    }
}