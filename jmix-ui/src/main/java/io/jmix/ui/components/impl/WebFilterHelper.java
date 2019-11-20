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

package io.jmix.ui.components.impl;

import com.haulmont.bali.datastruct.Node;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.entity.AbstractSearchFolder;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.Folder;
import io.jmix.core.BeanLocator;
import io.jmix.core.Configuration;
import io.jmix.ui.UiComponents;
import io.jmix.ui.components.*;
import io.jmix.ui.components.filter.ConditionsTree;
import io.jmix.ui.components.filter.FilterHelper;
import io.jmix.ui.components.filter.FtsFilterHelper;
import io.jmix.ui.components.filter.condition.AbstractCondition;
import io.jmix.ui.components.filter.condition.GroupCondition;
import io.jmix.ui.components.mainwindow.FoldersPane;
import io.jmix.ui.presentations.Presentations;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.AppUI;
import com.haulmont.cuba.web.WebConfig;
import io.jmix.ui.App.folders.AppFolderEditWindow;
import io.jmix.ui.App.folders.CubaFoldersPane;
import io.jmix.ui.App.folders.FolderEditWindow;
import com.haulmont.cuba.web.gui.components.util.ShortcutListenerDelegate;
import io.jmix.ui.widgets.CubaTextField;
import io.jmix.ui.widgets.CubaTree;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.components.grid.TreeGridDragSource;
import com.vaadin.ui.components.grid.TreeGridDropTarget;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

import static io.jmix.ui.screen.UiControllerUtils.getHostScreen;

@org.springframework.stereotype.Component(FilterHelper.NAME)
public class WebFilterHelper implements FilterHelper {

    protected static final String TREE_DRAGGED_ITEM_ID = "itemid";

    @Inject
    protected Configuration configuration;

    @Inject
    protected UiComponents uiComponents;

    @Inject
    protected BeanLocator beanLocator;

    @Override
    public void setLookupNullSelectionAllowed(LookupField lookupField, boolean value) {
        lookupField.setNullOptionVisible(value);
    }

    @Override
    public void setLookupTextInputAllowed(LookupField lookupField, boolean value) {
        lookupField.setTextInputAllowed(value);
    }

    @Override
    @Nullable
    public AbstractSearchFolder saveFolder(AbstractSearchFolder folder) {
        FoldersPane foldersPane = getUiFoldersPane();

        if (foldersPane == null)
            return null;

        CubaFoldersPane foldersPaneImpl = foldersPane.unwrapOrNull(CubaFoldersPane.class);
        if (foldersPaneImpl == null) {
            return null;
        }

        AbstractSearchFolder savedFolder = (AbstractSearchFolder) foldersPaneImpl.saveFolder(folder);
        foldersPaneImpl.refreshFolders();
        return savedFolder;
    }

    @Override
    public void openFolderEditWindow(boolean isAppFolder, AbstractSearchFolder folder, Presentations presentations, Runnable commitHandler) {
        FolderEditWindow window = AppFolderEditWindow.create(isAppFolder, false, folder, presentations, commitHandler);
        AppUI.getCurrent().addWindow(window);
    }

    @Override
    public boolean isFolderActionsEnabled() {
        return configuration.getConfig(WebConfig.class).getFoldersPaneEnabled();
    }

    @Override
    public boolean isFolderActionsAllowed(Frame frame) {
        return isFolderActionsEnabled() && mainScreenHasFoldersPane(frame);
    }

