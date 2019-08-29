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

package io.jmix.core;

import com.sample.addon1.TestAddon1Configuration;
import com.sample.app.TestAppConfiguration;
import com.sample.app.entity.Owner;
import com.sample.app.entity.Pet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmixCoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class ViewBuilderTest {

    @Test
    public void testBuild() {
        View view = ViewBuilder.of(Pet.class).build();

        assertNotNull(view);
        assertFalse(containsSystemProperties(view));
        assertFalse(view.containsProperty("name"));
    }

    @Test
    public void testProperty() {
        View view = ViewBuilder.of(Pet.class).add("name").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testRefProperty() {
        View view = ViewBuilder.of(Pet.class).add("owner").build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        View ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
    }

    @Test
    public void testInlineRefProperty() {
        View view = ViewBuilder.of(Pet.class)
                .add("owner.name")
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        View ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
    }

    @Test
    public void testRefView() {
        View view = ViewBuilder.of(Pet.class)
                .add("owner", builder -> builder.add("name"))
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        View ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
    }

    @Test
    public void testRefLocalView() {
        View view = ViewBuilder.of(Pet.class)
                .add("owner", View.LOCAL)
                .build();

        assertFalse(containsSystemProperties(view));

        assertNotNull(view.getProperty("owner"));
        View ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertTrue(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testProperties() {
        View view = ViewBuilder.of(Pet.class).addAll("name", "nick").build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testSystem() {
        View view = ViewBuilder.of(Pet.class).addSystem().addAll("name").build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testMinimal() {
        View view = ViewBuilder.of(Pet.class).addView(View.MINIMAL).build();

        assertFalse(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocal() {
        View petView = ViewBuilder.of(Pet.class).addView(View.LOCAL).build();

        assertTrue(containsSystemProperties(petView));
        assertTrue(petView.containsProperty("name"));

        View ownerView = ViewBuilder.of(Owner.class).addView(View.LOCAL).build();
        assertTrue(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));
    }

    @Test
    public void testBase() {
        View view = ViewBuilder.of(Pet.class).addView(View.BASE).build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));
    }

    @Test
    public void testLocalAndRef() {
        View view = ViewBuilder.of(Pet.class)
                .addView(View.LOCAL)
                .add("owner")
                .build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        View ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertFalse(ownerView.containsProperty("name"));
        assertFalse(ownerView.containsProperty("address"));

        view = ViewBuilder.of(Pet.class)
                .addView(View.LOCAL)
                .add("owner.name")
                .add("owner.address.city")
                .build();

        assertTrue(containsSystemProperties(view));
        assertTrue(view.containsProperty("name"));

        assertNotNull(view.getProperty("owner"));
        ownerView = view.getProperty("owner").getView();
        assertNotNull(ownerView);
        assertFalse(containsSystemProperties(ownerView));
        assertTrue(ownerView.containsProperty("name"));
        assertTrue(ownerView.containsProperty("address"));

        View addressView = ownerView.getProperty("address").getView();
        assertTrue(addressView.containsProperty("city"));
    }

    private boolean containsSystemProperties(View view) {
        return view.containsProperty("id")
                && view.containsProperty("version")
                && view.containsProperty("deleteTs")
                && view.containsProperty("deletedBy")
                && view.containsProperty("createTs")
                && view.containsProperty("createdBy")
                && view.containsProperty("updateTs")
                && view.containsProperty("updatedBy");
    }

}