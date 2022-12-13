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

package org.opengroup.osdu.core.common.model.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TransferInfo {

	private Long version;
	private String user;
	private Integer recordCount;
	private List<String> skippedRecords = new ArrayList<>();

	public TransferInfo(String currentUser, Integer recordCount) {

		this.user = currentUser;
		this.recordCount = recordCount;

		int randomNumber = new Random().nextInt(1000 - 1 + 1) + 1;
		this.version = System.currentTimeMillis() * 1000 + randomNumber;
	}
}
