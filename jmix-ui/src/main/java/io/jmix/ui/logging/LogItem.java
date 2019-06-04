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
package io.jmix.ui.logging;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nullable;
import java.util.Date;

public class LogItem {

    private Date timestamp;
    private LogLevel level;
    private String message;

    // legacy link
    private Throwable throwable;
    private String fullStackTrace;

    public LogItem(Date timestamp, LogLevel level, String message, Throwable throwable) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.throwable = throwable;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getStacktrace() {
        if (fullStackTrace != null) {
            return fullStackTrace;
        }

        return throwable != null ? ExceptionUtils.getStackTrace(throwable) : "";
    }

    /**
     * @return null if has been appended to log
     */
    @Nullable
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Cleans reference to throwable. Stacktrace still will be available.
     */
    public void sanitize() {
        if (throwable != null) {
            fullStackTrace = ExceptionUtils.getStackTrace(throwable);
        }
        this.throwable = null;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}