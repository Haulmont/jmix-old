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

package io.jmix.ui.widget.client.split;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import io.jmix.ui.widget.client.placeholder.JmixPlaceHolderWidget;
import com.vaadin.client.ui.VSplitPanelHorizontal;

import java.util.function.Consumer;

public class JmixHorizontalSplitPanelWidget extends VSplitPanelHorizontal {
    /**
     * Styles for widget
     */
    protected static final String SP_DOCK_BUTTON = "c-splitpanel-dock-button";
    protected static final String SP_DOCK_BUTTON_LEFT = "c-splitpanel-dock-button-left";
    protected static final String SP_DOCK_BUTTON_RIGHT = "c-splitpanel-dock-button-right";
    protected static final String SP_DOCK_LEFT = "c-splitpanel-dock-left";
    protected static final String SP_DOCK_RIGHT = "c-splitpanel-dock-right";
    protected static final String SP_DOCKABLE_LEFT = "c-splitpanel-dockable-left";
    protected static final String SP_DOCKABLE_RIGHT = "c-splitpanel-dockable-right";

    protected static final int BUTTON_WIDTH_SPACE = 10;
    protected boolean reversed;
    protected boolean docked;

    protected int splitHeight;

    protected enum DockButtonState {
        LEFT,
        RIGHT
    }

    protected DockButtonState dockButtonState = DockButtonState.LEFT;

    protected SplitPanelDockMode dockMode = SplitPanelDockMode.LEFT;

    protected Consumer<String> beforeDockPositionHandler = null;

    protected String defaultPosition = null;
    protected String beforeDockPosition = null;

    private Element dockButtonContainer;
    private JmixPlaceHolderWidget dockButton;

    public boolean isDockable() {
        return dockButtonContainer != null;
    }

    public void setDockable(boolean dockable) {
        if (isDockable() == dockable) {
            return;
        }

        if (dockable) {
            dockButton = createDockButton();
            dockButtonContainer = createDockButtonContainer();

            add(dockButton, dockButtonContainer);
            splitter.getParentElement().appendChild(dockButtonContainer);

            updateDockButtonPosition();
        } else {
            if (dockButtonContainer != null) {
                dockButtonContainer.removeFromParent();

                dockButtonContainer = null;
                dockButton = null;
            }
        }
    }

    protected JmixPlaceHolderWidget createDockButton() {
        JmixPlaceHolderWidget dockBtn = new JmixPlaceHolderWidget();
        dockBtn.setStyleName(SP_DOCK_BUTTON);
        if (dockMode == SplitPanelDockMode.RIGHT) {
            dockBtn.addStyleName(SP_DOCK_BUTTON_RIGHT);
        } else {
            dockBtn.addStyleName(SP_DOCK_BUTTON_LEFT);
        }
        dockBtn.addDomHandler(
                event -> onDockButtonClick(),
                ClickEvent.getType());
        return dockBtn;
    }

    protected Element createDockButtonContainer() {
        Element dockBtnContainer = DOM.createDiv();
        dockBtnContainer.getStyle().setZIndex(101);
        dockBtnContainer.getStyle().setPosition(Style.Position.ABSOLUTE);

        if (dockMode == SplitPanelDockMode.LEFT) {
            dockBtnContainer.addClassName(SP_DOCK_LEFT);
        } else if (dockMode == SplitPanelDockMode.RIGHT) {
            dockBtnContainer.addClassName(SP_DOCK_RIGHT);
        }
        return dockBtnContainer;
    }

