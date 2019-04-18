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

package io.jmix.core.security.impl;

import io.jmix.core.compatibility.AppContext;
import io.jmix.core.security.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Locale;
import java.util.UUID;

@Component
public class UserSessionSourceImpl implements UserSessionSource {

    @Inject
    protected UserSessions userSessions;

    @Override
    public boolean checkCurrentUserSession() {
        SecurityContext securityContext = AppContext.getSecurityContext();
        if (securityContext == null) {
            return false;
        }
        if (securityContext.getSession().isSystem()) {
            return true;
        }
        UserSession session = userSessions.getAndRefresh(securityContext.getSession().getId());
        return session != null;
    }

    @Override
    public UserSession getUserSession() throws NoUserSessionException {
        SecurityContext securityContext = AppContext.getSecurityContextNN();
        if (securityContext.getSession().isSystem()) {
            return securityContext.getSession();
        }

        UserSession session = userSessions.getAndRefresh(securityContext.getSession().getId());
        if (session == null) {
            throw new NoUserSessionException(securityContext.getSession().getId());
        }
        return session;
    }

    @Override
    public UUID currentOrSubstitutedUserId() {
        // todo user substitution
        return getUserSession().getUser().getId();
    }

    @Override
    public Locale getLocale() {
        // todo user session locale
        return null;
    }
}