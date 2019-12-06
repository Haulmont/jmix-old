/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.widgets.client.appui;

import io.jmix.ui.widgets.CubaUI;
import io.jmix.ui.widgets.client.clientmanager.CubaUIClientRpc;
import io.jmix.ui.widgets.client.tooltip.CubaTooltip;
import io.jmix.ui.widgets.client.ui.CubaUIConstants;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.UIDL;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.VNotification;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.ui.UIConstants;
import elemental.client.Browser;
import elemental.html.History;

import java.util.Map;

@Connect(CubaUI.class)
public class CubaUIConnector extends UIConnector {

    public CubaUIConnector() {
        VNotification.setRelativeZIndex(true);

        //noinspection Convert2Lambda
        registerRpc(CubaUIClientRpc.class, new CubaUIClientRpc() {
            @Override
            public void updateSystemMessagesLocale(Map<String, String> localeMap) {
                ApplicationConfiguration conf = getConnection().getConfiguration();
                ApplicationConfiguration.ErrorMessage communicationError = conf.getCommunicationError();
                communicationError.setCaption(localeMap.get(CubaUIClientRpc.COMMUNICATION_ERROR_CAPTION_KEY));
                communicationError.setMessage(localeMap.get(CubaUIClientRpc.COMMUNICATION_ERROR_MESSAGE_KEY));

                ApplicationConfiguration.ErrorMessage authError = conf.getAuthorizationError();
                authError.setCaption(localeMap.get(CubaUIClientRpc.AUTHORIZATION_ERROR_CAPTION_KEY));
                authError.setMessage(localeMap.get(CubaUIClientRpc.AUTHORIZATION_ERROR_MESSAGE_KEY));

                ApplicationConfiguration.ErrorMessage sessionExpiredError = conf.getSessionExpiredError();
                sessionExpiredError.setCaption(localeMap.get(CubaUIClientRpc.SESSION_EXPIRED_ERROR_CAPTION_KEY));
                sessionExpiredError.setMessage(localeMap.get(CubaUIClientRpc.SESSION_EXPIRED_ERROR_MESSAGE_KEY));
            }
        });
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.isInitialStateChange()) {
            // check mode of required indicator icon/hidden
            // performed on page open or full refresh
            CubaTooltip.checkRequiredIndicatorMode();
        }
    }

    @Override
    protected void updateBrowserHistory(UIDL uidl) {
        String lastHistoryOp = uidl.getStringAttribute(CubaUIConstants.LAST_HISTORY_OP);

        History history = Browser.getWindow().getHistory();
        String pageTitle = getState().pageState.title;

        String replace = uidl.getStringAttribute(UIConstants.ATTRIBUTE_REPLACE_STATE);
        String push = uidl.getStringAttribute(UIConstants.ATTRIBUTE_PUSH_STATE);

        if (CubaUIConstants.HISTORY_PUSH_OP.equals(lastHistoryOp)) {
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_REPLACE_STATE)) {
                history.replaceState(null, pageTitle, replace);
            }
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_PUSH_STATE)) {
                history.pushState(null, pageTitle, push);
            }
        } else {
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_PUSH_STATE)) {
                history.pushState(null, pageTitle, push);
            }
            if (uidl.hasAttribute(UIConstants.ATTRIBUTE_REPLACE_STATE)) {
                history.replaceState(null, pageTitle, replace);
            }
        }
    }
}
