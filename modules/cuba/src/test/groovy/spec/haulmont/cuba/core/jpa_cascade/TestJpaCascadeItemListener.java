/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.jpa_cascade;

import com.haulmont.cuba.core.model.jpa_cascade.JpaCascadeItem;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.cuba.core.listener.BeforeUpdateEntityListener;

import java.util.ArrayList;
import java.util.List;

public class TestJpaCascadeItemListener implements
        BeforeInsertEntityListener<JpaCascadeItem>, BeforeUpdateEntityListener<JpaCascadeItem> {

    static List<String> messages = new ArrayList<>();

    @Override
    public void onBeforeInsert(JpaCascadeItem entity, EntityManager entityManager) {
        messages.add("onBeforeInsert " + entity);
    }

    @Override
    public void onBeforeUpdate(JpaCascadeItem entity, EntityManager entityManager) {
        messages.add("onBeforeUpdate " + entity);
    }
}
