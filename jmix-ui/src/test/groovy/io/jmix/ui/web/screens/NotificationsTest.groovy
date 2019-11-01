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

package io.jmix.ui.web.screens

import com.haulmont.cuba.gui.components.ContentMode
import com.haulmont.cuba.gui.executors.BackgroundWorker
import com.haulmont.cuba.web.AppUI
import com.haulmont.cuba.web.sys.WebNotifications
import com.vaadin.shared.Position
import com.vaadin.ui.Notification
import spock.lang.Specification

@SuppressWarnings(["GroovyPointlessBoolean", "GroovyAccessibility"])
class NotificationsTest extends Specification {

    def "Notification can be show"() {
        def ui = new AppUI()
        def notifications = new WebNotifications(ui)
        notifications.backgroundWorker = Mock(BackgroundWorker)

        when:

        def notification = notifications.create()

        then:

        notification != null

        when:

        notification
                .withCaption('Greeting')
                .withDescription('Hello world')
                .withPosition(com.haulmont.cuba.gui.Notifications.Position.BOTTOM_CENTER)
                .withContentMode(ContentMode.HTML)
                .withType(com.haulmont.cuba.gui.Notifications.NotificationType.WARNING)
                .withStyleName('open-notification')

        then:

        notification.caption == 'Greeting'
        notification.description == 'Hello world'
        notification.position == com.haulmont.cuba.gui.Notifications.Position.BOTTOM_CENTER
        notification.type == com.haulmont.cuba.gui.Notifications.NotificationType.WARNING
        notification.contentMode == ContentMode.HTML
        notification.styleName == 'open-notification'

        when:

        notification.show()
        def extensions = ui.getExtensions()
        def vNotification = extensions.find { it instanceof Notification } as Notification

        then:

        vNotification != null
        vNotification.caption == 'Greeting'
        vNotification.description == 'Hello world'
        vNotification.position == Position.BOTTOM_CENTER
        vNotification.styleName == 'open-notification'
        vNotification.htmlContentAllowed == true
    }

    def "Notification does not support ContentMode.PREFORMATTED"() {
        def notifications = new WebNotifications(new AppUI())
        notifications.backgroundWorker = Mock(BackgroundWorker)

        when:

        def notification = notifications.create()
        notification.withContentMode(ContentMode.PREFORMATTED)

        then:

        thrown UnsupportedOperationException
    }
}