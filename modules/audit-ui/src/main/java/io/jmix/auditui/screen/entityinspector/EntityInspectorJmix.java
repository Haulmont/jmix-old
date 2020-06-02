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

package io.jmix.auditui.screen.entityinspector;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.Actions;
import io.jmix.ui.Screens;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.list.*;
import io.jmix.ui.action.picker.ClearAction;
import io.jmix.ui.action.picker.LookupAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.table.ContainerTableItems;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.*;

import static io.jmix.auditui.screen.entityinspector.EntityFormUtils.*;
import static io.jmix.core.metamodel.model.MetaProperty.Type.ASSOCIATION;
import static io.jmix.ui.component.Component.FULL_SIZE;

@UiController("entityInspector.edit")
@UiDescriptor("entity-inspector-edit.xml")
public class EntityInspectorJmix extends Screen implements EditorScreen {

    public static final int MAX_TEXTFIELD_STRING_LENGTH = 255;
    public static final int CAPTION_MAX_LENGTH = 100;
    public static final int MAX_TEXT_LENGTH = 50;
    public static final Screens.LaunchMode OPEN_TYPE = OpenMode.THIS_TAB;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiComponentsGenerator uiComponentsGenerator;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected Screens screens;
    @Autowired
    protected Actions actions;
    @Inject
    protected Icons icons;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected BoxLayout contentPane;

    @Autowired
    protected TabSheet tablesTabSheet;

    private DataContext dataContext;

    protected Entity item;
    protected Boolean isNew = false;
    protected MetaClass meta;
    protected String parentProperty;
    protected InstanceContainer container;

    protected Boolean autocommit;

    @Subscribe
    protected void onInit(InitEvent initEvent) {
        MapScreenOptions screenOptions = (MapScreenOptions) initEvent.getOptions();
        Map<String, Object> params = screenOptions.getParams();

        init(params);
        getOrCreateNewItem(params);
        setWindowCaption();
        initShortcuts();

        container = initMainContainer(params);
        container.setItem(item);
        createForm(null, container);
    }

    private InstanceContainer initMainContainer(Map<String, Object> params) {
        return params.get("parentContainer") != null && params.get("parentProperty") != null ?
                dataComponents.createInstanceContainer(
                        item.getClass(), (InstanceContainer) params.get("parentContainer"), (String) params.get("parentProperty"))
                : dataComponents.createInstanceContainer(item.getClass());
    }

