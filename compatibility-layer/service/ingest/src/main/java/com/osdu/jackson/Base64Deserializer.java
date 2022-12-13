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

package com.osdu.jackson;

import static java.lang.String.format;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;
import java.util.Base64;

public class Base64Deserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

  Class<?> resultClass;

  @Override
  public Object deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    String value = parser.getValueAsString();
    Base64.Decoder decoder = Base64.getDecoder();

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      byte[] decodedValue = decoder.decode(value);
      return objectMapper.readValue(decodedValue, resultClass);
    } catch (IllegalArgumentException | JsonParseException e) {
      String fieldName = parser.getParsingContext().getCurrentName();
      Class<?> wrapperClass = parser.getParsingContext().getCurrentValue().getClass();

      throw new InvalidFormatException(parser,
          format("Value for '%s' is not a base64 encoded JSON", fieldName), value, wrapperClass);
    }
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
      throws JsonMappingException {
    this.resultClass = property.getType().getRawClass();
    return this;
  }
}
