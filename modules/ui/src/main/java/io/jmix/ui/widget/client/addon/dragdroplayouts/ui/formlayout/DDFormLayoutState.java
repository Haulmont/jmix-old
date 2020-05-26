/*
 * Copyright 2015 John Ahlroos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.ui.widget.client.addon.dragdroplayouts.ui.formlayout;

import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.DDLayoutState;
import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.interfaces.DragAndDropAwareState;
import com.vaadin.shared.annotations.DelegateToWidget;
import com.vaadin.shared.ui.orderedlayout.FormLayoutState;

public class DDFormLayoutState extends FormLayoutState
        implements DragAndDropAwareState {

    public static final float DEFAULT_VERTICAL_DROP_RATIO = 0.3333f;

    @DelegateToWidget
    public float cellTopBottomDropRatio = DEFAULT_VERTICAL_DROP_RATIO;

    public DDLayoutState ddState = new DDLayoutState();

    @Override
    public DDLayoutState getDragAndDropState() {
        return ddState;
    }
}
