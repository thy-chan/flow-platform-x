/*
 * Copyright 2018 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flowci.exception;

/**
 * @author yang
 */
public class JsonException extends CIException {

    public JsonException(String message, String... params) {
        super(message, params);
    }

    public JsonException(String message, Throwable cause, String... params) {
        super(message, cause, params);
    }

    @Override
    public Integer getCode() {
        return ErrorCode.PARSE_YML_OR_JSON;
    }
}
