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

package io.jmix.samples.helloworld.data.api;

import io.jmix.core.JmixEntity;
import io.jmix.core.LoadContext;

import javax.annotation.Nullable;
import java.util.List;

public interface ScreenDataRepository<T extends JmixEntity> {

    List<T> loadEntitiesList(LoadContext<T> loadContext);

    @Nullable
    T loadEntity(LoadContext<T> context);

}
