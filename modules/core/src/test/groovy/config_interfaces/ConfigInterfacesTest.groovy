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

package config_interfaces

import test_support.addon1.TestAddon1Configuration
import test_support.app.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import test_support.app.TestConfig
import io.jmix.core.ConfigInterfaces
import io.jmix.core.GlobalConfig
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.compatibility.AppProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class ConfigInterfacesTest extends Specification {

    @Autowired
    ConfigInterfaces configInterfaces

    @Autowired
    AppProperties appProperties

    @Autowired
    Environment env

    def "default value can be overridden by setting property via AppProperties"() {
        when:

        def config = configInterfaces.getConfig(GlobalConfig)

        then:

        config != null
        config.getWebHostName() == 'localhost'

        when:

        appProperties.setProperty('cuba.webHostName', 'some_host')

        then:

        config.getWebHostName() == 'some_host'

        when:

        appProperties.setProperty('cuba.webHostName', null)

        then:

        config.getWebHostName() == 'localhost'
    }

    def "default value can be overridden by Environment properties"() {
        when:

        def testConfig = configInterfaces.getConfig(TestConfig)
        def globalConfig = configInterfaces.getConfig(GlobalConfig)

        then:

        env.getProperty('app.foo') == 'foo_value'
        env.getProperty('cuba.webContextName') == 'app'

        testConfig.getFoo() == 'foo_value'
        globalConfig.getWebContextName() == 'app'
    }
}