    @Subscribe("windowClose")
    protected void onWindowCloseActionPerformed(Action.ActionPerformedEvent event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("windowCommitAndClose")
    protected void onWindowCommitAndCloseActionPerformed(Action.ActionPerformedEvent event) {
        if (autocommit) {
            dataContext.commit();
        }
        close(StandardOutcome.COMMIT);
    }

    protected void initShortcuts() {
        Action commitAction = new BaseAction("commitAndClose")
                .withCaption(messages.getMessage("actions.OkClose"))
                .withShortcut(uiProperties.getCommitShortcut())
                .withHandler(e ->
                        onWindowCommitAndCloseActionPerformed(null)
                );
        getWindow().addAction(commitAction);
    }

    private void createForm(String caption, InstanceContainer container) {
        MetaClass metaClass = container.getEntityMetaClass();
        Entity item = container.getItem();

        Form form = uiComponents.create(Form.class);
        if (caption != null) {
            form.setCaption(caption);
        }

        contentPane.add(form);
        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            boolean isReadonly = metaProperty.isReadOnly();
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    boolean includeId = primaryKeyProperty.equals(metaProperty)
                            && String.class.equals(metaProperty.getJavaType());
                    //skip system properties
                    if (metadataTools.isSystem(metaProperty) && !includeId) {
                        continue;
                    }
                    if (metaProperty.getType() != MetaProperty.Type.ENUM
                            && (isByteArray(metaProperty) || isUuid(metaProperty))) {
                        continue;
                    }

                    if (includeId && !isNew) {
                        isReadonly = true;
                    }

                    addField(container, form, metaProperty, isReadonly);
                    break;
                case COMPOSITION:
                case ASSOCIATION:
                    if (metaProperty.getRange().getCardinality().isMany()) {
                        addTable(container, metaProperty);
                    } else {
                        if (isEmbedded(metaProperty)) {
                            Entity propertyValue = EntityValues.getValue(item, metaProperty.getName());
                            propertyValue = dataContext.merge(propertyValue);
                            InstanceContainer embeddedContainer = dataComponents.createInstanceContainer(
                                    item.getClass(), container, metaProperty.getName());
                            embeddedContainer.setItem(propertyValue);
                            createForm(getPropertyCaption(metaClass, metaProperty), embeddedContainer);
                        } else {
                            addField(container, form, metaProperty, isReadonly);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void init(Map<String, Object> params) {
        dataContext = (DataContext) params.getOrDefault("parentContext", dataComponents.createDataContext());
        autocommit = params.get("autocommit") != null ? (Boolean) params.get("autocommit") : true;
        parentProperty = (String) params.get("parentProperty");
    }

    private void setWindowCaption() {
        getWindow().setCaption(messages.getMessage(meta.getJavaClass(), meta.getName()));
    }

    private void getOrCreateNewItem(Map<String, Object> params) {
        item = (Entity) params.get("item");
        isNew = item == null || entityStates.isNew(item);
        meta = item != null ? metadata.getClass(item) : metadata.getSession().getClass((String) params.get("metaClass"));

        if (item == null) {
            item = metadata.create(meta);
            if (params.get("parent") != null && params.get("parentProperty") != null) {
                EntityValues.setValue(item, (String) params.get("parentProperty"), params.get("parent"));
            }
        } else {
            Object itemId = Id.of(item).getValue();
            if (!isNew) {
                item = loadSingleItem(meta, itemId, createView(meta));
            }
            if (item == null) {
                throw new EntityAccessException(meta, itemId);
            }
        }
        item = dataContext.merge(item);
    }

    /**
     * Loads single item by id.
     *
     * @param meta      item's meta class
     * @param id        item's id
     * @param fetchPlan fetchPlan
     * @return loaded item if found, null otherwise
     */
    protected Entity loadSingleItem(MetaClass meta, Object id, FetchPlan fetchPlan) {
        String primaryKeyName = metadataTools.getPrimaryKeyName(meta);
        if (primaryKeyName == null) {
            throw new IllegalStateException(String.format("Entity %s has no primary key", meta.getName()));
        }

        LoadContext ctx = new LoadContext(meta);
        ctx.setLoadDynamicAttributes(true);
        ctx.setSoftDeletion(false);
        ctx.setFetchPlan(fetchPlan);

        String query = String.format("select e from %s e where e.%s = :id", meta.getName(), primaryKeyName);
        LoadContext.Query q = ctx.setQueryString(query);
        q.setParameter("id", id);
        return dataManager.load(ctx);
    }

    /**
     * Creates a view, loading all the properties.
     * Referenced entities will be loaded with a LOCAL view.
     *
     * @param meta meta class
     * @return View instance
     */
    protected FetchPlan createView(MetaClass meta) {
        FetchPlan fetchPlan = new FetchPlan(meta.getJavaClass(), false);
        for (MetaProperty metaProperty : meta.getProperties()) {
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    fetchPlan.addProperty(metaProperty.getName());
                    break;
                case ASSOCIATION:
                case COMPOSITION:
                    MetaClass metaPropertyClass = metaProperty.getRange().asClass();
                    String metaPropertyClassName = metaPropertyClass.getName();

                    if (metadataTools.isEmbedded(metaProperty)) {
                        FetchPlan embeddedViewWithRelations = createEmbeddedPlan(metaPropertyClass);
                        fetchPlan.addProperty(metaProperty.getName(), embeddedViewWithRelations);
                    } else {
                        String viewName;
                        if (metaProperty.getRange().getCardinality().isMany()) {
                            viewName = FetchPlan.LOCAL;
                        } else {
                            viewName = FetchPlan.MINIMAL;
                        }
                        FetchPlan propView = fetchPlanRepository.getFetchPlan(metaPropertyClass, viewName);
                        fetchPlan.addProperty(metaProperty.getName(),
                                new FetchPlan(propView, metaPropertyClassName + ".entity-inspector-fetchPlan", true));
                    }
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }
        return fetchPlan;
    }

    protected FetchPlan createEmbeddedPlan(MetaClass metaPropertyClass) {
        FetchPlan propView = fetchPlanRepository.getFetchPlan(metaPropertyClass, FetchPlan.BASE);
        FetchPlan embeddedViewWithRelations = new FetchPlan(propView, metaPropertyClass.getName() + ".entity-inspector-view", true);

        // iterate embedded properties and add relations with MINIMAL view
        for (MetaProperty embeddedNestedProperty : metaPropertyClass.getProperties()) {
            if (embeddedNestedProperty.getRange().isClass() &&
                    !embeddedNestedProperty.getRange().getCardinality().isMany()) {
                FetchPlan embeddedRelationView = fetchPlanRepository.getFetchPlan(
                        embeddedNestedProperty.getRange().asClass(), FetchPlan.MINIMAL);

                embeddedViewWithRelations.addProperty(embeddedNestedProperty.getName(), embeddedRelationView);
            }
        }

        return embeddedViewWithRelations;
    }

    /**
     * Adds field to the specified field group.
     * If the field should be custom, adds it to the specified customFields collection
     * which can be used later to create fieldGenerators
     *
     * @param metaProperty meta property of the item's property which field is creating
     * @param form         field group to which created field will be added
     */
    protected void addField(InstanceContainer container, Form form, MetaProperty metaProperty, boolean isReadonly) {
        MetaClass metaClass = container.getEntityMetaClass();
        Range range = metaProperty.getRange();

        boolean isRequired = isRequired(metaProperty);
        if (!attrViewPermitted(metaClass, metaProperty))
            return;

        if ((range.isClass())
                && !entityOpPermitted(range.asClass(), EntityOp.READ))
            return;

        ValueSource valueSource = new ContainerValueSource<>(container, metaProperty.getName());

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(metaClass, metaProperty.getName());
        componentContext.setValueSource(valueSource);

        Field field = (Field) uiComponentsGenerator.generate(componentContext);

        if (requireTextArea(metaProperty, getEditedEntity(), MAX_TEXTFIELD_STRING_LENGTH)) {
            field = uiComponents.create(TextArea.NAME);
        }

        if (isBoolean(metaProperty)) {
            field = createBooleanField();
        }

        if (range.isClass()) {
            PickerField pickerField = uiComponents.create(PickerField.class);

            LookupAction lookupAction = actions.create(LookupAction.class);
            lookupAction.setScreenClass(EntityInspectorBrowser.class);
            lookupAction.setScreenOptionsSupplier(() -> getPropertyLookupOptions(metaProperty));
            lookupAction.setOpenMode(OpenMode.THIS_TAB);

            pickerField.addAction(lookupAction);
            pickerField.addAction(actions.create(ClearAction.class));

            field = pickerField;
        }

        field.setValueSource(valueSource);
        field.setCaption(getPropertyCaption(metaClass, metaProperty));
        field.setRequired(isRequired);

        isReadonly = isReadonly || metaProperty.getName().equals(parentProperty);
        if (range.isClass() && !metadataTools.isEmbedded(metaProperty)) {
            field.setEditable(metadataTools.isOwningSide(metaProperty) && !isReadonly);
        } else {
            field.setEditable(!isReadonly);
        }

        field.setWidth("400px");

        if (isRequired) {
            field.setRequiredMessage(messageTools.getDefaultRequiredMessage(metaClass, metaProperty.getName()));
        }
        form.add(field);
    }

    /**
     * Creates a table for the entities in ONE_TO_MANY or MANY_TO_MANY relation with the current one
     */
    protected void addTable(InstanceContainer parent, MetaProperty childMeta) {
        MetaClass meta = childMeta.getRange().asClass();

        //don't show empty table if the user don't have permissions on the attribute or the entity
        if (!attrViewPermitted(parent.getEntityMetaClass(), childMeta) ||
                !entityOpPermitted(meta, EntityOp.READ)) {
            return;
        }

        //don't show table on new master item, because an exception occurred on safe new item in table
        if (isNew && childMeta.getType().equals(ASSOCIATION)) {
            return;
        }

        //vertical box for the table and its label
        BoxLayout vbox = uiComponents.create(VBoxLayout.class);
        vbox.setSizeFull();
        CollectionLoader loader = dataComponents.createCollectionLoader();
        CollectionContainer container = dataComponents.createCollectionContainer(meta.getJavaClass(), parent, childMeta.getName());
        loader.setContainer(container);
//        TODO replace to query
        loader.setLoadDelegate(loadContext -> {
            Collection<?> value = EntityValues.getValue(parent.getItem(), childMeta.getName());
            return value != null ? new ArrayList<>(value) : new ArrayList<>();
        });

        Table<?> table = uiComponents.create(Table.NAME);
        table.setMultiSelect(true);

        //place non-system properties columns first
        List<Table.Column> nonSystemPropertyColumns = new ArrayList<>();
        List<Table.Column> systemPropertyColumns = new ArrayList<>();
        for (MetaProperty metaProperty : meta.getProperties()) {
            if (metaProperty.getRange().isClass() || isRelatedToNonLocalProperty(metaProperty))
                continue; // because we use local views
            Table.Column column = new Table.Column(meta.getPropertyPath(metaProperty.getName()));
            if (!metadataTools.isSystem(metaProperty)) {
                column.setCaption(getPropertyCaption(meta, metaProperty));
                nonSystemPropertyColumns.add(column);
            } else {
                column.setCaption(metaProperty.getName());
                systemPropertyColumns.add(column);
            }
            if (metaProperty.getJavaType().equals(String.class)) {
                column.setMaxTextLength(MAX_TEXT_LENGTH);
            }
        }
        for (Table.Column column : nonSystemPropertyColumns) {
            table.addColumn(column);
        }

        for (Table.Column column : systemPropertyColumns) {
            table.addColumn(column);
        }

        table.setItems(new ContainerTableItems(container));

        ButtonsPanel propertyButtonsPanel = createButtonsPanel(table, childMeta);
        table.setButtonsPanel(propertyButtonsPanel);

        //TODO supports paging
//        if (propertyDs instanceof CollectionDatasource.SupportsPaging) {
//            RowsCount rowsCount = uiComponents.create(RowsCount.class);
//            rowsCount.setDatasource(propertyDs);
//            table.setRowsCount(rowsCount);
//        }

        table.setWidth(FULL_SIZE);
        table.setHeight(FULL_SIZE);

        vbox.add(table);
        vbox.expand(table);
        vbox.setMargin(true);

        TabSheet.Tab tab = tablesTabSheet.addTab(childMeta.toString(), vbox);
        tab.setCaption(getPropertyCaption(parent.getEntityMetaClass(), childMeta));

        loader.load();
    }

    private Field createBooleanField() {
        LookupField field = uiComponents.create(LookupField.NAME);
        field.setOptionsMap(ParamsMap.of(
                messages.getMessage("trueString"), Boolean.TRUE,
                messages.getMessage("falseString"), Boolean.FALSE));
        field.setTextInputAllowed(false);
        return field;
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        String caption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (caption.length() < CAPTION_MAX_LENGTH)
            return caption;
        else
            return caption.substring(0, CAPTION_MAX_LENGTH);
    }


    /**
     * Determine whether the given metaProperty relates to at least one non local property
     */
    protected boolean isRelatedToNonLocalProperty(MetaProperty metaProperty) {
        MetaClass metaClass = metaProperty.getDomain();
        for (String relatedProperty : metadataTools.getRelatedProperties(metaProperty)) {
            //noinspection ConstantConditions
            if (metaClass.getProperty(relatedProperty).getRange().isClass()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a buttons panel managing table's content.
     *
     * @param table        table
     * @param metaProperty property representing table's data
     * @return buttons panel
     */
    protected ButtonsPanel createButtonsPanel(Table table, MetaProperty metaProperty) {
        ButtonsPanel propertyButtonsPanel = uiComponents.create(ButtonsPanel.class);

        propertyButtonsPanel.add(createButton(table, metaProperty));
        if (metaProperty.getType() == ASSOCIATION) {
            propertyButtonsPanel.add(addButton(table, metaProperty));
        }
        propertyButtonsPanel.add(editButton(table, metaProperty));
        propertyButtonsPanel.add(removeButton(table, metaProperty));
        return propertyButtonsPanel;
    }

    private Button addButton(Table table, MetaProperty metaProperty) {
        Button addButton = uiComponents.create(Button.class);
        AddAction addAction = createAddAction(table, metaProperty);
        addButton.setAction(addAction);
        table.addAction(addAction);
        addButton.setIcon(icons.get(JmixIcon.ADD_ACTION));
        return addButton;
    }

    private AddAction createAddAction(Table table, MetaProperty metaProperty) {
        AddAction addAction = actions.create(AddAction.class);
        addAction.setOpenMode(OpenMode.THIS_TAB);
        addAction.setTarget(table);
        addAction.setScreenClass(EntityInspectorBrowser.class);

        addAction.setScreenOptionsSupplier(() -> getPropertyLookupOptions(metaProperty));
        addAction.setShortcut(uiProperties.getTableAddShortcut());
        return addAction;
    }

    protected Object getPropertyLookupOptions(MetaProperty metaProperty) {
        Map<String, Object> params = new HashMap<>();
        params.put("entity", metaProperty.getRange().asClass().getName());
        MetaProperty inverseProperty = metaProperty.getInverse();
        if (inverseProperty != null) {
            params.put("parentProperty", inverseProperty.getName());
        }
        return new MapScreenOptions(params);
    }

    private Button createButton(Table table, MetaProperty metaProperty) {
        Button createButton = uiComponents.create(Button.class);
        CreateAction createAction = createCreateAction(table, metaProperty);
        createButton.setAction(createAction);
        table.addAction(createAction);
        createButton.setIcon(icons.get(JmixIcon.CREATE_ACTION));
        return createButton;
    }

    private CreateAction createCreateAction(Table table, MetaProperty metaProperty) {
        CreateAction createAction = actions.create(CreateAction.class);
        createAction.setOpenMode(OpenMode.THIS_TAB);
        createAction.setTarget(table);
        createAction.setScreenClass(EntityInspectorJmix.class);

        createAction.setScreenOptionsSupplier(() -> {
            Map<String, Object> editorParams = new HashMap<>();
            editorParams.put("metaClass", metaProperty.getRange().asClass().getName());
            editorParams.put("autocommit", Boolean.FALSE);
            MetaProperty inverseProperty = metaProperty.getInverse();
            if (inverseProperty != null) {
                editorParams.put("parentProperty", inverseProperty.getName());
            }
            editorParams.put("parent", getEditedEntity());
            editorParams.put("parentContext", dataContext);
//            if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
//                editorParams.put("parentContainer", container);
//            }
            return new MapScreenOptions(editorParams);

        });
        createAction.setShortcut(uiProperties.getTableInsertShortcut());
        return createAction;
    }

    private Button editButton(Table table, MetaProperty metaProperty) {
        Button editButton = uiComponents.create(Button.class);
        EditAction editAction = createEditAction(table, metaProperty);
        editButton.setAction(editAction);
        table.addAction(editAction);
        editButton.setIcon(icons.get(JmixIcon.EDIT_ACTION));
        return editButton;
    }

    private EditAction createEditAction(Table table, MetaProperty metaProperty) {
        EditAction editAction = actions.create(EditAction.class);
        editAction.setOpenMode(OpenMode.THIS_TAB);
        editAction.setTarget(table);
        editAction.setScreenClass(EntityInspectorJmix.class);

        editAction.setScreenOptionsSupplier(() -> {
            Map<String, Object> editorParams = new HashMap<>();
            editorParams.put("item", table.getSingleSelected());
            editorParams.put("autocommit", Boolean.FALSE);
            MetaProperty inverseProperty = metaProperty.getInverse();
            if (inverseProperty != null) {
                editorParams.put("parentProperty", inverseProperty.getName());
            }
            editorParams.put("parent", getEditedEntity());
            editorParams.put("parentContext", dataContext);
            return new MapScreenOptions(editorParams);

        });
        editAction.setShortcut(uiProperties.getTableInsertShortcut());
        return editAction;
    }

    private Button removeButton(Table table, MetaProperty metaProperty) {
        Button removeButton = uiComponents.create(Button.class);
        Action removeAction = createRemoveAction(table, metaProperty);
        removeButton.setAction(removeAction);
        table.addAction(removeAction);
        removeButton.setIcon(icons.get(JmixIcon.REMOVE_ACTION));
        removeButton.setCaption(messages.getMessage("remove"));
        return removeButton;
    }

    /**
     * Creates either Remove or Exclude action depending on property type
     */
    protected Action.HasTarget createRemoveAction(Table table, MetaProperty metaProperty) {
        Action.HasTarget result;
        switch (metaProperty.getType()) {
            case COMPOSITION:
                result = actions.create(RemoveAction.class);
                result.setTarget(table);
                break;
            case ASSOCIATION:
                result = actions.create(ExcludeAction.class);
                result.setTarget(table);
                break;
            default:
                throw new IllegalArgumentException("property must contain an entity");
        }
        result.setShortcut(uiProperties.getTableRemoveShortcut());
        return result;
    }

    public Entity getItem() {
        return getEditedEntity();
    }

    @Override
    public void setEntityToEdit(Entity entity) {
        item = entity;
    }

    @Override
    public Entity getEditedEntity() {
        return container.getItem();
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return dataContext != null && dataContext.isModified(item);
    }
}