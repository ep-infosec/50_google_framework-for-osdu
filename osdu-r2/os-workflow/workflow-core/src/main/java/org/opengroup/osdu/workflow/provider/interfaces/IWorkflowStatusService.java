/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.workflow.provider.interfaces;

import org.opengroup.osdu.core.common.exception.UnauthorizedException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.model.GetStatusResponse;
import org.opengroup.osdu.workflow.model.UpdateStatusRequest;
import org.opengroup.osdu.workflow.model.UpdateStatusResponse;

public interface IWorkflowStatusService {

  /**
   * GetWorkflowStatus returns status of workflow specified.
   *
   * @param request getStatus request
   * @param headers headers
   * @return workflow status.
   * @throws UnauthorizedException if token and partitionID are missing or, invalid
   */
  GetStatusResponse getWorkflowStatus(GetStatusRequest request, DpsHeaders headers);

  /**
   * Update Workflow Status returns status of workflow specified.
   *
   * @param request update status request
   * @param headers headers
   * @return workflow status.
   * @throws UnauthorizedException if token and partitionID are missing or, invalid
   */
  UpdateStatusResponse updateWorkflowStatus(UpdateStatusRequest request, DpsHeaders headers);

}
