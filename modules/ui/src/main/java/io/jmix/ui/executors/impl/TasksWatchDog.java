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

package io.jmix.ui.executors.impl;

import io.jmix.core.TimeSource;
import io.jmix.ui.executors.WatchDog;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * WatchDog for {@link io.jmix.ui.executors.BackgroundWorker}.
 */
@ThreadSafe
public abstract class TasksWatchDog implements WatchDog {

    public enum ExecutionStatus {
        NORMAL,
        TIMEOUT_EXCEEDED,
        SHOULD_BE_KILLED
    }

    @Inject
    protected TimeSource timeSource;

    private final Set<TaskHandlerImpl> watches = new LinkedHashSet<>();

    public TasksWatchDog() {
    }

    @Override
    public synchronized void cleanupTasks() {
        long actual = timeSource.currentTimestamp().getTime();

        List<TaskHandlerImpl> forRemove = new ArrayList<>();
        // copy watches since task.kill tries to remove task handler from watches
        for (TaskHandlerImpl task : new ArrayList<>(watches)) {
            if (task.isCancelled() || task.isDone()) {
                forRemove.add(task);
            } else {
                ExecutionStatus status = getExecutionStatus(actual, task);

                switch (status) {
                    case TIMEOUT_EXCEEDED:
                        task.closeByTimeout();
                        task.timeoutExceeded();
                        forRemove.add(task);
                        break;

                    case SHOULD_BE_KILLED:
                        task.kill();
                        forRemove.add(task);
                        break;

                    default:
                        break;
                }
            }
        }

        watches.removeAll(forRemove);
    }

    protected abstract ExecutionStatus getExecutionStatus(long actualTimeMs, TaskHandlerImpl taskHandler);

    @Override
    public synchronized void stopTasks() {
        // copy watches since task.kill tries to remove task handler from watches
        ArrayList<TaskHandlerImpl> taskHandlers = new ArrayList<>(watches);
        watches.clear();
        for (TaskHandlerImpl task : taskHandlers) {
            task.kill();
        }
    }

    @Override
    public synchronized int getActiveTasksCount() {
        return watches.size();
    }

    /**
     * {@inheritDoc}
     *
     * @param taskHandler Task handler
     */
    @Override
    public synchronized void manageTask(TaskHandlerImpl taskHandler) {
        watches.add(taskHandler);
    }

    @Override
    public synchronized void removeTask(TaskHandlerImpl taskHandler) {
        watches.remove(taskHandler);
    }
}