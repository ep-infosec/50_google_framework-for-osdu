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

package com.osdu.service;

import static com.osdu.service.DelfiSearchService.KIND_HEADER_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.client.delfi.DelfiSearchClient;
import com.osdu.exception.SearchException;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.delfi.entitlement.UserGroups;
import com.osdu.model.delfi.geo.ByDistance;
import com.osdu.model.delfi.geo.GeoLocation;
import com.osdu.model.delfi.geo.SpatialFilter;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.OsduSearchResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DelfiSearchServiceTest {

  private static final String KIND = "kind";
  private static final String PARTITION = "partition";
  private static final String AUTHORIZATION = "authorization";
  private static final String RESULT_1 = "result-1";
  private static final String RESULT_2 = "result-2";
  private static final String APP_KEY = "appKey";

  @MockBean
  private DelfiPortalProperties portalProperties;
  @MockBean
  private DelfiSearchClient delfiSearchClient;
  @MockBean
  private AuthenticationService authenticationService;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private DelfiSearchService searchService;

  @Test
  public void shouldSearchIndexByDistance() {

    //given
    Map<String, Object> headersMap = new HashMap<>();
    headersMap.put(KIND_HEADER_KEY, KIND);
    headersMap.put(OsduHeader.PARTITION, PARTITION);
    headersMap.put(OsduHeader.AUTHORIZATION, AUTHORIZATION);
    MessageHeaders headers = new MessageHeaders(headersMap);

    OsduSearchObject osduSearchObject = new OsduSearchObject();
    osduSearchObject.setStart(1);
    osduSearchObject.setCount(2);

    osduSearchObject.setGeoCentroid(Arrays.asList(Arrays.asList(36.742612, -99.074218)));

    DelfiSearchObject expectedDelfiSearchObject = new DelfiSearchObject();
    expectedDelfiSearchObject.setKind(KIND);
    expectedDelfiSearchObject.setLimit(2);
    expectedDelfiSearchObject.setOffset(1);
    SpatialFilter filter = new SpatialFilter();
    filter.setField("data.dlLatLongWGS84");
    GeoLocation location = new ByDistance(Arrays.asList(Arrays.asList(36.742612, -99.074218)),
        1000D);
    filter.setByDistance(location);
    expectedDelfiSearchObject.setSpatialFilter(filter);

    DelfiSearchResult delfiSearchResult = new DelfiSearchResult();
    delfiSearchResult.setTotalCount(2);
    delfiSearchResult.setResults(Arrays.asList(RESULT_1, RESULT_2));

    when(authenticationService.getUserGroups(eq(AUTHORIZATION), eq(PARTITION)))
        .thenReturn(new UserGroups());
    when(portalProperties.getAppKey()).thenReturn(APP_KEY);
    when(delfiSearchClient.searchIndex(eq(AUTHORIZATION), eq(APP_KEY), same(PARTITION),
        eq(expectedDelfiSearchObject))).thenReturn(delfiSearchResult);

    // when
    OsduSearchResult searchResult = (OsduSearchResult) searchService
        .searchIndex(osduSearchObject, headers);

    //then
    assertThat(searchResult.getCount()).isEqualTo(2);
    assertThat(searchResult.getTotalHits()).isEqualTo(2);
    assertThat(searchResult.getStart()).isEqualTo(1);
    assertThat(searchResult.getResults()).isEqualTo(Arrays.asList(RESULT_1, RESULT_2));
  }

  @Test
  public void shouldThrowExceptionIfParametersInvalid() throws JsonProcessingException {

    //given
    Map<String, Object> headersMap = new HashMap<>();
    headersMap.put(KIND_HEADER_KEY, KIND);
    headersMap.put(OsduHeader.PARTITION, PARTITION);
    headersMap.put(OsduHeader.AUTHORIZATION, AUTHORIZATION);
    MessageHeaders headers = new MessageHeaders(headersMap);

    OsduSearchObject osduSearchObject = new OsduSearchObject();
    osduSearchObject.setStart(1);
    osduSearchObject.setCount(2);
    osduSearchObject.setFulltext(null);
    osduSearchObject.setMetadata(null);
    osduSearchObject.setGeoCentroid(null);
    osduSearchObject.setGeoLocation(null);

    when(authenticationService.getUserGroups(eq(AUTHORIZATION), eq(PARTITION)))
        .thenReturn(new UserGroups());

    // when
    Throwable thrown = catchThrowable(() -> searchService.searchIndex(osduSearchObject, headers));

    // then
    assertThat(thrown)
        .isInstanceOf(SearchException.class)
        .hasMessageContaining("Input parameters validation fail - ");
  }
}
