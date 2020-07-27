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

package io.jmix.ui.component;

import io.jmix.core.DataLoadContext;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.model.BaseCollectionLoader;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Component that makes a data binding to load data by pages.
 */
public interface Pagination extends Component.BelongToFrame {
    String NAME = "pagination";

    enum State {
        FIRST_COMPLETE,     // "63 rows"
        FIRST_INCOMPLETE,   // "1-100 rows of [?] >"
        MIDDLE,             // "< 101-200 rows of [?] >"
        LAST                // "< 201-252 rows"
    }

    enum ButtonsAlignment {
        LEFT, RIGHT
    }

    /**
     * @return whether rows count should be loaded automatically
     */
    boolean getAutoLoad();

    /**
     * Sets whether rows count should be loaded automatically.
     *
     * @param autoLoad pass true to enable auto load, or false otherwise
     */
    void setAutoLoad(boolean autoLoad);

    /**
     * @return delegate which is used to get the total number of rows when user clicks "total count" or "last page".
     */
    @Nullable
    Function<DataLoadContext, Long> getTotalCountDelegate();

    /**
     * Sets delegate which is used to get the total number of rows when user clicks "total count" or "last page".
     */
    void setTotalCountDelegate(Function<DataLoadContext, Long> delegate);

    /**
     * Sets a loader that should be used for pagination.
     *
     * @param loader loader to set
     */
    void setLoaderTarget(BaseCollectionLoader loader);

    /**
     * @return a loader that is used for pagination.
     */
    @Nullable
    BaseCollectionLoader getLoaderTarget();

    /**
     * Sets buttons alignment inside Pagination component. Position is LEFT by default.
     *
     * @param alignment buttons alignment
     */
    void setButtonsAlignment(ButtonsAlignment alignment);

    /**
     * @return buttons alignment inside Pagination component. Position is LEFT by default.
     */
    ButtonsAlignment getButtonsAlignment();

    boolean isShowMaxResults();

    void setShowMaxResults(boolean showMaxResults);

    /**
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addBeforeRefreshListener(Consumer<Pagination.BeforeRefreshEvent> listener);

    /**
     * Event that is fired before refreshing the data container when the user clicks next, previous, etc.
     * <br>
     * You can prevent the data container refresh by invoking {@link Pagination.BeforeRefreshEvent#preventRefresh()},
     * for example:
     * <pre>{@code
     * usersTable.getPagination().addBeforeRefreshListener(refreshEvent -> {
     *     // check modified data and prevent refresh
     *     refreshEvent.preventRefresh();
     * });
     * }</pre>
     */
    class BeforeRefreshEvent extends EventObject {
        protected boolean refreshPrevented = false;

        public BeforeRefreshEvent(Pagination source) {
            super(source);
        }

        /**
         * If invoked, the component will not refresh the data container.
         */
        public void preventRefresh() {
            refreshPrevented = true;
        }

        public boolean isRefreshPrevented() {
            return refreshPrevented;
        }
    }
}