    public boolean mainScreenHasFoldersPane(Frame currentFrame) {
        RootWindow rootWindow = AppUI.getCurrent().getTopLevelWindow();
        if (rootWindow != null) {
            return rootWindow.getFrameOwner() instanceof Window.HasFoldersPane;
        } else {
            FrameOwner frameOwner = currentFrame.getFrameOwner();
            if (frameOwner instanceof ScreenFragment) {
                Screen rootScreen = getHostScreen((ScreenFragment) frameOwner);
                return rootScreen instanceof Window.HasFoldersPane;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initConditionsDragAndDrop(final Tree tree, final ConditionsTree conditions) {
        CubaTree vTree = tree.unwrapOrNull(CubaTree.class);
        if (vTree == null) {
            return;
        }

        TreeGridDragSource<AbstractCondition> treeGridDragSource = new TreeGridDragSource<>(vTree.getCompositionRoot());
        treeGridDragSource.setDragDataGenerator(TREE_DRAGGED_ITEM_ID, item -> item.getId().toString());

        TreeGridDropTarget<AbstractCondition> treeGridDropTarget =
                new TreeGridDropTarget<>(vTree.getCompositionRoot(), DropMode.ON_TOP_OR_BETWEEN);
        treeGridDropTarget.addTreeGridDropListener(event -> {
            if (!event.getDragSourceComponent().isPresent()
                    || event.getDragSourceComponent().get() != vTree.getCompositionRoot()) {
                return;
            }

            String sourceId = event.getDataTransferData(TREE_DRAGGED_ITEM_ID).isPresent() ?
                    event.getDataTransferData(TREE_DRAGGED_ITEM_ID).get() : null;

            if (sourceId == null) {
                return;
            }

            Object sourceItemId = UUID.fromString(sourceId);
            Object targetItemId = event.getDropTargetRow().isPresent() ? event.getDropTargetRow().get().getId() : null;

            if (targetItemId == null) {
                return;
            }

            // if we drop to itself
            if (targetItemId.equals(sourceItemId)) {
                return;
            }

            AbstractCondition sourceCondition = (AbstractCondition) tree.getItems().getItem(sourceItemId);
            AbstractCondition targetCondition = (AbstractCondition) tree.getItems().getItem(targetItemId);

            Node<AbstractCondition> sourceNode = conditions.getNode(sourceCondition);
            Node<AbstractCondition> targetNode = conditions.getNode(targetCondition);

            // if we drop parent to its child
            if (isAncestorOf(targetNode, sourceNode)) {
                return;
            }

            boolean moveToTheSameParent = Objects.equals(sourceNode.getParent(), targetNode.getParent());

            DropLocation location = event.getDropLocation();
            if (location == DropLocation.ON_TOP) {
                // prevent drop to not group condition
                if (!(targetCondition instanceof GroupCondition)) {
                    return;
                }

                if (sourceNode.getParent() == null) {
                    conditions.getRootNodes().remove(sourceNode);
                } else {
                    sourceNode.getParent().getChildren().remove(sourceNode);
                }
                targetNode.addChild(sourceNode);
                refreshConditionsDs(tree, conditions);
                tree.expand(targetCondition);
            } else {
                List<Node<AbstractCondition>> siblings;
                if (targetNode.getParent() == null)
                    siblings = conditions.getRootNodes();
                else
                    siblings = targetNode.getParent().getChildren();

                int targetIndex = siblings.indexOf(targetNode);
                if (location == DropLocation.BELOW)
                    targetIndex++;

                int sourceNodeIndex;
                if (sourceNode.getParent() == null) {
                    sourceNodeIndex = conditions.getRootNodes().indexOf(sourceNode);
                    conditions.getRootNodes().remove(sourceNode);
                } else {
                    sourceNodeIndex = sourceNode.getParent().getChildren().indexOf(sourceNode);
                    sourceNode.getParent().getChildren().remove(sourceNode);
                }

                //decrease drop position index if dragging from top to bottom inside the same parent node
                if (moveToTheSameParent && (sourceNodeIndex < targetIndex))
                    targetIndex--;

                // if we drop source accurate below expanded target
                if (tree.isExpanded(targetItemId) && location == DropLocation.BELOW) {
                    targetNode.insertChildAt(0, sourceNode);
                } else if (targetNode.getParent() == null) {
                    sourceNode.parent = null;
                    conditions.getRootNodes().add(targetIndex, sourceNode);
                } else {
                    targetNode.getParent().insertChildAt(targetIndex, sourceNode);
                }

                refreshConditionsDs(tree, conditions);
            }
        });
    }

    protected boolean isAncestorOf(Node childNode, Node possibleParentNode) {
        while (childNode.getParent() != null) {
            if (childNode.getParent().equals(possibleParentNode)) {
                return true;
            }
            childNode = childNode.getParent();
        }
        return false;
    }

    protected void refreshConditionsDs(Tree tree, ConditionsTree conditions) {
        tree.getDatasource().refresh(Collections.singletonMap("conditions", conditions));
    }

    @Override
    public Object getFoldersPane() {
        FoldersPane foldersPane = getUiFoldersPane();

        if (foldersPane == null) {
            return null;
        }

        return foldersPane.unwrapOrNull(CubaFoldersPane.class);
    }

    @Override
    public void removeFolderFromFoldersPane(Folder folder) {
        FoldersPane foldersPane = getUiFoldersPane();

        if (foldersPane == null) {
            return;
        }

        foldersPane.withUnwrapped(CubaFoldersPane.class, vFoldersPane -> {
            vFoldersPane.removeFolder(folder);
            vFoldersPane.refreshFolders();
        });
    }

    @Nullable
    protected FoldersPane getUiFoldersPane() {
        AppUI ui = AppUI.getCurrent();

        Screen topLevelWindow = ui.getTopLevelWindowNN().getFrameOwner();
        if (topLevelWindow instanceof Window.HasFoldersPane) {
            return ((Window.HasFoldersPane) topLevelWindow).getFoldersPane();
        }
        return null;
    }

    @Override
    public boolean isTableActionsEnabled() {
        return configuration.getConfig(WebConfig.class).getFoldersPaneEnabled();
    }

    @Override
    public void initTableFtsTooltips(ListComponent listComponent, MetaClass metaClass, String searchTerm) {
        FtsFilterHelper ftsFilterHelper;
        if (beanLocator.containsBean(FtsFilterHelper.NAME)) {
            ftsFilterHelper = beanLocator.get(FtsFilterHelper.class);
        } else {
            return;
        }

        if (listComponent instanceof Table) {
            listComponent.withUnwrapped(com.vaadin.v7.ui.Table.class, vTable ->
                    vTable.setItemDescriptionGenerator((source, itemId, propertyId) -> {
                        return ftsFilterHelper.buildTableTooltip(metaClass.getName(), itemId, searchTerm);
                    }));
        } else if (listComponent instanceof DataGrid) {
            ((DataGrid) listComponent).setRowDescriptionProvider(o -> {
                if (o instanceof Entity) {
                    return ftsFilterHelper.buildTableTooltip(metaClass.getName(), ((Entity) o).getId(), searchTerm);
                } else {
                    return null;
                }
            });
        }
    }

    @Override
    public void removeTableFtsTooltips(ListComponent listComponent) {
        if (listComponent instanceof Table) {
            listComponent.withUnwrapped(com.vaadin.v7.ui.Table.class, vTable ->
                    vTable.setItemDescriptionGenerator(null));
        } else if (listComponent instanceof DataGrid) {
            ((DataGrid) listComponent).setRowDescriptionProvider(null);
        }
    }

    @Override
    public void setFieldReadOnlyFocusable(TextField textField, boolean readOnlyFocusable) {
        textField.withUnwrapped(CubaTextField.class, vTextField ->
                vTextField.setReadOnlyFocusable(readOnlyFocusable));
    }

    @Override
    public void setComponentFocusable(io.jmix.ui.components.Component component, boolean focusable) {
        com.vaadin.ui.Component vComponent = component.unwrap(com.vaadin.ui.Component.class);
        if (vComponent instanceof Component.Focusable) {
            ((Component.Focusable) vComponent).setTabIndex(focusable ? 0 : -1);
        }
    }

    @Override
    public void setLookupCaptions(LookupField lookupField, Map<Object, String> captions) {
        lookupField.setOptionCaptionProvider(captions::get);
    }

    @Override
    public void addTextChangeListener(TextField textField, final TextChangeListener listener) {
        textField.addTextChangeListener(event -> listener.textChanged(event.getText()));
    }

    @Override
    public void addShortcutListener(TextField textField, final ShortcutListener listener) {
        textField.withUnwrapped(CubaTextField.class, vTextField -> {
            int[] modifiers = null;
            KeyCombination.Modifier[] listenerModifiers = listener.getKeyCombination().getModifiers();
            if (listenerModifiers != null) {
                modifiers = new int[listenerModifiers.length];
                for (int i = 0; i < modifiers.length; i++) {
                    modifiers[i] = listenerModifiers[i].getCode();
                }
            }
            int keyCode = listener.getKeyCombination().getKey().getCode();

            vTextField.addShortcutListener(
                    new ShortcutListenerDelegate(listener.getCaption(), keyCode, modifiers)
                            .withHandler((sender, target) ->
                                    listener.handleShortcutPressed()
                            ));
        });
    }

    @Override
    public void setLookupFieldPageLength(LookupField lookupField, int pageLength) {
        lookupField.setPageLength(pageLength);
    }

    @Override
    public void setInternalDebugId(io.jmix.ui.components.Component component, String id) {
        AppUI ui = AppUI.getCurrent();
        if (ui != null && ui.isTestMode()) {
            component.unwrap(Component.class).setCubaId(id);
        }
    }

    @Override
    public io.jmix.ui.components.ComponentContainer createSearchButtonGroupContainer() {
        CssLayout layout = uiComponents.create(CssLayout.class);
        layout.addStyleName("v-component-group");
        return layout;
    }
}
