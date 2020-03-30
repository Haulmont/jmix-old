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

package com.haulmont.cuba;

import com.haulmont.cuba.core.global.impl.CubaMetadata;
import com.haulmont.cuba.core.global.impl.MessagesImpl;
import com.haulmont.cuba.web.gui.CubaUiComponents;
import com.haulmont.cuba.web.gui.CubaUiControllerReflectionInspector;
import io.jmix.core.BeanLocator;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.JmixProperty;
import io.jmix.core.impl.MetadataLoader;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.ui.JmixUiConfiguration;
import io.jmix.ui.UiComponents;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.settings.JmixUiSettingsConfiguration;
import io.jmix.ui.sys.UiControllerDependencyInjector;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.sys.UiControllersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(
        dependsOn = {
                JmixCoreConfiguration.class,
                JmixDataConfiguration.class,
                JmixUiConfiguration.class,
                JmixUiSettingsConfiguration.class},
        properties = {
                @JmixProperty(name = "jmix.core.fetchPlansConfig", value = "/com/haulmont/cuba/cuba-views.xml", append = true),
                @JmixProperty(name = "cuba.windowConfig", value = "/com/haulmont/cuba/web-screens.xml", append = true)
        })
public class JmixCubaConfiguration {

    protected BeanLocator beanLocator;
    protected UiControllerReflectionInspector uiControllerReflectionInspector;

    @Autowired
    protected void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Autowired
    protected void setUiControllerReflectionInspector(UiControllerReflectionInspector uiControllerReflectionInspector) {
        this.uiControllerReflectionInspector = uiControllerReflectionInspector;
    }

    @Bean(Messages.NAME)
    protected Messages messages() {
        return new MessagesImpl();
    }

    @Bean(Metadata.NAME)
    protected Metadata metadata(MetadataLoader metadataLoader) {
        return new CubaMetadata(metadataLoader);
    }

    @Bean(UiComponents.NAME)
    protected UiComponents uiComponents() {
        return new CubaUiComponents();
    }

    @Bean("cuba_UiControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("com.haulmont.cuba.web.app"));
        return uiControllers;
    }

    @Bean(UiControllerDependencyInjector.NAME)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    protected UiControllerDependencyInjector uiControllerDependencyInjector(FrameOwner frameOwner, ScreenOptions options) {
        UiControllerDependencyInjector injector = new CubaUiControllerReflectionInspector(frameOwner, options);
        injector.setBeanLocator(beanLocator);
        injector.setReflectionInspector(uiControllerReflectionInspector);
        return injector;
    }
}
