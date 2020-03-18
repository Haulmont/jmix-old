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

package test_support.entity;

import io.jmix.data.entity.StandardEntity;

import javax.persistence.*;

@Entity(name = "test_TestSecondAppEntity")
@Table(name = "TEST_SECOND_APP_ENTITY")
public class TestSecondAppEntity extends StandardEntity {

    private static final long serialVersionUID = 8256929425690816623L;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_ENTITY_ID")
    private TestAppEntity appEntity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestAppEntity getAppEntity() {
        return appEntity;
    }

    public void setAppEntity(TestAppEntity appEntity) {
        this.appEntity = appEntity;
    }
}
