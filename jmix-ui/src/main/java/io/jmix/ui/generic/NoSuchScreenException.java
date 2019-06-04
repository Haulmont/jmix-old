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
package io.jmix.ui.generic;

/**
 * Raised on attempt to open an unknown screen.
 */
public class NoSuchScreenException extends RuntimeException {

    private static final long serialVersionUID = -3751833162235475862L;

    private final String screenId;

    public NoSuchScreenException(String screenId) {
        super(String.format("Screen '%s' is not defined", screenId));

        this.screenId = screenId;
    }

    public String getScreenId() {
        return screenId;
    }
}