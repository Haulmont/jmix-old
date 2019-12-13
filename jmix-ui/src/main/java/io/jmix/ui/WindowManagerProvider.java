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

package io.jmix.ui;

import io.jmix.ui.components.compatibility.WindowManager;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;

/**
 * Provides current client specific WindowManager.
 *
 * @deprecated Pass {@link FrameOwner} explicitly instead. Use {@link UiControllerUtils} to get {@link Screens} API.
 */
@Deprecated
public interface WindowManagerProvider {

    String NAME = "cuba_WindowManagerProvider";

    /**
     * @deprecated Pass {@link FrameOwner} explicitly instead.
     */
    @Deprecated
    WindowManager get();
}