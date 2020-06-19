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
package io.jmix.ui.download;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Format of data exported by {@link Downloader}.
 */
public final class DownloadFormat implements Serializable {
    private static final long serialVersionUID = 4477403854619413777L;

    public static final DownloadFormat HTML = new DownloadFormat("text/html", "html");
    public static final DownloadFormat HTM = new DownloadFormat("text/html", "htm");
    public static final DownloadFormat PDF = new DownloadFormat("application/pdf", "pdf");
    public static final DownloadFormat XLS = new DownloadFormat("application/vnd.ms-excel", "xls");
    public static final DownloadFormat XLSX = new DownloadFormat("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
    public static final DownloadFormat RTF = new DownloadFormat("application/rtf", "rtf");
    public static final DownloadFormat DOC = new DownloadFormat("application/doc", "doc");
    public static final DownloadFormat DOCX = new DownloadFormat("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
    public static final DownloadFormat XML = new DownloadFormat("text/xml", "xml");
    public static final DownloadFormat CSV = new DownloadFormat("application/csv", "csv");
    public static final DownloadFormat JPEG = new DownloadFormat("image/jpeg", "jpeg");
    public static final DownloadFormat JPG = new DownloadFormat("image/jpeg", "jpg");
    public static final DownloadFormat PNG = new DownloadFormat("image/png", "png");
    public static final DownloadFormat RAR = new DownloadFormat("application/x-rar-compressed", "rar");
    public static final DownloadFormat ZIP = new DownloadFormat("application/zip", "zip");
    public static final DownloadFormat GZ = new DownloadFormat(" application/x-gzip", "gz");
    public static final DownloadFormat JSON = new DownloadFormat(" application/json", "json");
    public static final DownloadFormat OCTET_STREAM = new DownloadFormat("application/octet-stream", "");
    public static final DownloadFormat TEXT = new DownloadFormat("text/plain", "");

    public static final List<DownloadFormat> DEFAULT_FORMATS = Collections.unmodifiableList(
            Arrays.asList(HTML, HTM, PDF, XLS, XLSX, RTF, DOC, DOCX, XML, CSV, JPEG, JPG, PNG, RAR, GZ, ZIP, OCTET_STREAM, JSON));

    protected final String contentType;
    protected final String fileExt;

    public DownloadFormat(String contentType, String fileExt) {
        this.contentType = contentType;
        this.fileExt = fileExt;
    }

    public static DownloadFormat getByExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return OCTET_STREAM;
        }

        String extLowerCase = StringUtils.lowerCase(extension);

        List<DownloadFormat> formats = DEFAULT_FORMATS;
        for (DownloadFormat f : formats) {
            if (f.getFileExt().equals(extLowerCase))
                return f;
        }
        return OCTET_STREAM;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExt() {
        return fileExt;
    }
}