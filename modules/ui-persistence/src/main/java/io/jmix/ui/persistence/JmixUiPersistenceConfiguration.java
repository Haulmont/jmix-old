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

package io.jmix.ui.persistence;

import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.JmixProperty;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.ui.JmixUiConfiguration;
import io.jmix.ui.components.Component;
import io.jmix.ui.presentations.TablePresentations;
import io.jmix.ui.settings.UserSettingService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@EnableTransactionManagement
@JmixModule(
        dependsOn = {JmixCoreConfiguration.class, JmixDataConfiguration.class, JmixUiConfiguration.class},
        properties = {@JmixProperty(name = "jmix.core.fetchPlansConfig", value = "io/jmix/ui/persistence/fetch-plan.xml", append = true)})
public class JmixUiPersistenceConfiguration {

    @Bean(UserSettingService.NAME)
    public UserSettingService userSettingService() {
        return new UserSettingImpl();
    }

    @Bean(TablePresentations.NAME)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public TablePresentations presentations(Component component) {
        return new PresentationsImpl(component);
    }
}

