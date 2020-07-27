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

package io.jmix.ui.component.validation;

import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import io.jmix.core.Resources;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * GroovyScript validator runs a custom Groovy script. If the script returns any object,
 * then {@link ValidationException} is thrown. {@code scriptResult.toString()} is used as error message.
 * <p>
 * For error message it uses Groovy string and it is possible to use '$value' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *   &lt;bean id="ui_GroovyScriptValidator" class="io.jmix.ui.component.validation.GroovyScriptValidator" scope="prototype"/&gt;
 *   </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> any Object
 */
@Component(GroovyScriptValidator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroovyScriptValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "ui_GroovyScriptValidator";

    protected Resources resources;

    protected String validatorGroovyScript;

    protected String scriptPath;

    protected ScriptEvaluator scriptEvaluator;

    public GroovyScriptValidator() {
    }

    /**
     * Constructor with default error message.
     *
     * @param validatorGroovyScript groovy script with 'value' macro
     */
    public GroovyScriptValidator(String validatorGroovyScript) {
        this.validatorGroovyScript = validatorGroovyScript;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    protected void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    protected void setScriptEvaluator(ScriptEvaluator scriptEvaluator) {
        this.scriptEvaluator = scriptEvaluator;
    }

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    /**
     * @return a Groovy script
     */
    public String getValidatorGroovyScript() {
        return validatorGroovyScript;
    }

    /**
     * Sets a Groovy script
     *
     * @param validatorGroovyScript Groovy script
     */
    public void setValidatorGroovyScript(String validatorGroovyScript) {
        this.validatorGroovyScript = validatorGroovyScript;
    }

    /**
     * @return a path to Groovy script
     */
    public String getScriptPath() {
        return scriptPath;
    }

    /**
     * Sets a path to Groovy script
     *
     * @param scriptPath path to Groovy script
     */
    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }
        Object scriptResult = null;
        if (validatorGroovyScript != null) {
            scriptResult = scriptEvaluator.evaluate(new StaticScriptSource(validatorGroovyScript),
                    Collections.singletonMap("value", value));
        } else if (scriptPath != null) {
            scriptResult = scriptEvaluator.evaluate(new ResourceScriptSource(resources.getResource(scriptPath)),
                    Collections.singletonMap("value", value));
        }

        if (scriptResult != null) {
            setMessage(scriptResult.toString());
            fireValidationException(value);
        }
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        String formattedValue = formatValue(value);

        String formattedMessage = getTemplateErrorMessage(
                message, ParamsMap.of("value", formattedValue));

        throw new ValidationException(formattedMessage);
    }
}
