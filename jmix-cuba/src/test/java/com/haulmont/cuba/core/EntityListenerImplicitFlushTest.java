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
 */

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CubaCoreTest;
import com.haulmont.cuba.core.testsupport.TestContainer;
import io.jmix.core.Metadata;
import io.jmix.core.View;
import io.jmix.data.EntityManager;
import io.jmix.data.Persistence;
import io.jmix.data.Transaction;
import io.jmix.data.TypedQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CubaCoreTest
public class EntityListenerImplicitFlushTest {

    public static TestContainer cont = TestContainer.Common.INSTANCE;

    @Inject
    private Persistence persistence;
    @Inject
    private Metadata metadata;

    private Group group;
    private User user;

    @AfterEach
    public void cleanup() {
        cont.deleteRecord(user, group);
    }

    @Test
    public void test() throws Exception {

        try (Transaction tx = persistence.createTransaction()) {
            user = metadata.create(User.class);
            group = metadata.create(Group.class);

            EntityManager em = persistence.getEntityManager();

            group.setName("group");
            em.persist(group);

            user.setGroup(group);
            user.setLogin("u-" + user.getId());
            em.persist(user);

            TypedQuery<User> query = em.createQuery("select u from test$User u where u.loginLowerCase = :login", User.class);
            query.setParameter("login", user.getLogin());
            query.setViewName(View.LOCAL); // setting a view leads to using FlushModeType.AUTO - see QueryImpl.getQuery()
            List<User> list = query.getResultList();
            assertEquals(1, list.size());
            assertEquals(user, list.get(0));

            tx.commit();
        }
    }
}
