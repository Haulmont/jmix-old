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
package io.jmix.ui.download;

import io.jmix.core.FileStorage;

import javax.annotation.Nullable;

/**
 * Generic interface to download data from the system.
 */
public interface Downloader {

    String NAME = "ui_Downloader";

    /**
     * Downloads an arbitrary resource defined by a DownloadDataProvider.
     *
     * @param dataProvider resource provider
     * @param resourceName resource name
     * @param format       download format, can be null
     */
    void download(DownloadDataProvider dataProvider, String resourceName, @Nullable DownloadFormat format);

    /**
     * Downloads an arbitrary resource defined by a DownloadDataProvider.
     *
     * @param dataProvider resource provider
     * @param resourceName resource name
     */
    void download(DownloadDataProvider dataProvider, String resourceName);

    /**
     * Downloads a file from file storage.
     * <p>
     * The default file storage of the system is used by default.
     * Different file storage can be set in {@link #setFileStorage(FileStorage)}.
     *
     * @param fileReference file reference
     * @param <R>           file reference type
     */
    <R> void download(R fileReference);

    /**
     * Downloads a file from file storage.
     * <p>
     * The default file storage of the system is used by default.
     * Different file storage can be set in {@link #setFileStorage(FileStorage)}.
     *
     * @param fileReference file reference
     * @param <R>           file reference type
     * @param format        download format, can be null
     */
    <R> void download(R fileReference, @Nullable DownloadFormat format);

    /**
     * Sets a file storage where the files will be downloaded from.
     *
     * @param fileStorage file storage
     * @param <R>         file reference type
     */
    <R> void setFileStorage(FileStorage<R, ?> fileStorage);

    /**
     * @return {@code true} if downloader should open a new window with the file content
     */
    boolean isShowNewWindow();

    /**
     * Sets explicit new window option.
     *
     * @param showNewWindow {@code true} if downloader opens new window, otherwise {@code false}
     */
    void setShowNewWindow(boolean showNewWindow);
}
