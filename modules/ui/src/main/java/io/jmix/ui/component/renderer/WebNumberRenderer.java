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

package io.jmix.ui.component.renderer;

import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.renderers.Renderer;
import io.jmix.core.JmixEntity;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.impl.WebAbstractDataGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A renderer for presenting number values.
 */
@Component(DataGrid.NumberRenderer.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WebNumberRenderer extends WebAbstractDataGrid.AbstractRenderer<JmixEntity, Number> implements DataGrid.NumberRenderer {

    private Locale locale;
    private NumberFormat numberFormat;
    private String formatString;

    public WebNumberRenderer() {
        super("");
    }

    public WebNumberRenderer(NumberFormat numberFormat) {
        this(numberFormat, "");
    }

    public WebNumberRenderer(NumberFormat numberFormat, String nullRepresentation) {
        super(nullRepresentation);

        this.numberFormat = numberFormat;
    }

    public WebNumberRenderer(String formatString) throws IllegalArgumentException {
        this(formatString, null);
    }

    public WebNumberRenderer(String formatString, Locale locale) throws IllegalArgumentException {
        this(formatString, locale, "");
    }

    public WebNumberRenderer(String formatString, Locale locale, String nullRepresentation) {
        super(nullRepresentation);

        this.formatString = formatString;
        this.locale = locale;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        if (locale == null) {
            locale = currentAuthentication.getLocale();
        }
    }

    @Override
    public NumberRenderer getImplementation() {
        return (NumberRenderer) super.getImplementation();
    }

    @Override
    protected Renderer<Number> createImplementation() {
        if (numberFormat != null) {
            return new NumberRenderer(numberFormat, getNullRepresentation());
        } else {
            checkNotNullArgument(formatString, "Format string may not be null");
            checkNotNullArgument(locale, "Locale may not be null");
            return new NumberRenderer(formatString, locale, getNullRepresentation());
        }
    }

    @Override
    public String getNullRepresentation() {
        return super.getNullRepresentation();
    }

    @Override
    public void setNullRepresentation(String nullRepresentation) {
        super.setNullRepresentation(nullRepresentation);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        checkRendererNotSet();
        this.locale = locale;
    }

    @Override
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    @Override
    public void setNumberFormat(NumberFormat numberFormat) {
        checkRendererNotSet();
        this.numberFormat = numberFormat;
        this.formatString = null;
    }

    @Override
    public String getFormatString() {
        return formatString;
    }

    @Override
    public void setFormatString(String formatString) {
        checkRendererNotSet();
        this.formatString = formatString;
        this.numberFormat = null;
    }
}
