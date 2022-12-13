/*
 * Copyright  2019 Google LLC
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

package com.osdu.core.endpoints.factories.specified;

import com.osdu.core.data.properties.PropertyHolder;
import com.osdu.core.endpoints.creator.InstanceCreator;
import com.osdu.core.endpoints.services.FileServiceCreator;
import com.osdu.core.endpoints.services.IngestCreator;
import com.osdu.core.endpoints.services.WorkflowServiceCreator;

/**
 * Get local url for the required endpoint
 */
public class LocalUrlFactory implements BaseFactory {
    InstanceCreator instanceCreator = new InstanceCreator();

    @Override
    public String getIngest(String resource) {
        return instanceCreator.creator(new IngestCreator()).getUrl(PropertyHolder.localProps.getIngestLocalUrl() + resource);
    }

    @Override
    public String getFileService(String resource) {
        return instanceCreator.creator(new FileServiceCreator()).getUrl(PropertyHolder.localProps.getFileServiceHost() + resource);
    }

    @Override
    public String getWorkflowService(String resource) {
        return instanceCreator.creator(new WorkflowServiceCreator()).getUrl(PropertyHolder.localProps.getWorkflowServiceHost() + resource);

    }
}