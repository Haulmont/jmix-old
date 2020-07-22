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

package test_support.app.entity.model_objects;

import io.jmix.core.EntityEntry;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.ModelObjectEntityEntry;
import io.jmix.core.metamodel.annotation.ModelObject;

@ModelObject(name = "test_OrderLineObject")
public class OrderLineObject implements JmixEntity {

    private OrderObject order;

    private String product;

    private Double quantity;

    public OrderObject getOrder() {
        return order;
    }

    public void setOrder(OrderObject order) {
        this.order = order;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    // TODO Replace with enhancing - begin

    private EntityEntry _jmixEntityEntry = new ModelObjectEntityEntry(this);

    @Override
    public EntityEntry __getEntityEntry() {
        return _jmixEntityEntry;
    }

    @Override
    public void __copyEntityEntry() {
        ModelObjectEntityEntry newEntry = new ModelObjectEntityEntry(this);
        newEntry.copy(_jmixEntityEntry);
        _jmixEntityEntry = newEntry;
    }

    // TODO Replace with enhancing - end
}
