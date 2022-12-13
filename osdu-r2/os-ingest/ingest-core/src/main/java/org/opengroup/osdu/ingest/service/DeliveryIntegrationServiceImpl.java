/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.ingest.client.IDeliveryServiceClient;
import org.opengroup.osdu.ingest.exception.ServerErrorException;
import org.opengroup.osdu.ingest.provider.interfaces.IDeliveryIntegrationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryIntegrationServiceImpl implements IDeliveryIntegrationService {

  final IDeliveryServiceClient deliveryServiceClient;
  final ObjectMapper objectMapper;

  @Override
  public FileLocationResponse getFileInfo(String fileId, DpsHeaders commonHeaders) {

    FileLocationRequest request = FileLocationRequest.builder()
        .fileID(fileId).build();

    log.debug("Send file location request to file service, request - {}", request);

    try (Response response = deliveryServiceClient
        .getFileLocation(commonHeaders.getAuthorization(), commonHeaders.getPartitionIdWithFallbackToAccountId(),
            request)) {

      FileLocationResponse fileLocationResponse = objectMapper
          .readValue(response.body().asInputStream(), FileLocationResponse.class);

      log.debug("Receive file location response from file service, response - {}",
          fileLocationResponse);

      if (fileLocationResponse.getLocation() == null) {
        throw new ServerErrorException("No file location in file service response");
      }

      return fileLocationResponse;
    } catch (IOException exception) {
      throw new ServerErrorException("Exception while getting file location", exception);
    }
  }

}
