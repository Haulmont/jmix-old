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

package io.jmix.ui.component.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.PopupView;
import io.jmix.ui.widget.JmixPopupView;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

public class WebPopupView extends WebAbstractComponent<JmixPopupView> implements PopupView {
    protected Component popupContent;
    protected String minimizedValue;

    protected Registration popupVisibilityListenerRegistration;

    public WebPopupView() {
        component = new JmixPopupView(new EmptyContent());
    }

    @Override
    public Subscription addPopupVisibilityListener(Consumer<PopupVisibilityEvent> listener) {
        getEventHub().subscribe(PopupVisibilityEvent.class, listener);

        if (popupVisibilityListenerRegistration == null) {
            popupVisibilityListenerRegistration = component.addPopupVisibilityListener(e ->
                    publish(PopupVisibilityEvent.class, new PopupVisibilityEvent(this))
            );
        }
        return () -> removePopupVisibilityListener(listener);
    }

    @Override
    public void removePopupVisibilityListener(Consumer<PopupVisibilityEvent> listener) {
        unsubscribe(PopupVisibilityEvent.class, listener);

        if (!hasSubscriptions(PopupVisibilityEvent.class)) {
            popupVisibilityListenerRegistration.remove();
        }
    }

    @Override
    public void setPopupContent(Component popupContent) {
        if (this.popupContent != null) {
            if (this.popupContent instanceof BelongToFrame) {
                ((BelongToFrame) this.popupContent).setFrame(null);
            } else {
                detachFromFrame(this.popupContent);
            }
            this.popupContent.setParent(null);
        }

        this.popupContent = popupContent;

        if (popupContent != null) {
            component.setContent(new PopupContent());

            if (frame != null) {
                if (popupContent instanceof BelongToFrame
                        && ((BelongToFrame) popupContent).getFrame() == null) {
                    ((BelongToFrame) popupContent).setFrame(frame);
                } else {
                    attachToFrame(popupContent);
                }
            }
            popupContent.setParent(this);
        } else {
            component.setContent(new EmptyContent());
        }
    }

    protected void attachToFrame(Component childComponent) {
        ((FrameImplementation) frame).registerComponent(childComponent);
    }

    protected void detachFromFrame(Component childComponent) {
        ((FrameImplementation) frame).unregisterComponent(childComponent);
    }

    @Override
    public void setFrame(Frame frame) {
        super.setFrame(frame);

        if (popupContent != null && frame != null) {
            if (popupContent instanceof BelongToFrame
                    && ((BelongToFrame) popupContent).getFrame() == null) {
                ((BelongToFrame) popupContent).setFrame(frame);
            } else {
                attachToFrame(popupContent);
            }
        }
    }

    @Override
    public Component getPopupContent() {
        return popupContent;
    }

    @Override
    public void setPopupVisible(boolean value) {
        component.setPopupVisible(value);
        component.markAsDirty();
    }

    @Override
    public void setHideOnMouseOut(boolean value) {
        component.setHideOnMouseOut(value);
        component.markAsDirty();
    }

    @Override
    public boolean isHideOnMouseOut() {
        return component.isHideOnMouseOut();
    }

    @Override
    public boolean isPopupVisible() {
        return component.isPopupVisible();
    }

    @Override
    public void setMinimizedValue(String minimizedValue) {
        this.minimizedValue = minimizedValue;
        component.markAsDirty();
    }

    @Override
    public String getMinimizedValue() {
        return minimizedValue;
    }

    @Override
    public void setCaptionAsHtml(boolean value) {
        component.setCaptionAsHtml(value);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return component.isCaptionAsHtml();
    }

    @Override
    public void setPopupPosition(int top, int left) {
        component.setPopupPosition(top, left);
    }

    @Override
    public void setPopupPositionTop(int top) {
        component.setPopupPositionTop(top);
    }

    @Override
    public int getPopupPositionTop() {
        return component.getPopupPositionTop();
    }

    @Override
    public void setPopupPositionLeft(int left) {
        component.setPopupPositionLeft(left);
    }

    @Override
    public int getPopupPositionLeft() {
        return component.getPopupPositionLeft();
    }

    @Override
    public void setPopupPosition(PopupPosition position) {
        component.setPopupPosition(WebWrapperUtils.toVaadinPopupPosition(position));
    }

    @Override
    public PopupPosition getPopupPosition() {
        return WebWrapperUtils.fromVaadinPopupPosition(component.getPopupPosition());
    }

    protected class EmptyContent implements com.vaadin.ui.PopupView.Content {
        private Label label = new Label("");

        @Override
        public String getMinimizedValueAsHTML() {
            return minimizedValue;
        }

        @Override
        public com.vaadin.ui.Component getPopupComponent() {
            return label;
        }
    }

    protected class PopupContent implements com.vaadin.ui.PopupView.Content {
        @Override
        public final com.vaadin.ui.Component getPopupComponent() {
            return WebComponentsHelper.getComposition(popupContent);
        }

        @Override
        public final String getMinimizedValueAsHTML() {
            return minimizedValue;
        }
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        setIconStyle();
    }

    @Override
    public void setIcon(String icon) {
        super.setIcon(icon);
        setIconStyle();
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);
        setIconStyle();
    }

    protected void setIconStyle() {
        if (StringUtils.isNotEmpty(getIcon()) && StringUtils.isEmpty(getCaption())) {
            getComposition().addStyleName("popupview-icon-on-left");
        } else {
            getComposition().removeStyleName("popupview-icon-on-left");
        }
    }
}
