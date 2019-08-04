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

package io.jmix.ui.components.impl;

import io.jmix.ui.components.data.ContainerValueSource;
import io.jmix.ui.components.data.ValueSource;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public final class UiTestIds {

    private UiTestIds() {
    }

    @Nullable
    public static String getInferredTestId(ValueSource<?> valueSource) {
        if (valueSource instanceof ContainerValueSource) {
            ContainerValueSource dcValueSource = (ContainerValueSource) valueSource;

            return StringUtils.join(dcValueSource.getMetaPropertyPath().getPropertyNames(), "_");
        }

        return null;
    }
}