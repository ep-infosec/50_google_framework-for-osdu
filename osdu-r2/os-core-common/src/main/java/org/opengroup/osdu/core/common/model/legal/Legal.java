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

package org.opengroup.osdu.core.common.model.legal;

import java.util.Set;

import javax.validation.constraints.NotEmpty;

import org.opengroup.osdu.core.common.model.storage.validation.ValidNotNullCollection;
import org.opengroup.osdu.core.common.model.storage.validation.ValidationDoc;

import io.jsonwebtoken.lang.Collections;
import lombok.Data;

@Data
public class Legal {

	@ValidNotNullCollection
	private Set<String> legaltags;

	@ValidNotNullCollection
	@NotEmpty(message = ValidationDoc.RECORD_ORDC_NOT_EMPTY)
	private Set<String> otherRelevantDataCountries;

	private LegalCompliance status;

	public boolean hasLegaltags() {
		return !Collections.isEmpty(this.legaltags);
	}
}
