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

package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class ByBoundingBox implements GeoLocation {

  @NonNull
  Point topLeft;
  @NonNull
  Point bottomRight;

  /**
   * Constructor.
   *
   * @param coordinates coordinates
   */
  public ByBoundingBox(List<List<Double>> coordinates) {
    if (coordinates.size() != 2) {
      throw new GeoLocationException(
          "Bounding box GeoJSON requires exactly 2 points for creation, actual, received "
              + coordinates.size());
    }
    topLeft = GeoUtils.coordinatesToPoint(coordinates.get(0));
    bottomRight = GeoUtils.coordinatesToPoint(coordinates.get(1));
  }
}
