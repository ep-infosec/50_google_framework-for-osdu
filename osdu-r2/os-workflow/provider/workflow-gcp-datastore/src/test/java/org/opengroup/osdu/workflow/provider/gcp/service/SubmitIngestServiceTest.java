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

package org.opengroup.osdu.workflow.provider.gcp.service;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.exception.RuntimeException;
import org.opengroup.osdu.workflow.provider.gcp.property.AirflowProperties;
import org.opengroup.osdu.workflow.provider.interfaces.ISubmitIngestService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class SubmitIngestServiceTest {

  private static final String TEST_AIRFLOW_URL = "http://test-airflow";
  private static final String TEST_CLIENT_ID = "client-id";
  @Mock
  private GoogleIapHelper googleIapHelper;

  @Mock
  private AirflowProperties airflowProperties;

  @Mock
  private HttpRequest httpRequest;

  @Mock
  private HttpResponse httpResponse;

  ISubmitIngestService submitIngestService;

  @BeforeEach
  void setUp() {
    submitIngestService = new SubmitIngestServiceImpl(airflowProperties, googleIapHelper);
  }

  @Test
  void shouldStartWorkflow() throws IOException {

    // given
    HashMap<String, Object> data = new HashMap<>();
    data.put("key", "value");
    given(airflowProperties.getUrl()).willReturn(TEST_AIRFLOW_URL);
    given(googleIapHelper.getIapClientId(eq(TEST_AIRFLOW_URL))).willReturn(TEST_CLIENT_ID);
    given(googleIapHelper.buildIapRequest(anyString(), eq(TEST_CLIENT_ID), anyMap()))
        .willReturn(httpRequest);
    given(httpRequest.execute()).willReturn(httpResponse);
    given(httpResponse.getContent()).willReturn(new ByteArrayInputStream("test".getBytes()));

    // when
    submitIngestService.submitIngest("dag-name", data);

    // then
    InOrder inOrder = Mockito.inOrder(airflowProperties, googleIapHelper);
    inOrder.verify(airflowProperties).getUrl();
    inOrder.verify(googleIapHelper).getIapClientId(eq(TEST_AIRFLOW_URL));
    inOrder.verify(googleIapHelper).buildIapRequest(anyString(), anyString(), anyMap());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowExceptionIfRequestFails() throws IOException {

    // given
    HashMap<String, Object> data = new HashMap<>();
    data.put("key", "value");
    given(airflowProperties.getUrl()).willReturn(TEST_AIRFLOW_URL);
    given(googleIapHelper.getIapClientId(eq(TEST_AIRFLOW_URL))).willReturn(TEST_CLIENT_ID);
    given(googleIapHelper.buildIapRequest(anyString(), eq(TEST_CLIENT_ID), anyMap()))
        .willReturn(httpRequest);
    given(httpRequest.execute()).willThrow(new IOException("test-exception"));

    // when
    Throwable thrown = catchThrowable(() -> submitIngestService.submitIngest("dag-name", data));

    // then
    then(thrown).satisfies(exception -> {
      then(exception).isInstanceOf(RuntimeException.class);
      then(exception).hasMessage("test-exception");
    });
  }

}
