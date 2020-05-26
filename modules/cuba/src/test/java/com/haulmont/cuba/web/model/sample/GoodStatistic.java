/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.web.model.sample;

import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;

import java.math.BigDecimal;
import java.util.Date;

@ModelObject(name = "test_GoodStatistic")
public class GoodStatistic extends BaseUuidEntity {

    @ModelProperty
    protected String name;

    @ModelProperty
    protected Date fromDate;

    @ModelProperty
    protected Date toDate;

    @ModelProperty
    protected Integer sales;

    @ModelProperty
    protected Long count;

    @ModelProperty
    protected Double usages;

    @ModelProperty
    protected BigDecimal price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getUsages() {
        return usages;
    }

    public void setUsages(Double usages) {
        this.usages = usages;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
