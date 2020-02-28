/*
 * Copyright (c) 2008-2020 Haulmont.
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

package spec.haulmont.cuba.core.setget

import com.haulmont.cuba.core.model.SettersEntity
import io.jmix.core.Metadata
import spec.haulmont.cuba.core.CoreTestSpecification

import javax.inject.Inject

class SettersTest extends CoreTestSpecification {

    @Inject
    private Metadata metadata

    private SettersEntity settersEntity

    void setup() {
        settersEntity = metadata.create(SettersEntity)
    }

    def "Overloaded setter"() {
        when:
        String stringValue = "stringValue"
        settersEntity.__getEntityEntry().setEntityValue("stringField", stringValue)
        String afterValue = settersEntity.__getEntityEntry().getEntityValue("stringField")

        then:
        stringValue == afterValue

        when:
        Double doubleValue = 10D
        settersEntity.__getEntityEntry().setEntityValue("stringField", doubleValue)
        afterValue = settersEntity.__getEntityEntry().getEntityValue("stringField")

        then:
        doubleValue.toString() == afterValue

        when:
        settersEntity.__getEntityEntry().setEntityValue("stringField", true)

        then:
        thrown(IllegalArgumentException)
    }

    def "Static setter"() {
        when:
        Boolean booleanValue = false
        SettersEntity.setStaticFlag(booleanValue)
        Boolean afterValue = SettersEntity.getStaticFlag()

        then:
        booleanValue == afterValue

        when:
        booleanValue = true
        settersEntity.__getEntityEntry().setEntityValue("staticFlag", booleanValue)

        then:
        thrown(IllegalArgumentException)
    }
}
