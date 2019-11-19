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

import com.haulmont.cuba.client.testsupport.TestSupport
import io.jmix.ui.model.DataComponents
import com.haulmont.cuba.web.container.CubaTestContainer
import com.haulmont.cuba.web.testmodel.datacontext.TestIdentityIdEntity
import com.haulmont.cuba.web.testsupport.TestContainer
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class DataContextIdProxyTest extends Specification {

    @Shared @ClassRule
    public TestContainer cont = CubaTestContainer.Common.INSTANCE

    private DataComponents factory

    void setup() {
        factory = cont.getBean(DataComponents)
    }

    @SuppressWarnings("GroovyAccessibility")
    def "identity-id entity merged after assigning id generated by database"() {
        def entity = new TestIdentityIdEntity(name: 'aaa')

        def dataContext = factory.createDataContext()

        def merged = dataContext.merge(entity)

        when:

        TestIdentityIdEntity serverSideEntity = TestSupport.reserialize(merged)
        serverSideEntity.setDbGeneratedId(10)
        TestIdentityIdEntity returnedEntity = TestSupport.reserialize(serverSideEntity)

        TestIdentityIdEntity mergedReturnedEntity = dataContext.merge(returnedEntity)

        then:

        mergedReturnedEntity.getId().get() == 10
    }
}
