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

package io.jmix.ui.web.datacontext

import io.jmix.core.app.DataService
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.ui.model.CollectionContainer
import io.jmix.ui.model.CollectionLoader
import io.jmix.ui.model.DataComponents
import com.haulmont.cuba.web.container.CubaTestContainer
import com.haulmont.cuba.web.testmodel.datacontext.Foo
import com.haulmont.cuba.web.testsupport.TestContainer
import com.haulmont.cuba.web.testsupport.proxy.TestServiceProxy
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Consumer

class CollectionLoaderTest extends Specification {

    @Shared @ClassRule
    public TestContainer cont = CubaTestContainer.Common.INSTANCE

    private Metadata metadata
    private DataManager dataManager
    private DataComponents factory

    void setup() {
        metadata = cont.getBean(Metadata)
        dataManager = cont.getBean(DataManager)
        factory = cont.getBean(DataComponents)
    }

    void cleanup() {
        TestServiceProxy.clear()
    }

    def "successful load"() {
        CollectionLoader<Foo> loader = factory.createCollectionLoader()
        CollectionContainer<Foo> container = factory.createCollectionContainer(Foo)

        Consumer preLoadListener = Mock()
        loader.addPreLoadListener(preLoadListener)

        Consumer postLoadListener = Mock()
        loader.addPostLoadListener(postLoadListener)

        Foo foo = new Foo()

        TestServiceProxy.mock(DataService, Mock(DataService) {
            loadList(_) >> [com.haulmont.cuba.client.testsupport.TestSupport.reserialize(foo)]
        })

        when:

        loader.setContainer(container)
        loader.setQuery('select bla-bla')
        loader.load()

        then:

        container.getItems() == [foo]

        1 * preLoadListener.accept(_)
        1 * postLoadListener.accept(_)
    }

    def "prevent load by PreLoadEvent"() {
        CollectionLoader<Foo> loader = factory.createCollectionLoader()
        CollectionContainer<Foo> container = factory.createCollectionContainer(Foo)

        Consumer preLoadListener = { CollectionLoader.PreLoadEvent e -> e.preventLoad() }
        loader.addPreLoadListener(preLoadListener)

        Consumer postLoadListener = Mock()
        loader.addPostLoadListener(postLoadListener)

        when:

        loader.setContainer(container)
        loader.setQuery('select bla-bla')
        loader.load()

        then:

        container.getItems() == []

        0 * postLoadListener.accept(_)
    }
}
