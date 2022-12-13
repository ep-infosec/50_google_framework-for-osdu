/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service.google;

import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.MEDIA_COMPLETE;
import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.MEDIA_IN_PROGRESS;

import com.google.api.client.googleapis.media.MediaHttpUploader.UploadState;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ByteStreams;
import com.google.api.client.util.Preconditions;
import com.osdu.model.upload.ContentChunk;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomMediaHttpUploader {

  static final int MB = 0x100000;
  int chunkSize = 10 * MB;

  /**
   * Perform chunked resumable upload.
   *
   * @param mediaContent content to upload
   * @param uploadUrl    url to upload to
   * @return HttpResponse of the upload request
   * @throws IOException if connection down
   */
  public HttpResponse resumableUpload(InputStreamContent mediaContent, GenericUrl uploadUrl)
      throws IOException {
    try (InputStream contentInputStream = new BufferedInputStream(mediaContent.getInputStream())) {
      UploadProgress progress = new UploadProgress(mediaContent.getLength(),
          UploadState.NOT_STARTED,
          uploadUrl);

      HttpResponse response;
      while (true) {
        ContentChunk contentChunk = buildContentChunk(mediaContent,
            progress.getTotalBytesServerReceived());
        HttpTransport transport = new NetHttpTransport();
        HttpRequestFactory requestFactory = transport.createRequestFactory();
        HttpRequest currentRequest = requestFactory.buildPutRequest(uploadUrl, null);
        currentRequest.setContent(contentChunk.getContent());
        currentRequest.getHeaders().setContentRange(contentChunk.getContentRange());
        currentRequest.getHeaders().setContentLength(mediaContent.getLength());

        response = executeCurrentRequestWithoutGZip(currentRequest);

        try {
          if (response.isSuccessStatusCode()) {
            progress.setTotalBytesServerReceived(mediaContent.getLength());
            if (mediaContent.getCloseInputStream()) {
              contentInputStream.close();
            }
            progress.updateProgressState(MEDIA_COMPLETE);
            return response;
          }

          if (response.getStatusCode() != 308) {
            return response;
          }

          long newBytesServerReceived = getNextByteIndex(response.getHeaders().getRange());
          long currentBytesServerReceived =
              newBytesServerReceived - progress.getTotalBytesServerReceived();
          Preconditions.checkState(currentBytesServerReceived >= 0
              && currentBytesServerReceived <= contentChunk.getLength());
          long notSendBytes = contentChunk.getLength() - currentBytesServerReceived;

          if (notSendBytes > 0) {
            contentInputStream.reset();
            long actualSkipValue = contentInputStream.skip(currentBytesServerReceived);
            Preconditions.checkState(currentBytesServerReceived == actualSkipValue);
          }

          progress.setTotalBytesServerReceived(newBytesServerReceived);
          progress.updateProgressState(MEDIA_IN_PROGRESS);
        } finally {
          response.disconnect();
        }
      }
    }
  }

  private ContentChunk buildContentChunk(InputStreamContent mediaContent,
      Long totalBytesServerReceived) {

    int blockSize = (int) Math.min(chunkSize, mediaContent.getLength() - totalBytesServerReceived);

    AbstractInputStreamContent contentChunk;
    InputStream contentInputStream = mediaContent.getInputStream();
    contentInputStream.mark(blockSize);

    InputStream limitInputStream = ByteStreams.limit(contentInputStream, blockSize);
    contentChunk = new InputStreamContent(mediaContent.getType(),
        limitInputStream).setRetrySupported(true).setLength(blockSize).setCloseInputStream(false);
    String mediaContentLengthStr = String.valueOf(mediaContent.getLength());

    String bytes = "bytes";
    String contentRange = blockSize == 0 ? String.format("%s %s", bytes, mediaContentLengthStr)
        : String.format("%s %d-%d/%s", bytes, totalBytesServerReceived,
            totalBytesServerReceived + blockSize - 1, mediaContentLengthStr);

    return new ContentChunk(contentChunk, contentRange, blockSize);
  }

  private HttpResponse executeCurrentRequestWithoutGZip(HttpRequest request) throws IOException {
    request.setThrowExceptionOnExecuteError(false);
    return request.execute();
  }

  private long getNextByteIndex(String rangeHeader) {
    if (rangeHeader == null) {
      return 0L;
    }
    return Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('-') + 1)) + 1;
  }
}