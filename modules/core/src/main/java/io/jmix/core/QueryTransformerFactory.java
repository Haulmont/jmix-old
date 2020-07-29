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
package io.jmix.core;

import io.jmix.core.impl.jpql.DomainModel;
import io.jmix.core.impl.jpql.DomainModelBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory to get {@link QueryParser} and {@link QueryTransformer} instances.
 */
@Component(QueryTransformerFactory.NAME)
public class QueryTransformerFactory {

    public static final String NAME = "core_QueryTransformerFactory";

    protected volatile DomainModel domainModel;

    @Autowired
    protected BeanFactory beanFactory;

    @Autowired
    protected ObjectProvider<QueryParser> queryParserProvider;

    public QueryTransformer transformer(String query) {
        if (domainModel == null) {
            DomainModelBuilder builder = (DomainModelBuilder) beanFactory.getBean(DomainModelBuilder.NAME);
            domainModel = builder.produce();
        }
        return beanFactory.getBean(QueryTransformer.class, domainModel, query);
    }

    public QueryParser parser(String query) {
        if (domainModel == null) {
            DomainModelBuilder builder = (DomainModelBuilder) beanFactory.getBean(DomainModelBuilder.NAME);
            domainModel = builder.produce();
        }
        return queryParserProvider.getObject(domainModel, query);
    }
}