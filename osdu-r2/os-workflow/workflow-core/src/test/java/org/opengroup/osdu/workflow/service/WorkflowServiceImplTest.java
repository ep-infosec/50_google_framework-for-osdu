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

package org.opengroup.osdu.workflow.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.WorkflowType;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowResponse;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.exception.RuntimeException;
import org.opengroup.osdu.workflow.model.WorkflowStatus;
import org.opengroup.osdu.workflow.model.WorkflowStatusType;
import org.opengroup.osdu.workflow.provider.interfaces.IIngestionStrategyService;
import org.opengroup.osdu.workflow.provider.interfaces.ISubmitIngestService;
import org.opengroup.osdu.workflow.provider.interfaces.IValidationService;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowStatusRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class WorkflowServiceImplTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String DAG_NAME = "dag-name";
  private static final String TEST_EXCEPTION = "test-exception";
  private static final String DATA_TYPE = "test-type";

  @Mock
  private IValidationService validationService;
  @Mock
  private IIngestionStrategyService ingestionStrategyService;
  @Mock
  private ISubmitIngestService submitIngestService;
  @Mock
  private IWorkflowStatusRepository workflowStatusRepository;

  @Captor
  ArgumentCaptor<WorkflowStatus> workflowStatusCaptor;

  WorkflowServiceImpl workflowService;

  @BeforeEach
  void setUp() {
    workflowService = new WorkflowServiceImpl(validationService, ingestionStrategyService, submitIngestService, workflowStatusRepository);
  }

  @Test
  void shouldStartWorkflow() {

    // given
    HashMap<String, Object> context = new HashMap<>();
    context.put("key", "value");
    StartWorkflowRequest request = StartWorkflowRequest.builder()
        .workflowType(WorkflowType.INGEST)
        .dataType(DATA_TYPE)
        .context(context).build();
    DpsHeaders headers = getMessageHeaders();
    given(ingestionStrategyService
        .determineStrategy(eq(WorkflowType.INGEST), eq(DATA_TYPE), isNull()))
        .willReturn(DAG_NAME);

    // when
    StartWorkflowResponse startWorkflowResponse = workflowService
        .startWorkflow(request, headers);

    // then
    then(startWorkflowResponse.getWorkflowId()).isNotNull();
    InOrder inOrder = Mockito.inOrder(validationService,
        ingestionStrategyService, submitIngestService, workflowStatusRepository);
    inOrder.verify(validationService).validateStartWorkflowRequest(request);
    inOrder.verify(ingestionStrategyService)
        .determineStrategy(eq(WorkflowType.INGEST), eq(DATA_TYPE), isNull());
    inOrder.verify(submitIngestService).submitIngest(eq(DAG_NAME), eq(context));
    inOrder.verify(workflowStatusRepository).saveWorkflowStatus(workflowStatusCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    then(workflowStatusCaptor.getValue()).satisfies(status -> {
      then(status.getAirflowRunId()).isNotNull();
      then(status.getWorkflowId()).isNotNull();
      then(status.getWorkflowStatusType()).isEqualTo(WorkflowStatusType.SUBMITTED);
    });
  }

  @Test
  void shouldNotSaveWorkflowStatusIfSubmitRequestFails() {

    // given
    HashMap<String, Object> context = new HashMap<>();
    context.put("key", "value");
    StartWorkflowRequest request = StartWorkflowRequest.builder()
        .workflowType(WorkflowType.INGEST)
        .dataType(DATA_TYPE)
        .context(context).build();
    DpsHeaders headers = getMessageHeaders();
    given(ingestionStrategyService
        .determineStrategy(eq(WorkflowType.INGEST), eq(DATA_TYPE), isNull()))
        .willReturn(DAG_NAME);
    doThrow(new RuntimeException(TEST_EXCEPTION)).when(submitIngestService)
        .submitIngest(eq(
            DAG_NAME),
            eq(context));

    // when
    Throwable thrown = catchThrowable(() -> workflowService
        .startWorkflow(request, headers));

    // then
    then(thrown).isInstanceOf(RuntimeException.class);
    verify(workflowStatusRepository, never()).saveWorkflowStatus(any());
  }

  private DpsHeaders getMessageHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put(DpsHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(DpsHeaders.DATA_PARTITION_ID, PARTITION);

    return DpsHeaders.createFromMap(headers);
  }
}
