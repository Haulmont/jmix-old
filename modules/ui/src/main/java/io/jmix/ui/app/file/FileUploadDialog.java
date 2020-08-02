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

package io.jmix.ui.app.file;

import io.jmix.ui.Notifications;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.SingleFileUploadField.FileUploadSucceedEvent;
import io.jmix.ui.component.UploadField.FileUploadErrorEvent;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import java.util.UUID;

@UiController("singleFileUploadDialog")
@UiDescriptor("file-upload-dialog.xml")
@DialogMode(forceDialog = true)
public class FileUploadDialog extends Screen {

    @Inject
    protected FileStorageUploadField fileUpload;

    @Inject
    protected HBoxLayout dropZone;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected MessageBundle messageBundle;

    protected UUID fileId;

    protected String fileName;

    public UUID getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    @Subscribe("fileUpload")
    protected void onFileUploadSucceedEvent(FileUploadSucceedEvent event) {
        fileId = fileUpload.getFileId();
        fileName = fileUpload.getFileName();
        close(StandardOutcome.COMMIT);
    }

    @Subscribe("fileUpload")
    protected void onFileUploadErrorEvent(FileUploadErrorEvent event) {
        notifications.create(Notifications.NotificationType.WARNING)
                .withCaption(messageBundle.getMessage("notification.uploadUnsuccessful"))
                .show();
    }
}
