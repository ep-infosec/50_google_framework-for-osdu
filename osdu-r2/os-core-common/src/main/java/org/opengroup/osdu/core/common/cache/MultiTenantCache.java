/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.cache;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MultiTenantCache<V> {
    private final ICache<String, V> wrappedCache;
    private final Map<String, TenantSafeCache> tenantCaches = new HashMap<>();

    public MultiTenantCache(ICache<String, V> cache) {
        this.wrappedCache = cache;
    }

    public ICache<String, V> get(String partition) {
        String tenantName = partition;
        if (StringUtils.isBlank(tenantName))
            invalidTenantGivenException(tenantName);
        if (!tenantCaches.containsKey(tenantName)) {
            addCache(tenantName);
        }
        return tenantCaches.get(tenantName);
    }

    private void addCache(String tenantName) {
        tenantCaches.put(tenantName, new TenantSafeCache<>(tenantName, wrappedCache));
    }

    private void invalidTenantGivenException(String tenantName) {
        throw new IllegalArgumentException(String.format("Partition given does not exist: %s", tenantName));
    }
}
