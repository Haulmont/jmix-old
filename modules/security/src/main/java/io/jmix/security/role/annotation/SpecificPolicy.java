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

package io.jmix.security.role.annotation;

import java.lang.annotation.*;

/**
 * Defines specific resource policy in annotated role. Multiple {@code SpecificPolicy} annotations may be placed on a
 * single method. {@code SpecificPolicy} annotation may present on multiple methods of the same class. Annotated method
 * may have any name and return type.
 * <p>
 * Example:
 * <pre>
 * &#064;Role(name = "My Role", code = "myRole")
 * public interface MyRole {
 *
 *     &#064;SpecificPolicy(resources = {"app.order.someSpecificStuff", "app.order.anotherSpecificStuff"})
 *     void specific();
 * }
 * </pre>
 *
 * @see io.jmix.security.role.annotation.Role
 * @see io.jmix.security.model.ResourcePolicy
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SpecificPolicyContainer.class)
public @interface SpecificPolicy {

    String[] resources();

}