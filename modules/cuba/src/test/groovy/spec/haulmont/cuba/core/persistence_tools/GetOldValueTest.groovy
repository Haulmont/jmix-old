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

package spec.haulmont.cuba.core.persistence_tools

import com.haulmont.cuba.core.model.sales.Customer
import com.haulmont.cuba.core.model.sales.Order
import com.haulmont.cuba.core.model.sales.OrderLine
import com.haulmont.cuba.core.model.sales.Status
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.Metadata
import io.jmix.core.FetchPlan
import com.haulmont.cuba.core.Persistence
import io.jmix.data.PersistenceTools
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired


class GetOldValueTest extends CoreTestSpecification {
    @Autowired
    private PersistenceTools persistenceTools
    @Autowired
    private Persistence persistence
    @Autowired
    private Metadata metadata
    @Autowired
    private TestSupport testSupport

    private Customer customer1
    private Order order1
    private OrderLine orderLine1

    void setup() {
        persistence.runInTransaction({ em ->
            customer1 = metadata.create(Customer)
            customer1.name = 'a customer'
            em.persist(customer1)

            order1 = metadata.create(Order)
            order1.setNumber('1')
            order1.setCustomer(customer1)
            em.persist(order1)

            orderLine1 = metadata.create(OrderLine)
            orderLine1.productName = "prod1"
            orderLine1.quantity = 10
            orderLine1.order = order1
            em.persist(orderLine1)
        })
    }

    void cleanup() {
        def jdbcTemplate = new JdbcTemplate(persistence.dataSource)
        jdbcTemplate.update('delete from TEST_ORDER_LINE')
        jdbcTemplate.update('delete from TEST_ORDER')
        jdbcTemplate.update('delete from TEST_CUSTOMER')
    }

    def "test not changed attribute"() {
        def order
        def oldValue = null

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id)

            order.amount = 100
            oldValue = persistenceTools.getOldValue(order, 'number')
        }

        then:

        oldValue == '1'
    }

    def "test local attribute"() {
        def order
        def oldValue

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id)

            order.amount = 100
            oldValue = persistenceTools.getOldValue(order, 'amount')
        }

        then:

        oldValue == null

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id)

            order.amount = 200
            oldValue = persistenceTools.getOldValue(order, 'amount')
        }

        then:

        oldValue == 100
    }

    def "test local unfetched attribute"() {
        def order
        def oldValue

        def view = new FetchPlan(Order).addProperty('number').setLoadPartialEntities(true)

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id, view)

            order.amount = 100
            oldValue = persistenceTools.getOldValue(order, 'amount')
        }

        then:

        noExceptionThrown()
        oldValue == null

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id, view)

            order.amount = 200
            oldValue = persistenceTools.getOldValue(order, 'amount')
        }

        then:

        oldValue == 100
    }

    def "test collection attribute"() {
        def order
        def oldValue

        def view = new FetchPlan(Order).addProperty('orderLines', new FetchPlan(OrderLine).addProperty('productName'))

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id, view)

            def orderLine = metadata.create(OrderLine)
            orderLine.productName = "prod2"
            orderLine.order = order
            order.orderLines.add(orderLine)
            em.persist(orderLine)

            oldValue = persistenceTools.getOldValue(order, 'orderLines')
        }

        then:

        oldValue == [orderLine1]
    }

    def "test collection attribute after repeated deletion"() {
        Order order
        List<OrderLine> oldValue
        OrderLine orderLine11, orderLine12

        def view = new FetchPlan(Order).addProperty('orderLines', new FetchPlan(OrderLine).addProperty('productName'))

        persistence.runInTransaction { em ->
            orderLine11 = metadata.create(OrderLine)
            orderLine11.productName = "prod11"
            orderLine11.order = persistence.getEntityManager().getReference(Order, order1.id)
            em.persist(orderLine11)

            orderLine12 = metadata.create(OrderLine)
            orderLine12.productName = "prod12"
            orderLine12.order = persistence.getEntityManager().getReference(Order, order1.id)
            em.persist(orderLine12)
        }

        when:

        persistence.runInTransaction { em ->
            order = persistence.getEntityManager().find(Order, order1.id, view)

            def orderLine = order.orderLines.find { it.productName == 'prod11' }
            em.remove(orderLine)

            oldValue = persistenceTools.getOldValue(order, 'orderLines')
        }

        then:

        oldValue.sort { it.productName } == [orderLine1, orderLine11, orderLine12]

        when: "item is deleted and composition saved"

        def orderLine = order.orderLines.find { it.productName == 'prod12' }
        order.orderLines.remove(orderLine)

        persistence.runInTransaction { em ->
            def mergedOrder = em.merge(order)
            em.remove(orderLine)

            oldValue = persistenceTools.getOldValue(mergedOrder, 'orderLines')
            oldValue.each {
                println it
            }
        }

        then: "old value does not include previously deleted item"

        oldValue.sort { it.productName } == [orderLine1, orderLine12]

        cleanup:

        testSupport.deleteRecord(orderLine11, orderLine12)
    }

    def "test enum attribute"() {
        def customer
        def oldValue
        def oldEnumValue

        when:

        persistence.runInTransaction { em ->
            customer = persistence.getEntityManager().find(Customer, customer1.id)
            customer.setStatus(Status.OK)

            oldValue = persistenceTools.getOldValue(customer, 'status')
            oldEnumValue = persistenceTools.getOldEnumValue(customer, 'status')
        }

        then:

        oldValue == null
        oldEnumValue == null

        when:

        persistence.runInTransaction { em ->
            customer = persistence.getEntityManager().find(Customer, customer1.id)
            customer.setStatus(Status.NOT_OK)

            oldValue = persistenceTools.getOldValue(customer, 'status')
            oldEnumValue = persistenceTools.getOldEnumValue(customer, 'status')
        }

        then:

        oldValue == 'O'
        oldEnumValue == Status.OK

        when: "using getOldEnumValue for non-enum attribute"

        persistence.runInTransaction { em ->
            customer = persistence.getEntityManager().find(Customer, customer1.id)

            oldEnumValue = persistenceTools.getOldEnumValue(customer, 'name')
        }

        then: "return null"

        oldEnumValue == null
    }
}
