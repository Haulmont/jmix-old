/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.web.datacontext

import com.haulmont.cuba.core.model.Foo
import io.jmix.ui.component.Table
import io.jmix.ui.component.TextField
import io.jmix.ui.component.data.table.ContainerTableItems
import io.jmix.ui.component.data.value.ContainerValueSource
import io.jmix.ui.model.CollectionContainer
import io.jmix.ui.model.InstanceContainer
import spec.haulmont.cuba.web.UiScreenSpec

class ContainerBindingTest extends UiScreenSpec {

    def "fields with one instance container"() {
        InstanceContainer<Foo> container = dataComponents.createInstanceContainer(Foo)

        TextField field1 = uiComponents.create(TextField)
        field1.setValueSource(new ContainerValueSource(container, 'name'))

        TextField field2 = uiComponents.create(TextField)
        field2.setValueSource(new ContainerValueSource(container, 'name'))

        def foo = new Foo(name: 'foo1')

        when:

        container.setItem(foo)

        then:

        field1.getValue() == 'foo1'
        field2.getValue() == 'foo1'

        when:

        field1.setValue('changed')

        then:

        field2.getValue() == 'changed'
        foo.name == 'changed'
    }

    def "field and table with collection container"() throws Exception {

        CollectionContainer<Foo> container = dataComponents.createCollectionContainer(Foo)

        Table<Foo> table = uiComponents.create(Table)
        table.addColumn(new Table.Column(metadata.getClassNN(Foo).getPropertyPath("name")))
        table.setItems(new ContainerTableItems(container))

        TextField textField = uiComponents.create(TextField)
        textField.setValueSource(new ContainerValueSource(container, 'name'))

        Foo foo1 = new Foo(name: 'foo1')
        Foo foo2 = new Foo(name: 'foo2')

        container.items = [foo1, foo2]

        when:

        table.setSelected(foo1)

        then:

        table.getSingleSelected() == foo1
        container.getItem() == foo1
        textField.getValue() == 'foo1'

        when:

        table.setSelected(foo2)

        then:

        table.getSingleSelected() == foo2
        container.getItem() == foo2
        textField.getValue() == 'foo2'
    }
}