    private void onDockButtonClick() {
        String newPosition = position;
        boolean isDocked = false;

        if (dockMode == SplitPanelDockMode.LEFT) {
            if (dockButtonState == DockButtonState.LEFT) {
                defaultPosition = position;
                newPosition = "0px";
                isDocked = true;
            } else if (defaultPosition != null) {
                newPosition = defaultPosition;
            } else if (beforeDockPosition != null) {
                // apply last saved position if defaultPosition is null
                newPosition = beforeDockPosition;
            } else if (isSplitterInRightChangeArea()) {
                // splitter is placed in the absolute LEFT position and if we click on the dock button
                // it won't be replaced, because of defaultPosition and beforeDockPosition are null.
                // So we replace it to the absolute RIGHT position.
                defaultPosition = position;
                newPosition = reversed ? "0px" : getAbsoluteRight() + "px";
                isDocked = true;
            }
        } else if (dockMode == SplitPanelDockMode.RIGHT) {
            if (dockButtonState == DockButtonState.RIGHT) {
                defaultPosition = position;
                newPosition = reversed ? "0px" : getAbsoluteRight() + "px";
                isDocked = true;
            } else if (defaultPosition != null) {
                newPosition = defaultPosition;
            } else if (beforeDockPosition != null) {
                // apply last saved position if defaultPosition is null
                newPosition = beforeDockPosition;
            } else if (isSplitterInLeftChangeArea()) {
                // splitter is placed in the absolute RIGHT position and if we click on the dock button
                // it won't be replaced, because of defaultPosition and beforeDockPosition are null.
                // So we replace it to the absolute LEFT position.
                defaultPosition = position;
                newPosition = "0px";
                isDocked = true;
            }
        }

        // save last position before dock changes position
        beforeDockPositionHandler.accept(defaultPosition);
        setDocked(isDocked);
        setSplitPosition(newPosition);
        fireEvent(new SplitterMoveHandler.SplitterMoveEvent(this));
    }

    public void setDockMode(SplitPanelDockMode dockMode) {
        this.dockMode = dockMode;

        updateDockButtonPosition();
    }

    protected void updateDockButtonPosition() {
        if (isDockable()) {

            Style dockButtonStyle = dockButtonContainer.getStyle();
            if (dockMode == SplitPanelDockMode.LEFT) {
                int left = splitter.getOffsetLeft();
                if (left > BUTTON_WIDTH_SPACE) {
                    dockButtonStyle.setLeft(left - (dockButton.getOffsetWidth() - getSplitterSize()), Style.Unit.PX);
                    dockButtonStyle.setTop(getDockBtnContainerVerticalPosition(), Style.Unit.PX);

                    if (dockButtonState == DockButtonState.RIGHT) {
                        updateDockButtonStyle(SP_DOCK_BUTTON_LEFT, SP_DOCK_BUTTON_RIGHT);
                        dockButtonState = DockButtonState.LEFT;
                    }

                    updateSplitPanelStyle(SP_DOCKABLE_LEFT, SP_DOCKABLE_RIGHT);
                } else {
                    dockButtonStyle.setLeft(left, Style.Unit.PX);
                    dockButtonStyle.setTop(getDockBtnContainerVerticalPosition(), Style.Unit.PX);

                    if (dockButtonState == DockButtonState.LEFT) {
                        updateDockButtonStyle(SP_DOCK_BUTTON_RIGHT, SP_DOCK_BUTTON_LEFT);
                        dockButtonState = DockButtonState.RIGHT;
                    }

                    updateSplitPanelStyle(SP_DOCKABLE_RIGHT, SP_DOCKABLE_LEFT);
                }
            } else if (dockMode == SplitPanelDockMode.RIGHT) {
                int right = splitter.getOffsetLeft() + splitter.getOffsetWidth();
                int splitRightPosition = getAbsoluteRight();

                if (right < splitRightPosition - BUTTON_WIDTH_SPACE) {
                    dockButtonStyle.setLeft(right - getSplitterSize(), Style.Unit.PX);
                    dockButtonStyle.setTop(getDockBtnContainerVerticalPosition(), Style.Unit.PX);

                    if (dockButtonState == DockButtonState.LEFT) {
                        updateDockButtonStyle(SP_DOCK_BUTTON_RIGHT, SP_DOCK_BUTTON_LEFT);
                        dockButtonState = DockButtonState.RIGHT;
                    }

                    updateSplitPanelStyle(SP_DOCKABLE_RIGHT, SP_DOCKABLE_LEFT);
                } else {
                    dockButtonStyle.setLeft(right - (dockButton.getOffsetWidth()), Style.Unit.PX);
                    dockButtonStyle.setTop(getDockBtnContainerVerticalPosition(), Style.Unit.PX);

                    if (dockButtonState == DockButtonState.RIGHT) {
                        updateDockButtonStyle(SP_DOCK_BUTTON_LEFT, SP_DOCK_BUTTON_RIGHT);
                        dockButtonState = DockButtonState.LEFT;
                    }

                    updateSplitPanelStyle(SP_DOCKABLE_LEFT, SP_DOCKABLE_RIGHT);
                }
            }
        }
    }

