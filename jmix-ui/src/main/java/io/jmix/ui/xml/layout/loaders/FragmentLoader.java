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
package io.jmix.ui.xml.layout.loaders;

import io.jmix.core.DevelopmentException;
import io.jmix.ui.AppConfig;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.components.AbstractFrame;
import io.jmix.ui.components.Facet;
import io.jmix.ui.components.Fragment;
import io.jmix.ui.components.Frame;
import io.jmix.ui.data.Datasource;
import io.jmix.ui.data.DsContext;
import io.jmix.ui.data.impl.DatasourceImplementation;
import io.jmix.ui.data.impl.GenericDataSupplier;
import io.jmix.ui.logging.ScreenLifeCycle;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.screen.compatibility.LegacyFrame;
import io.jmix.ui.sys.CompanionDependencyInjector;
import io.jmix.ui.sys.ScreenViewsLoader;
import io.jmix.ui.xml.FacetLoader;
import io.jmix.ui.xml.data.DsContextLoader;
import io.jmix.ui.xml.layout.ComponentRootLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.perf4j.StopWatch;

import javax.annotation.Nullable;
import java.util.List;

import static io.jmix.ui.logging.UIPerformanceLogger.createStopWatch;

public class FragmentLoader extends ContainerLoader<Fragment> implements ComponentRootLoader<Fragment> {

    public void setResultComponent(Fragment fragment) {
        this.resultComponent = fragment;
    }

    @Override
    public void createComponent() {
        throw new UnsupportedOperationException("Fragment cannot be created from XML element");
    }

    @Override
    public void createContent(Element layoutElement) {
        if (layoutElement == null) {
            throw new DevelopmentException("Missing required 'layout' element");
        }
        createSubComponents(resultComponent, layoutElement);
    }

    @Override
    public void loadComponent() {
        if (resultComponent.getFrameOwner() instanceof AbstractFrame) {
            getScreenViewsLoader().deployViews(element);
        }

        ComponentContext componentContext = getComponentContext();

        if (componentContext.getParent() == null) {
            throw new IllegalStateException("FragmentLoader is always called within parent ComponentLoaderContext");
        }

        assignXmlDescriptor(resultComponent, element);

        Element layoutElement = element.element("layout");
        if (layoutElement == null) {
            throw new GuiDevelopmentException("Required 'layout' element is not found",
                    componentContext.getFullFrameId());
        }

        loadIcon(resultComponent, layoutElement);
        loadCaption(resultComponent, layoutElement);
        loadDescription(resultComponent, layoutElement);

        loadVisible(resultComponent, layoutElement);
        loadEnable(resultComponent, layoutElement);
        loadActions(resultComponent, element);

        loadSpacing(resultComponent, layoutElement);
        loadMargin(resultComponent, layoutElement);
        loadWidth(resultComponent, layoutElement);
        loadHeight(resultComponent, layoutElement);
        loadStyleName(resultComponent, layoutElement);
        loadResponsive(resultComponent, layoutElement);
        loadCss(resultComponent, element);

        Element dataEl = element.element("data");
        if (dataEl != null) {
            loadScreenData(dataEl);
        } else if (resultComponent.getFrameOwner() instanceof LegacyFrame) {
            Element dsContextElement = element.element("dsContext");
            loadDsContext(dsContextElement);
        }

        if (resultComponent.getFrameOwner() instanceof AbstractFrame) {
            Element companionsElem = element.element("companions");
            if (companionsElem != null) {
                componentContext.addInjectTask(new FragmentLoaderCompanionTask(resultComponent));
            }
        }

        loadSubComponentsAndExpand(resultComponent, layoutElement);
        setComponentsRatio(resultComponent, layoutElement);

        loadFacets(resultComponent, element);
    }

    protected ScreenViewsLoader getScreenViewsLoader() {
        return beanLocator.get(ScreenViewsLoader.NAME);
    }

    protected void loadScreenData(Element dataEl) {
        ScreenData hostScreenData = null;
        ComponentContext parent = getComponentContext().getParent();
        while (hostScreenData == null && parent != null) {
            hostScreenData = parent.getScreenData();
            parent = parent.getParent();
        }
        ScreenDataXmlLoader screenDataXmlLoader = beanLocator.get(ScreenDataXmlLoader.class);
        ScreenData screenData = UiControllerUtils.getScreenData(resultComponent.getFrameOwner());
        screenDataXmlLoader.load(screenData, dataEl, hostScreenData);
        ((ComponentLoaderContext) context).setScreenData(screenData);
    }

    protected void loadFacets(Fragment resultComponent, Element fragmentElement) {
        Element facetsElement = fragmentElement.element("facets");
        if (facetsElement != null) {
            List<Element> facetElements = facetsElement.elements();

            for (Element facetElement : facetElements) {
                FacetLoader loader = beanLocator.get(FacetLoader.NAME);
                Facet facet = loader.load(facetElement, getComponentContext());

                resultComponent.addFacet(facet);
            }
        }
    }

    protected void loadDsContext(@Nullable Element dsContextElement) {
        DsContext dsContext = null;
        if (resultComponent.getFrameOwner() instanceof LegacyFrame) {
            DsContextLoader dsContextLoader;
            DsContext parentDsContext = getComponentContext().getParent().getDsContext();
            if (parentDsContext != null){
                dsContextLoader = new DsContextLoader(parentDsContext.getDataSupplier());
            } else {
                dsContextLoader = new DsContextLoader(new GenericDataSupplier());
            }

            dsContext = dsContextLoader.loadDatasources(dsContextElement, parentDsContext,
                    getComponentContext().getAliasesMap());
            ((ComponentLoaderContext) context).setDsContext(dsContext);
        }
        if (dsContext != null) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            if (frameOwner instanceof LegacyFrame) {
                LegacyFrame frame = (LegacyFrame) frameOwner;
                frame.setDsContext(dsContext);

                for (Datasource ds : dsContext.getAll()) {
                    if (ds instanceof DatasourceImplementation) {
                        ((DatasourceImplementation) ds).initialized();
                    }
                }

                dsContext.setFrameContext(resultComponent.getContext());
            }
        }
    }

    protected class FragmentLoaderCompanionTask implements InjectTask {
        protected Fragment fragment;

        public FragmentLoaderCompanionTask(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void execute(ComponentContext context, Frame frame) {
            String loggingId = context.getFullFrameId();
            try {
                if (fragment.getFrameOwner() instanceof AbstractFrame) {
                    Element companionsElem = element.element("companions");
                    if (companionsElem != null) {
                        StopWatch companionStopWatch = createStopWatch(ScreenLifeCycle.COMPANION, loggingId);

                        initCompanion(companionsElem, (AbstractFrame) fragment.getFrameOwner());

                        companionStopWatch.stop();
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("Unable to init frame companion", e);
            }
        }

        protected void initCompanion(Element companionsElem, AbstractFrame frame) {
            String clientTypeId = AppConfig.getClientType().toString().toLowerCase();
            Element element = companionsElem.element(clientTypeId);
            if (element != null) {
                String className = element.attributeValue("class");
                if (!StringUtils.isBlank(className)) {
                    Class aClass = getScripting().loadClassNN(className);
                    Object companion;
                    try {
                        companion = aClass.newInstance();
                        frame.setCompanion(companion);

                        CompanionDependencyInjector cdi = new CompanionDependencyInjector(frame, companion);
                        cdi.setBeanLocator(beanLocator);
                        cdi.inject();
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to init companion for frame", e);
                    }
                }
            }
        }
    }
}