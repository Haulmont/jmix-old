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

import io.jmix.core.sys.serialization.Serialization
import io.jmix.core.sys.serialization.StandardSerialization
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.security.entity.UserRole
import spock.lang.Specification

class ObservableListTest extends Specification {

    def "test serialization"() {
        Serialization serialization = new StandardSerialization()

        when:

        User user1 = new User()
        user1.login = "u1"
        user1.name = "User 1"
        user1.userRoles = new ObservableList<>(new ArrayList<>(), null)

        Role role1 = new Role()
        role1.name = "Role 1"

        UserRole user1Role1 = new UserRole()
        user1Role1.user = user1
        user1Role1.role = role1

        user1.userRoles.add(user1Role1)

        User deserializedUser = (User) serialization.deserialize(serialization.serialize(user1))

        then:

        deserializedUser.userRoles instanceof ArrayList
    }
}
