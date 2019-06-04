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

package io.jmix.ui.sys.delegates;

import io.jmix.ui.screen.FrameOwner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class InstalledSupplier implements Supplier {
    private final FrameOwner frameOwner;
    private final Method method;

    public InstalledSupplier(FrameOwner frameOwner, Method method) {
        this.frameOwner = frameOwner;
        this.method = method;
    }

    @Override
    public Object get() {
        try {
            return method.invoke(frameOwner);
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (e instanceof InvocationTargetException
                    && ((InvocationTargetException) e).getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ((InvocationTargetException) e).getTargetException();
            }

            throw new RuntimeException("Exception on @Install invocation", e);
        }
    }

    @Override
    public String toString() {
        return "InstalledSupplier{" +
                "target=" + frameOwner.getClass() +
                ", method=" + method +
                '}';
    }
}