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

package io.jmix.ui.gui.data.compatibility;

import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.xml.layout.loader.ComponentLoaderContext;
import org.dom4j.Element;

public interface DsSupport {

    ComponentLoaderContext createComponentLoaderContext(ScreenOptions screenOptions);

    void initDsContext(Screen screen, Element screenDescriptor, ComponentLoaderContext context);
}
