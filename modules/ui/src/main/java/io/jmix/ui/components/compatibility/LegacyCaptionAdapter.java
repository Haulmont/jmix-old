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

package io.jmix.ui.components.compatibility;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.components.CaptionMode;
import io.jmix.ui.components.HasItemCaptionProvider;
import io.jmix.ui.components.HasOptionCaptionProvider;

import java.util.function.Function;

/**
 * Legacy item/options caption provider adapter that supports {@code captionMode} and {@code captionProperty}
 * properties.
 *
 * @deprecated use {@link HasItemCaptionProvider#setItemCaptionProvider(Function)} or
 * {@link HasOptionCaptionProvider#setOptionCaptionProvider(Function)} directly instead
 */
@Deprecated
public class LegacyCaptionAdapter implements Function<Object, String> {

    protected CaptionMode captionMode;
    protected String captionProperty;

    protected Metadata metadata = AppBeans.get(Metadata.NAME);
    protected MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);

    public LegacyCaptionAdapter(CaptionMode captionMode, String captionProperty) {
        this.captionMode = captionMode;
        this.captionProperty = captionProperty;
    }

    @Override
    public String apply(Object o) {
        if (!(o instanceof Entity)) {
            return "";
        }

        Entity entity = (Entity) o;
        if (captionMode == CaptionMode.PROPERTY
                && captionProperty != null) {

            if (metadata.getClass(entity).getPropertyPath(captionProperty) == null) {
                throw new IllegalArgumentException(String.format("Couldn't find property with name '%s'", captionProperty));
            }

            Object propertyValue = EntityValues.getAttributeValueEx(entity, captionProperty);
            return propertyValue != null
                    ? propertyValue.toString()
                    : " ";
        }

        return metadataTools.getInstanceName(entity);
    }

    public CaptionMode getCaptionMode() {
        return captionMode;
    }

    public void setCaptionMode(CaptionMode captionMode) {
        this.captionMode = captionMode;
    }

    public String getCaptionProperty() {
        return captionProperty;
    }

    public void setCaptionProperty(String captionProperty) {
        this.captionProperty = captionProperty;
    }
}
