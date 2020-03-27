/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.core.impl.method;

import io.jmix.core.BeanLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves method parameters by delegating to a list of registered
 * {@link MethodArgumentResolver MethodArgumentResolvers}.
 * Previously resolved method parameters are cached for faster lookups.
 */
public class ContextArgumentResolverComposite extends CachedArgumentResolverComposite {

	protected BeanLocator beanLocator;

	public ContextArgumentResolverComposite(BeanLocator beanLocator) {
		this.beanLocator = beanLocator;
	}

	/**
	 * Return a read-only list with the contained resolvers, or an empty list.
	 */
	public List<MethodArgumentResolver> getResolvers() {
		return new ArrayList<>(beanLocator.getAll(MethodArgumentResolver.class).values());
	}

}
