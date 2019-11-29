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

package io.jmix.ui.sys.linkhandling;

import io.jmix.core.*;
import io.jmix.core.compatibility.EntityLoadInfo;
import io.jmix.core.entity.Entity;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.ui.App;
import io.jmix.ui.NoSuchScreenException;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.components.compatibility.WindowManager.OpenType;
import io.jmix.ui.exception.AccessDeniedHandler;
import io.jmix.ui.exception.EntityAccessExceptionHandler;
import io.jmix.ui.exception.NoSuchScreenHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@Component(ScreensLinkHandlerProcessor.NAME)
public class ScreensLinkHandlerProcessor implements LinkHandlerProcessor, Ordered {
    public static final String NAME = "cuba_ScreensLinkHandlerProcessor";

    @Inject
    private Logger log;

    @Inject
    protected Metadata metadata;

    @Inject
    protected DataManager dataService;

    @Inject
    protected WindowConfig windowConfig;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected EntityAccessExceptionHandler entityAccessExceptionHandler;
    @Inject
    protected AccessDeniedHandler accessDeniedHandler;
    @Inject
    protected NoSuchScreenHandler noSuchScreenHandler;
    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;

    @Override
    public boolean canHandle(ExternalLinkContext linkContext) {
        return linkContext.getRequestParams().containsKey("screen");
    }

    @Override
    public void handle(ExternalLinkContext linkContext) {
        String screenName = linkContext.getRequestParams().get("screen");
        App app = linkContext.getApp();

        final WindowInfo windowInfo = windowConfig.getWindowInfo(screenName);
        if (windowInfo == null) {
            log.warn("WindowInfo not found for screen: {}", screenName);
            return;
        }

        try {
            openWindow(windowInfo, linkContext);
        } catch (EntityAccessException e) {
            entityAccessExceptionHandler.handle(e, app.getWindowManager());
        } catch (AccessDeniedException e) {
            accessDeniedHandler.handle(e, app.getWindowManager());
        } catch (NoSuchScreenException e) {
            noSuchScreenHandler.handle(e, app.getWindowManager());
        }
    }

    protected void openWindow(WindowInfo windowInfo, ExternalLinkContext linkContext) {
        Map<String, String> requestParams = linkContext.getRequestParams();
        App app = linkContext.getApp();

        String itemStr = requestParams.get("item");
        String openTypeParam = requestParams.get("openType");
        OpenType openType = OpenType.NEW_TAB;

        if (StringUtils.isNotEmpty(openTypeParam)) {
            try {
                openType = OpenType.valueOf(openTypeParam);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown open type ({}) in request parameters", openTypeParam);
            }
        }

        if (itemStr == null) {
            app.getWindowManager().openWindow(windowInfo, openType, getParamsMap(requestParams));
        } else {
            EntityLoadInfo info = EntityLoadInfo.parse(itemStr);
            if (info == null) {
                log.warn("Invalid item definition: {}", itemStr);
            } else {
                Entity entity = loadEntityInstance(info);
                if (entity != null)
                    app.getWindowManager().openEditor(windowInfo, entity, openType, getParamsMap(requestParams));
                else
                    throw new EntityAccessException();
            }
        }
    }

    protected Map<String, Object> getParamsMap(Map<String, String> requestParams) {
        Map<String, Object> params = new HashMap<>();
        String paramsStr = requestParams.get("params");
        if (paramsStr == null)
            return params;

        String[] entries = paramsStr.split(",");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 2) {
                log.warn("Invalid parameter: {}", entry);
                return params;
            }
            String name = parts[0];
            String value = parts[1];
            EntityLoadInfo info = EntityLoadInfo.parse(value);
            if (info != null) {
                Entity entity = loadEntityInstance(info);
                if (entity != null)
                    params.put(name, entity);
            } else if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
                params.put(name, BooleanUtils.toBoolean(value));
            } else {
                params.put(name, value);
            }
        }
        return params;
    }

    protected Entity loadEntityInstance(EntityLoadInfo info) {
        if (info.isNewEntity()) {
            return metadata.create(info.getMetaClass());
        }

        String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntityFromLink(info.getMetaClass());
        //noinspection unchecked
        LoadContext<Entity> ctx = new LoadContext(info.getMetaClass());
        ctx.setQueryString(format("select e from %s e where e.%s = :entityId", info.getMetaClass().getName(), pkName))
                .setParameter("entityId", info.getId());
        if (info.getViewName() != null) {
            View view = viewRepository.findView(info.getMetaClass(), info.getViewName());
            if (view != null) {
                ctx.setView(view);
            } else {
                log.warn("Unable to find view \"{}\" for entity \"{}\"", info.getViewName(), info.getMetaClass());
            }
        }
        Entity entity;
        try {
            entity = dataService.load(ctx);
        } catch (Exception e) {
            log.warn("Unable to load item: {}", info, e);
            return null;
        }
        return entity;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 30;
    }
}
