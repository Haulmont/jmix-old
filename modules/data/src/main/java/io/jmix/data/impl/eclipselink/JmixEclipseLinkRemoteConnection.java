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

package io.jmix.data.impl.eclipselink;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.cluster.ClusterListenerAdapter;
import io.jmix.core.cluster.ClusterManager;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.impl.entitycache.QueryCacheManager;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet;
import org.eclipse.persistence.internal.sessions.coordination.broadcast.BroadcastRemoteConnection;
import org.eclipse.persistence.sessions.coordination.MergeChangeSetCommand;
import org.eclipse.persistence.sessions.coordination.RemoteCommandManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Component(JmixEclipseLinkRemoteConnection.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixEclipseLinkRemoteConnection extends BroadcastRemoteConnection {
    public static final String NAME = "data_JmixEclipseLinkRemoteConnection";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected QueryCacheManager queryCacheManager;
    @Autowired
    protected ClusterManager clusterManager;

    public JmixEclipseLinkRemoteConnection(RemoteCommandManager rcm) {
        super(rcm);
    }

    @PostConstruct
    protected void init() {
        rcm.logDebug("creating_broadcast_connection", getInfo());
        try {
            this.clusterManager.addListener(Message.class, new ClusterListenerAdapter<Message>() {
                @Override
                public void receive(Message message) {
                    onMessage(message);
                }
            });
            rcm.logDebug("broadcast_connection_created", getInfo());
        } catch (RuntimeException ex) {
            rcm.logDebug("failed_to_create_broadcast_connection", getInfo());
            close();
            throw ex;
        }
    }

    public boolean isLocal() {
        return true;
    }

    @Override
    protected Object executeCommandInternal(Object command) throws Exception {
        Message message = new Message(command);

        Object[] debugInfo = null;
        if (this.rcm.shouldLogDebugMessage()) {
            debugInfo = logDebugBeforePublish(null);
        }

        if (queryCacheManager.isEnabled()) {
            invalidateQueryCache(command);
        }
        this.clusterManager.send(message);

        if (debugInfo != null) {
            logDebugAfterPublish(debugInfo, null);
        }

        return null;
    }

    public void onMessage(Message message) {
        if (rcm.shouldLogDebugMessage()) {
            logDebugOnReceiveMessage(null);
        }
        if (message.getObject() != null) {
            Object command = message.getObject();
            if (queryCacheManager.isEnabled()) {
                invalidateQueryCache(command);
            }
            processReceivedObject(command, "");
        }
    }

    @Override
    protected boolean areAllResourcesFreedOnClose() {
        return !isLocal();
    }

    @Override
    protected void closeInternal() {
    }

    @Override
    protected void createDisplayString() {
        this.displayString = Helper.getShortClassName(this) + "[" + serviceId.toString() + "]";
    }

    @Override
    protected boolean shouldCheckServiceId() {
        return false;
    }

    protected void invalidateQueryCache(Object command) {
        if (command instanceof MergeChangeSetCommand) {
            MergeChangeSetCommand changeSetCommand = (MergeChangeSetCommand) command;
            UnitOfWorkChangeSet changeSet = changeSetCommand.getChangeSet(null);
            if (changeSet != null && changeSet.getAllChangeSets() != null) {
                Set<String> typeNames = new HashSet<>();
                changeSet.getAllChangeSets().values().stream().filter(obj -> obj.getClassName() != null).forEach(obj -> {
                    MetaClass metaClass = metadata.findClass(ReflectionHelper.getClass(obj.getClassName()));
                    if (metaClass != null) {
                        metaClass = extendedEntities.getOriginalOrThisMetaClass(metaClass);
                        typeNames.add(metaClass.getName());
                    }
                });
                queryCacheManager.invalidate(typeNames, false);
            }
        }
    }

    public static class Message implements Serializable {

        private Object object;

        public Message(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return object;
        }

        @Override
        public String toString() {
            return String.format("Message{object=%s}", object);
        }
    }
}
