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

package spec.haulmont.cuba.web.timer

import io.jmix.ui.app.main.MainScreen
import io.jmix.ui.component.Timer
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.widget.JmixTimer
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.timer.screens.FragmentWithTimer
import spec.haulmont.cuba.web.timer.screens.ScreenWithNestedFragment
import spec.haulmont.cuba.web.timer.screens.ScreenWithTimer

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck", "GroovyPointlessBoolean"])
class TimerFacetTest extends UiScreenSpec {

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.timer.screens', 'com.haulmont.cuba.web.app.main'])
    }

    def "open screen with Timer facet"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create(MainScreen, OpenMode.ROOT)
        screens.show(mainWindow)

        when:

        def screen = screens.create(ScreenWithTimer)
        screen.show()

        then:

        screen.window.getFacet('testTimer') instanceof Timer
        screen.timer != null
        screen.timer.repeating == true
        screen.timer.delay == 2000
        screen.window.facets.count() == 1

        when:

        def impl = screen.timer.timerImpl as JmixTimer
        impl.actionListeners.get(0).accept(impl)
        impl.stopListeners.get(0).accept(impl)

        then:

        screen.ticksCounter == 1
        screen.stopped == true
    }

    def "open fragment with Timer facet"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create(MainScreen, OpenMode.ROOT)
        screens.show(mainWindow)

        when:

        def screen = screens.create(ScreenWithNestedFragment)
        screen.show()

        then:

        screen.testFragment.getFacet('testTimer') instanceof Timer
        def fragmentWithTimer = screen.testFragment.frameOwner as FragmentWithTimer
        fragmentWithTimer.timer != null

        when:

        def timer = fragmentWithTimer.timer
        def impl = timer.timerImpl as JmixTimer
        impl.actionListeners.get(0).accept(impl)
        impl.stopListeners.get(0).accept(impl)

        then:

        fragmentWithTimer.ticksCounter == 1
        fragmentWithTimer.stopped == true
    }
}