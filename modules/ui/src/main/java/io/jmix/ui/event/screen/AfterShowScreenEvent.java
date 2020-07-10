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

package io.jmix.ui.event.screen;

import io.jmix.ui.screen.Screen;
import org.springframework.context.ApplicationEvent;

/**
 * Application event that is fired after {@link Screen} is shown.
 */
public class AfterShowScreenEvent extends ApplicationEvent {

    public AfterShowScreenEvent(Screen screen) {
        super(screen);
    }

    @Override
    public Screen getSource() {
        return (Screen) super.getSource();
    }
}
