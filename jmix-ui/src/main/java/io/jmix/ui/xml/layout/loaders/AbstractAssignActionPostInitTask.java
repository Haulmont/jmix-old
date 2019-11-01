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

package io.jmix.ui.xml.layout.loaders;

import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.sys.ValuePathHelper;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.ComponentLoader;

public abstract class AbstractAssignActionPostInitTask implements ComponentLoader.PostInitTask {
    protected Component component;
    protected String actionId;
    protected Frame frame;

    public AbstractAssignActionPostInitTask(Component component, String actionId, Frame frame) {
        this.component = component;
        this.actionId = actionId;
        this.frame = frame;
    }

    @Override
    public void execute(ComponentLoader.ComponentContext context, Frame frame) {
        String[] elements = ValuePathHelper.parse(actionId);
        if (elements.length > 1) {
            String id = elements[elements.length - 1];

            // using this.frame to look up the component inside the actual frame
            String prefix = ValuePathHelper.pathPrefix(elements);
            Component holder = this.frame.getComponent(prefix);
            if (holder == null) {
                throw new GuiDevelopmentException(
                        String.format("Can't find component: %s for action: %s", prefix, actionId),
                        context, "Component ID", prefix);
            }

            if (!(holder instanceof ActionsHolder)) {
                throw new GuiDevelopmentException(String.format(
                        "Component '%s' can't contain actions", holder.getId()), context,
                        "Holder ID", holder.getId());
            }

            Action action = ((ActionsHolder) holder).getAction(id);
            if (action == null) {
                throw new GuiDevelopmentException(String.format(
                        "Can't find action '%s' in '%s'", id, holder.getId()), context,
                        "Holder ID", holder.getId());
            }

            addAction(action);
        } else if (elements.length == 1) {
            String id = elements[0];
            Action action = getActionRecursively(this.frame, id);

            if (action == null) {
                if (!hasOwnAction(id)) {
                    String message = "Can't find action " + id;
                    if (Window.Editor.WINDOW_COMMIT.equals(id) || Window.Editor.WINDOW_COMMIT_AND_CLOSE.equals(id)) {
                        message += ". This may happen if you are opening an AbstractEditor-based screen by openWindow() method, " +
                                "for example from the main menu. Use openEditor() method or give the screen a name ended " +
                                "with '.edit' to open it as editor from the main menu.";
                    }

                    throw new GuiDevelopmentException(message, context.getFullFrameId());
                }
            } else {
                addAction(action);
            }
        } else {
            throw new GuiDevelopmentException("Empty action name", context.getFullFrameId());
        }
    }

    protected abstract boolean hasOwnAction(String id);

    protected abstract void addAction(Action action);

    protected Action getActionRecursively(Frame frame, String actionId) {
        Action action = frame.getAction(actionId);
        if (action == null) {
            Frame parentFrame = frame.getFrame();
            if (parentFrame != frame) {
                return getActionRecursively(parentFrame, actionId);
            }
        }
        return action;
    }
}