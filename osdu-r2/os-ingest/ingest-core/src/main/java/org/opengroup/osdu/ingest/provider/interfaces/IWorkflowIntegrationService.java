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

package org.opengroup.osdu.ingest.provider.interfaces;

import java.util.Map;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.ingest.exception.ServerErrorException;

public interface IWorkflowIntegrationService {

  /**
   * Start new workflow according to {@code workflowType} and {@code dataType}.
   *
   * @param workflowType workflowType
   * @param dataType dataType
   * @param context workflow context
   * @param commonHeaders common headers
   * @return workflow ID
   * @throws ServerErrorException if unable to create start workflow request
   *                              or workflow response doesn't contain workflow ID
   */
  String submitIngestToWorkflowService(WorkflowType workflowType, String dataType,
                                       Map<String, Object> context,
                                       DpsHeaders commonHeaders);
}
