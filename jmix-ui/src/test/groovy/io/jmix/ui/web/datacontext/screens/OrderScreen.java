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

package io.jmix.ui.web.datacontext.screens;

import io.jmix.core.Metadata;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.builders.EditorClassBuilder;
import io.jmix.ui.components.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import com.haulmont.cuba.web.testmodel.sales.Order;
import com.haulmont.cuba.web.testmodel.sales.OrderLine;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UiController
@UiDescriptor("order-screen.xml")
public class OrderScreen extends Screen {

    private static final Logger log = LoggerFactory.getLogger(OrderScreen.class);

    @Inject
    private DataComponents dataComponents;

    @Inject
    private DataContext dataContext;

    @Inject
    private InstanceContainer<Order> orderDc;

    @Inject
    private CollectionContainer<OrderLine> linesDc;

    @Inject
    private ScreenBuilders screenBuilders;

    @Inject
    private Metadata metadata;

    @Inject
    private Table<OrderLine> itemsTable;

    private Order order;

    public void setOrder(Order order) {
        this.order = order;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
//        getScreenData().setDataContext(dataComponents.createDataContext());
        log.debug("onInit: dataContext={}", dataContext);
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        Order mergedOrder = dataContext.merge(order);
        orderDc.setItem(mergedOrder);
    }

    public LineScreen buildLineScreenForCreate(boolean explicitParentDc) {
        EditorClassBuilder<OrderLine, LineScreen> builder = screenBuilders.editor(itemsTable)
                .withScreenClass(LineScreen.class)
                .newEntity();
        if (explicitParentDc) {
            builder.withParentDataContext(getScreenData().getDataContext());
        }
        return builder.build();
    }
        
    public LineScreen buildLineScreenForEdit(boolean explicitParentDc) {
        itemsTable.setSelected(linesDc.getItems().get(0));
        EditorClassBuilder<OrderLine, LineScreen> builder = screenBuilders.editor(itemsTable)
                .withScreenClass(LineScreen.class);
        if (explicitParentDc) {
            builder.withParentDataContext(getScreenData().getDataContext());
        }
        return builder.build();
    }

}
