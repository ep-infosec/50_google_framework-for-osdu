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

package com.osdu.service.delfi;

import com.osdu.exception.OsduBadRequestException;
import com.osdu.mapper.IngestHeadersMapper;
import com.osdu.messaging.IngestPubSubGateway;
import com.osdu.model.IngestHeaders;
import com.osdu.model.IngestResult;
import com.osdu.model.job.IngestMessage;
import com.osdu.model.type.manifest.LoadManifest;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import com.osdu.service.InitialIngestService;
import com.osdu.service.JobStatusService;
import com.osdu.service.validation.LoadManifestValidationService;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiInitialIngestService implements InitialIngestService {

  final JobStatusService jobStatusService;
  final LoadManifestValidationService loadManifestValidationService;
  final IngestPubSubGateway ingestGateway;
  final AuthenticationService authenticationService;

  @Named
  final IngestHeadersMapper ingestHeadersMapper;

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    log.debug("Request to ingest file with following parameters: {}, and headers : {}",
        loadManifest, headers);

    checkPreconditions(headers);

    IngestHeaders ingestHeaders = ingestHeadersMapper.toIngestHeaders(headers);
    log.debug("Parse ingest headers. Headers: {}", ingestHeaders);

    authenticationService
        .checkAuthentication(ingestHeaders.getAuthorizationToken(), ingestHeaders.getPartition());

    loadManifestValidationService.validateManifest(loadManifest);

    String jobId = jobStatusService.initInjectJob();

    IngestMessage ingestMessage = IngestMessage.builder()
        .ingestJobId(jobId)
        .loadManifest(loadManifest)
        .headers(ingestHeaders)
        .build();
    log.debug("Send ingest message for processing. Message: {}", ingestMessage);
    ingestGateway.sendIngestToPubSub(ingestMessage);

    log.debug("Request to ingest with parameters : {}, init the injection jobId: {}", loadManifest,
        jobId);
    return IngestResult.builder()
        .jobId(jobId)
        .build();
  }

  private void checkPreconditions(MessageHeaders headers) {
    if (!headers.containsKey(OsduHeader.AUTHORIZATION)) {
      throw new OsduBadRequestException("Missing authorization token");
    }

    if (!headers.containsKey(OsduHeader.PARTITION)) {
      throw new OsduBadRequestException("Missing partition");
    }

    if (!headers.containsKey(OsduHeader.LEGAL_TAGS)) {
      throw new OsduBadRequestException("Missing \"legal-tags\" header");
    }

    if (!headers.containsKey(OsduHeader.RESOURCE_HOME_REGION_ID)) {
      throw new OsduBadRequestException("Missing \"resource-home-region-id\" header");
    }

    if (!headers.containsKey(OsduHeader.RESOURCE_HOST_REGION_IDS)) {
      throw new OsduBadRequestException("Missing \"resource-host-region-ids\" header");
    }
  }

}
