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

package io.jmix.ui.sys;

import com.google.common.base.Strings;
import io.jmix.core.DevelopmentException;
import io.jmix.ui.screen.*;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public final class UiDescriptorUtils {

    private UiDescriptorUtils() {
    }

    public static String getInferredScreenId(UiController uiController,
                                             Class<? extends FrameOwner> annotatedScreenClass) {
        checkNotNullArgument(uiController);
        checkNotNullArgument(annotatedScreenClass);

        return getInferredScreenId(uiController.id(), uiController.value(), annotatedScreenClass.getName());
    }

    public static String getInferredScreenId(String idAttribute, String valueAttribute, String className) {
        String id = valueAttribute;
        if (Strings.isNullOrEmpty(id)) {
            id = idAttribute;

            if (Strings.isNullOrEmpty(id)) {
                int indexOfDot = className.lastIndexOf('.');
                if (indexOfDot < 0) {
                    id = className;
                } else {
                    id = className.substring(indexOfDot + 1);
                }
            }
        }

        return id;
    }

    public static String getInferredTemplate(UiDescriptor uiDescriptor,
                                             Class<? extends FrameOwner> annotatedScreenClass) {
        checkNotNullArgument(uiDescriptor);

        String templateLocation = uiDescriptor.value();
        if (Strings.isNullOrEmpty(templateLocation)) {
            templateLocation = uiDescriptor.path();

            if (Strings.isNullOrEmpty(templateLocation)) {
                throw new DevelopmentException("Screen class annotated with @UiDescriptor without template: " + annotatedScreenClass);
            }
        }

        return templateLocation;
    }

    public static String getInferredSubscribeId(Subscribe subscribe) {
        checkNotNullArgument(subscribe);

        String target = subscribe.value();
        if (Strings.isNullOrEmpty(target)) {
            target = subscribe.id();
        }

        return target;
    }

    public static String getInferredProvideId(Install install) {
        checkNotNullArgument(install);

        return install.to();
    }
}