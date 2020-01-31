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

package metadata

import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.Stores
import io.jmix.core.entity.BaseGenericIdEntity
import io.jmix.core.entity.BaseUuidEntity
import io.jmix.core.entity.StandardEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.app.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class MetadataTest extends Specification {

    @Inject
    Metadata metadata

    def "entities are in metadata"() {
        expect:

        metadata.getClass(StandardEntity) != null
        metadata.getClass(TestAddon1Entity) != null
    }

    def "ancestors and descendants are collected recursively"() {

        def pet = metadata.getClassNN(Pet)
        def standardEntity = metadata.getClassNN(StandardEntity)
        def baseUuidEntity = metadata.getClassNN(BaseUuidEntity)
        def baseGenericIdEntity = metadata.getClassNN(BaseGenericIdEntity)

        expect:

        pet.ancestor == standardEntity
        pet.ancestors[0] == pet.ancestor
        pet.ancestors[1] == baseUuidEntity
        pet.ancestors[2] == baseGenericIdEntity

        baseGenericIdEntity.descendants.containsAll([baseUuidEntity, standardEntity, pet])
        baseUuidEntity.descendants.containsAll([standardEntity, pet])
        standardEntity.descendants.containsAll([pet])
    }

    def "inherited properties"() {

        def baseMetaClass = metadata.getClassNN(StandardEntity)
        def baseProp = baseMetaClass.getPropertyNN('createTs')
        def baseIdProp = baseMetaClass.getPropertyNN('id')

        def entityMetaClass = metadata.getClassNN(TestAddon1Entity)
        def entityProp = entityMetaClass.getPropertyNN('createTs')
        def entityIdProp = entityMetaClass.getPropertyNN('id')

        expect:

        !entityProp.is(baseProp)
        baseProp.domain == baseMetaClass
        entityProp.domain == entityMetaClass

        entityProp.range == baseProp.range
        entityProp.annotatedElement == baseProp.annotatedElement
        entityProp.declaringClass == baseProp.declaringClass
        entityProp.inverse == baseProp.inverse
        entityProp.javaType == baseProp.javaType
        entityProp.mandatory == baseProp.mandatory
        entityProp.readOnly == baseProp.readOnly

        !entityIdProp.is(baseIdProp)
        entityIdProp.domain == entityMetaClass
        entityIdProp.range == baseIdProp.range
        entityIdProp.annotatedElement == baseIdProp.annotatedElement
    }

    def "store of entity is NOOP"() {

        def metaClass = metadata.getClassNN(TestAddon1Entity)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.NOOP
    }

    def "store of mapped superclass is UNDEFINED"() {

        def metaClass = metadata.getClassNN(StandardEntity)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.UNDEFINED
    }

    def "store of entity property is NOOP"() {

        def metaProperty = metadata.getClassNN(TestAddon1Entity).getPropertyNN('name')

        expect:

        metaProperty.store != null
        metaProperty.store.name == Stores.NOOP
    }

    def "store of mapped superclass property is UNDEFINED"() {

        def metaProperty = metadata.getClassNN(StandardEntity).getPropertyNN('createTs')

        expect:

        metaProperty.store != null
        metaProperty.store.name == Stores.UNDEFINED
    }

    def "store of entity property inherited from mapped superclass is NOOP"() {

        def idProp = metadata.getClassNN(TestAddon1Entity).getPropertyNN('id')
        def createTsProp = metadata.getClassNN(TestAddon1Entity).getPropertyNN('createTs')

        expect:

        idProp.store.name == Stores.NOOP
        createTsProp.store.name == Stores.NOOP
    }

    def "store of entity annotated with @Entity is MAIN"() {

        def metaClass = metadata.getClassNN(Pet)
        def idProp = metaClass.getPropertyNN('id')
        def nameProp = metaClass.getPropertyNN('name')

        expect:

        idProp.store.name == Stores.MAIN
        nameProp.store.name == Stores.MAIN
    }

    def "store of non-mapped property of entity annotated with @Entity is UNDEFINED"() {

        def metaClass = metadata.getClassNN(Pet)
        def nickProp = metaClass.getPropertyNN('nick')

        expect:

        nickProp.store.name == Stores.UNDEFINED
    }

    def "store of method-based property of entity annotated with @Entity is UNDEFINED"() {

        def metaClass = metadata.getClassNN(Pet)
        def descriptionProp = metaClass.getPropertyNN('description')

        expect:

        descriptionProp.store.name == Stores.UNDEFINED
    }
}
