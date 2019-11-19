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

package io.jmix.ui.web.components.composite

import io.jmix.ui.ScreenBuilders
import io.jmix.ui.components.Button
import io.jmix.ui.model.InstanceContainer
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.screen.UiControllerUtils
import io.jmix.ui.xml.layout.LayoutLoaderConfig
import com.haulmont.cuba.web.gui.WebUiComponents
import com.haulmont.cuba.web.testmodel.sales.OrderLine
import spec.cuba.web.UiScreenSpec
import spec.cuba.web.components.composite.components.comments.TestCommentaryPanel
import spec.cuba.web.components.composite.components.comments.TestCommentaryPanelLoader
import spec.cuba.web.components.composite.components.comments.TestProgrammaticCommentaryPanel
import spec.cuba.web.components.composite.components.stepper.TestStepperField
import spec.cuba.web.components.composite.components.stepper.TestStepperFieldLoader
import spec.cuba.web.components.composite.screens.CommentScreen
import spec.cuba.web.components.composite.screens.CompositeFieldScreen

@SuppressWarnings("GroovyAssignabilityCheck")
class CompositeComponentTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.cuba.web.components.composite.screens', 'io.jmix.ui.app.main'])

        // Register composite components
        ((WebUiComponents) uiComponents).register(TestStepperField.NAME, TestStepperField)
        ((WebUiComponents) uiComponents).register(TestCommentaryPanel.NAME, TestCommentaryPanel)
        ((WebUiComponents) uiComponents).register(TestProgrammaticCommentaryPanel.NAME, TestProgrammaticCommentaryPanel)

        // Register composite component loaders
        def layoutLoaderConfig = cont.getBean(LayoutLoaderConfig)
        layoutLoaderConfig.registerLoader("testStepperField", TestStepperFieldLoader)
        layoutLoaderConfig.registerLoader("testCommentaryPanel", TestCommentaryPanelLoader)
    }

    def "composite component as field in an editor screen and relative path to descriptor"() {
        def screens = vaadinUi.screens
        def screenBuilders = cont.getBean(ScreenBuilders)

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def compositeFieldScreen = screenBuilders.editor(OrderLine, mainWindow)
                .withScreenClass(CompositeFieldScreen)
                .newEntity()
                .build()
        compositeFieldScreen.show()

        when:

        TestStepperField quantityField =
                compositeFieldScreen.getWindow().getComponentNN("quantityField") as TestStepperField

        then: "quantityField is loaded"

        quantityField != null

        when: "Set value to item"

        InstanceContainer<OrderLine> lineDc =
                UiControllerUtils.getScreenData(compositeFieldScreen).getContainer("lineDc")
        lineDc.getItem().setQuantity(10)

        then: "Composite component value is changed respectively"

        quantityField.getValue() == 10

        when: "Set value to composite component"

        quantityField.setValue(20)

        then: "Item value is changed respectively"

        lineDc.getItem().getQuantity() == 20
    }

    def "composite component containing a DataGrid with MetaClass and full path to descriptor"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def commentsScreen = screens.create(CommentScreen)
        commentsScreen.show()

        when:

        TestCommentaryPanel commentaryPanel =
                commentsScreen.getWindow().getComponentNN("commentaryPanel") as TestCommentaryPanel

        then: "commentaryPanel is loaded"

        commentaryPanel != null

        when:

        Button sendBtn = commentaryPanel.getComposition().getComponentNN("sendBtn") as Button

        then: "Button caption is localized"

        sendBtn.getCaption() == "Send"
    }

    def "composite component with programmatic creation of nested components"() {

        when:

        TestProgrammaticCommentaryPanel commentaryPanel = uiComponents.create(TestProgrammaticCommentaryPanel.NAME)

        then: "commentaryPanel is created"

        commentaryPanel != null

        when:

        Button sendBtn = commentaryPanel.getComposition().getComponentNN("sendBtn") as Button

        then: "Button caption is localized"

        sendBtn.getCaption() == "Send"
    }
}
