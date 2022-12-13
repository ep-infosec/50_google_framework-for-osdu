/*
 * Copyright  2020 Google LLC
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

package com.osdu.core.utils.helper;

public class EnvironmentVariableReceiver {
    public static String getGoogleCredentialFile() {
        return System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    }

    public static String getBearerToken() {
        return System.getenv("TOKEN");
    }

    /**
     * Get main url paths
     */
    public static String getIngestHost() {
        return System.getenv("INGEST");
    }

    public static String getDeliveryHost() {
        return System.getenv("DELIVERY");
    }

    public static String getWorkflowHost() {
        return System.getenv("WORKFLOW");
    }

    public static String getAirflowHost() {
        return System.getenv("AIRFLOW");
    }

    public static String getGoogleLogin() {
        return System.getenv("GOOGLE_LOGIN");
    }

    public static String getGooglePassword() {
        return System.getenv("GOOGLE_PASSWORD");
    }

    public static String getTokenPage(){
        return System.getenv("PAGE_WITH_TOKEN");
    }
}