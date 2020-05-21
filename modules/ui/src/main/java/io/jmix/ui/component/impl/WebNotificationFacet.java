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

package io.jmix.ui.component.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.NotificationFacet;
import io.jmix.ui.screen.UiControllerUtils;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class WebNotificationFacet extends WebAbstractFacet implements NotificationFacet {

    protected String caption;
    protected String description;
    protected int delayMs = Notifications.DELAY_DEFAULT;
    protected String styleName;
    protected Notifications.NotificationType type = Notifications.NotificationType.HUMANIZED;
    protected ContentMode contentMode = ContentMode.TEXT;
    protected Notifications.Position position = Notifications.Position.DEFAULT;

    protected Supplier<String> captionProvider;
    protected Supplier<String> descriptionProvider;

    protected String actionId;
    protected String buttonId;

    protected boolean htmlSanitizerEnabled;

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setType(Notifications.NotificationType type) {
        this.type = type;
    }

    @Override
    public Notifications.NotificationType getType() {
        return type;
    }

    @Override
    public void setDelay(int delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public int getDelay() {
        return delayMs;
    }

    @Override
    public void setContentMode(ContentMode contentMode) {
        this.contentMode = contentMode;
    }

    @Override
    public ContentMode getContentMode() {
        return contentMode;
    }

    @Override
    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    @Override
    public String getStyleName() {
        return styleName;
    }

    @Override
    public void setPosition(Notifications.Position position) {
        this.position = position;
    }

    @Override
    public Notifications.Position getPosition() {
        return position;
    }

    @Override
    public Subscription addCloseListener(Consumer<CloseEvent> listener) {
        return getEventHub().subscribe(CloseEvent.class, listener);
    }

    @Override
    public void setCaptionProvider(Supplier<String> captionProvider) {
        this.captionProvider = captionProvider;
    }

    @Override
    public Supplier<String> getCaptionProvider() {
        return captionProvider;
    }

    @Override
    public void setDescriptionProvider(Supplier<String> descriptionProvider) {
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    public Supplier<String> getDescriptionProvider() {
        return descriptionProvider;
    }

    @Override
    public String getActionTarget() {
        return actionId;
    }

    @Override
    public void setActionTarget(String actionId) {
        this.actionId = actionId;
    }

    @Override
    public String getButtonTarget() {
        return buttonId;
    }

    @Override
    public void setButtonTarget(String buttonId) {
        this.buttonId = buttonId;
    }

    @Override
    public void setHtmlSanitizerEnabled(boolean htmlSanitizerEnabled) {
        this.htmlSanitizerEnabled = htmlSanitizerEnabled;
    }

    @Override
    public boolean isHtmlSanitizerEnabled() {
        return htmlSanitizerEnabled;
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        subscribe();
    }

    @Override
    public void show() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        Notifications notifications = UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getNotifications();

        String caption = this.caption;
        if (captionProvider != null) {
            caption = captionProvider.get();
        }

        String description = this.description;
        if (descriptionProvider != null) {
            description = descriptionProvider.get();
        }

        notifications.create(type)
                .withCaption(caption)
                .withDescription(description)
                .withHideDelayMs(delayMs)
                .withContentMode(contentMode)
                .withHtmlSanitizer(htmlSanitizerEnabled)
                .withStyleName(styleName)
                .withPosition(position)
                .withCloseListener(e -> publish(CloseEvent.class, new CloseEvent(this)))
                .show();
    }

    protected void subscribe() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        if (isNotEmpty(actionId)
                && isNotEmpty(buttonId)) {
            throw new GuiDevelopmentException(
                    "Notification should have either action or button target", owner.getId());
        }

        if (isNotEmpty(actionId)) {
            subscribeOnAction(owner);
        } else if (isNotEmpty(buttonId)) {
            subscribeOnButton(owner);
        }
    }

    protected void subscribeOnAction(Frame owner) {
        Action action = WebComponentsHelper.findAction(owner, actionId);

        if (!(action instanceof BaseAction)) {
            throw new GuiDevelopmentException(
                    String.format("Unable to find Notification target action with id '%s'", actionId),
                    owner.getId());
        }

        ((BaseAction) action).addActionPerformedListener(e ->
                show());
    }

    protected void subscribeOnButton(Frame owner) {
        Component component = owner.getComponent(buttonId);

        if (!(component instanceof Button)) {
            throw new GuiDevelopmentException(
                    String.format("Unable to find Notification target button with id '%s'", buttonId),
                    owner.getId());
        }

        ((Button) component).addClickListener(e ->
                show());
    }
}