    protected boolean isSplitterInRightChangeArea() {
        int left = splitter.getOffsetLeft();
        return left < getAbsoluteLeft() + BUTTON_WIDTH_SPACE;
    }

    protected boolean isSplitterInLeftChangeArea() {
        int right = splitter.getOffsetLeft() + splitter.getOffsetWidth();
        int splitRightPosition = getAbsoluteLeft() + getAbsoluteRight();
        return right > splitRightPosition - BUTTON_WIDTH_SPACE;
    }

    private void updateDockButtonStyle(String newStyle, String oldStyle) {
        dockButton.removeStyleName(oldStyle);
        dockButton.addStyleName(newStyle);
    }

    private void updateSplitPanelStyle(String newStyle, String oldStyle) {
        this.removeStyleName(oldStyle);
        this.addStyleName(newStyle);
    }

    private int getDockBtnContainerVerticalPosition() {
        return splitHeight / 2 - dockButton.getOffsetHeight() / 2;
    }

    public int getAbsoluteRight() {
        return getOffsetWidth();
    }

    @Override
    public void updateSizes() {
        super.updateSizes();

        splitHeight = splitter.getOffsetHeight();

        if (isAttached()) {
            updateDockButtonPosition();
        }
    }

    @Override
    public void onMouseDown(Event event) {
        if (isDockable()
                && isDocked()
                && (isMinPositionSet() || isMaxPositionSet())) {
            return;
        }
        super.onMouseDown(event);
    }

    @Override
    public void setSplitPosition(String pos) {
        if (isDockable()
                && isDocked()
                && !isExtremePosition(pos)) {
            setDocked(false);
        }
        super.setSplitPosition(pos);
    }

    protected boolean isMinPositionSet() {
        return dockMode == SplitPanelDockMode.LEFT
                && !minimumPosition.equals("0%")
                && !minimumPosition.equals("0px");
    }

    protected boolean isMaxPositionSet() {
        return dockMode == SplitPanelDockMode.RIGHT
                && !maximumPosition.equals("100%")
                && !maximumPosition.equals(getAbsoluteRight() + "px");
    }

    protected boolean isExtremePosition(String pos) {
        boolean dockedToLeft = dockMode == SplitPanelDockMode.LEFT
                && (pos.equals("0%") || pos.equals("0px"));
        boolean dockedToRight = dockMode == SplitPanelDockMode.RIGHT
                && (pos.equals("100%") || pos.equals(getAbsoluteRight() + "px"));
        return dockedToLeft || dockedToRight;
    }

    @Override
    public void onMouseUp(Event event) {
        super.onMouseUp(event);

        splitHeight = splitter.getOffsetHeight();
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        if (dockButtonContainer != null) {
            dockButtonContainer.removeFromParent();
        }
    }

    @Override
    protected String checkSplitPositionLimits(String pos) {
        return isDocked() ? pos : super.checkSplitPositionLimits(pos);
    }

    @Override
    public void setPositionReversed(boolean reversed) {
        super.setPositionReversed(reversed);

        this.reversed = reversed;
    }

    protected void setDocked(boolean docked) {
        if (this.docked != docked) {
            this.docked = docked;
        }
    }

    protected boolean isDocked() {
        return docked;
    }
}
