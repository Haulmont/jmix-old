/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.entity.multidb;

import io.jmix.data.entity.BaseDbGeneratedIdEntity;
import io.jmix.core.metamodel.annotations.NamePattern;
import io.jmix.core.metamodel.annotations.Store;

import javax.persistence.*;
import java.util.List;

@Entity(name = "test_Db1Customer")
@Table(name = "CUSTOMER")
@NamePattern("%s|name")
@Store(name = "db1")
public class Db1Customer extends BaseDbGeneratedIdEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO/*, generator = "ref$Db1Customer"*/)
//    @SequenceGenerator(name = "ref$Db1Customer", sequenceName = "customer_sequence", allocationSize = 1)
    @Column(name = "ID")
    protected Long id;

    @OneToMany(mappedBy = "customer")
    protected List<Db1Order> orders;

    @Column(name = "NAME")
    private String name;

    @Override
    protected void setDbGeneratedId(Long id) {
        this.id = id;
    }

    @Override
    protected Long getDbGeneratedId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Db1Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Db1Order> orders) {
        this.orders = orders;
    }
}
