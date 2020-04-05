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

package io.jmix.ui.navigation;

import io.jmix.ui.AppUI;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.components.RootWindow;
import io.jmix.ui.components.Window;
import io.jmix.ui.navigation.accessfilter.NavigationFilter;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.util.OperationResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Bean that handles URL history transitions.
 */
@Component(HistoryNavigator.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HistoryNavigator {

    public static final String NAME = "jmix_HistoryNavigator";

    private static final Logger log = LoggerFactory.getLogger(HistoryNavigator.class);

    @Inject
    protected UrlTools urlTools;

    protected final AppUI ui;

    protected final UrlChangeHandler urlChangeHandler;
    protected final History history;

    public HistoryNavigator(AppUI ui, UrlChangeHandler urlChangeHandler) {
        this.ui = ui;
        this.urlChangeHandler = urlChangeHandler;
        this.history = ui.getHistory();
    }

    protected boolean handleHistoryNavigation(NavigationState requestedState) {
        boolean backward = history.searchBackward(requestedState);
        if (backward) {
            handleHistoryBackward(requestedState);
        }
        return backward;
    }

    protected void handleHistoryBackward(NavigationState requestedState) {
        NavigationState currentState = history.getNow();

        NavigationState previousState = findPreviousState(requestedState);
        if (previousState == null) {
            urlChangeHandler.revertNavigationState();
            return;
        }

        NavigationFilter.AccessCheckResult accessCheckResult = urlChangeHandler.navigationAllowed(previousState);
        if (accessCheckResult.isRejected()) {
            if (StringUtils.isNotEmpty(accessCheckResult.getMessage())) {
                urlChangeHandler.showNotification(accessCheckResult.getMessage());
            }
            urlChangeHandler.revertNavigationState();
            return;
        }

        if (urlChangeHandler.isRootState(previousState)) {
            handleRootBackNavigation(previousState);
        } else {
            handleScreenBackNavigation(currentState, previousState);
        }
    }

    protected void handleScreenBackNavigation(NavigationState currentState, NavigationState previousState) {
        Screen lastOpenedScreen = urlChangeHandler.findActiveScreenByState(currentState);
        if (lastOpenedScreen != null
                && urlChangeHandler.isNotCloseable(lastOpenedScreen.getWindow())) {
            urlChangeHandler.revertNavigationState();
            return;
        }

        if (lastOpenedScreen != null) {
            OperationResult screenCloseResult = lastOpenedScreen.getWindow()
                    .getFrameOwner()
                    .close(FrameOwner.WINDOW_CLOSE_ACTION)
                    .then(() -> proceedHistoryBackward(previousState));

            if (OperationResult.Status.FAIL == screenCloseResult.getStatus()
                    || OperationResult.Status.UNKNOWN == screenCloseResult.getStatus()) {
                urlChangeHandler.revertNavigationState();
            }
        } else {
            proceedHistoryBackward(previousState);
        }
    }

    protected void handleRootBackNavigation(NavigationState previousState) {
        WindowInfo rootWindowInfo = urlChangeHandler.windowConfig.findWindowInfoByRoute(previousState.getRoot());
        if (rootWindowInfo == null) {
            log.debug("Unable to find registered root screen with route: '{}'", previousState.getRoot());
            urlChangeHandler.revertNavigationState();
            return;
        }

        Class<? extends FrameOwner> requestedScreenClass = rootWindowInfo.getControllerClass();

        RootWindow topLevelWindow = AppUI.getCurrent().getTopLevelWindow();
        Class<? extends FrameOwner> currentScreenClass = topLevelWindow != null
                ? topLevelWindow.getFrameOwner().getClass()
                : null;

        if (currentScreenClass != null
                && requestedScreenClass.isAssignableFrom(currentScreenClass)) {

            if (Window.HasWorkArea.class.isAssignableFrom(requestedScreenClass)) {
                if (closeWorkAreaScreens()) {
                    history.backward();
                }
            } else {
                history.backward();
            }
        } else {
            urlChangeHandler.getScreenNavigator()
                    .handleScreenNavigation(previousState);
            /*
             * Since back navigation from one root screen to another root screen
             * can be performed only via screen opening we have to trigger history
             * back twice.
             */
            history.backward();
            history.backward();
        }
    }

    protected boolean closeWorkAreaScreens() {
        for (Screens.WindowStack windowStack : urlChangeHandler.getOpenedScreens().getWorkAreaStacks()) {
            if (!urlChangeHandler.closeWindowStack(windowStack)) {
                urlChangeHandler.revertNavigationState();
                return false;
            }
        }
        return true;
    }

    protected void proceedHistoryBackward(NavigationState requestedState) {
        Screen screen = urlChangeHandler.findActiveScreenByState(requestedState);
        urlChangeHandler.selectScreen(screen);

        urlTools.replaceState(requestedState.asRoute(), ui);

        history.backward();
    }

    protected NavigationState findPreviousState(NavigationState requestedState) {
        if (urlChangeHandler.isRootState(requestedState)) {
            return requestedState;
        }

        if (Objects.equals(requestedState, history.getNow())) {
            requestedState = history.getPrevious();
        }

        NavigationState prevState;
        Screen prevStateScreen = urlChangeHandler.findScreenByState(requestedState);

        if (prevStateScreen == null
                && !urlChangeHandler.isRootState(requestedState)) {

            while (history.getPrevious() != null) {
                history.backward();
                NavigationState previousState = history.getPrevious();

                if (urlChangeHandler.findActiveScreenByState(previousState) != null
                        || urlChangeHandler.isRootState(previousState)) {
                    break;
                }
            }

            prevState = history.getPrevious();
        } else {
            prevState = requestedState;
        }

        return prevState;
    }
}
