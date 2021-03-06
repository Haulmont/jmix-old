/*
 * Copyright 2020 Haulmont.
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

package test_support

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.security.SecurityConfiguration
import io.jmix.securitydata.SecurityDataConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, SecurityConfiguration,
        SecurityDataConfiguration, SecurityDataTestConfiguration])
@TestPropertySource(properties = ["jmix.securityImplementation = standard"])
class SecurityDataSpecification extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    def cleanup() {
        jdbcTemplate.update("delete from SEC_ROW_LEVEL_POLICY_ENTITY")
        jdbcTemplate.update("delete from SEC_RESOURCE_POLICY_ENTITY")
        jdbcTemplate.update("delete from SEC_ROLE_ENTITY")
        jdbcTemplate.update("delete from SEC_ROLE_ASSIGNMENT_ENTITY")
    }
}
