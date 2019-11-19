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

package io.jmix.ui.web.components.optionsgroup.screens;

import io.jmix.core.Metadata;
import io.jmix.ui.components.OptionsGroup;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstancePropertyContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import com.haulmont.cuba.web.testmodel.sales.Catalog;
import com.haulmont.cuba.web.testmodel.sales.Order;
import com.haulmont.cuba.web.testmodel.sales.OrderLine;
import com.haulmont.cuba.web.testmodel.sales.Product;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UiController()
@UiDescriptor("options-group-test-screen.xml")
public class OptionsGroupTestScreen extends Screen {

    @Inject
    private Metadata metadata;

    @Inject
    public InstanceContainer<Order> orderDc;
    @Inject
    public CollectionContainer<OrderLine> orderLinesDc;
    @Inject
    public CollectionContainer<OrderLine> allOrderLinesDc;

    @Inject
    public InstanceContainer<OrderLine> orderLineDc;
    @Inject
    public InstancePropertyContainer<Product> productDc;
    @Inject
    public CollectionContainer<Product> allProductsDc;

    @Inject
    public InstanceContainer<Catalog> catalogDc;

    @Inject
    public OptionsGroup<OrderLine, OrderLine> optionsGroup;
    @Inject
    public OptionsGroup<Product, Product> singleOptionGroup;
    @Inject
    public OptionsGroup<Set<Product>, Product> setOptionsGroup;

    @Subscribe
    private void onInit(InitEvent event) {
        Order order = metadata.create(Order.class);
        orderDc.setItem(order);

        List<OrderLine> orderLines = IntStream.range(0, 5)
                .mapToObj(i -> metadata.create(OrderLine.class))
                .collect(Collectors.toList());
        allOrderLinesDc.getMutableItems().addAll(orderLines);

        OrderLine orderLine = metadata.create(OrderLine.class);
        orderLineDc.setItem(orderLine);

        List<Product> products = IntStream.range(0, 5)
                .mapToObj(i -> metadata.create(Product.class))
                .collect(Collectors.toList());
        allProductsDc.getMutableItems().addAll(products);

        Catalog catalog = metadata.create(Catalog.class);
        catalogDc.setItem(catalog);
    }
}
