/*
 * Copyright 2019 flow.ci
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

package com.flowci.core.flow.domain;

import com.google.common.base.Strings;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yang
 */
@Data
public class FlowGitTest {

    @NotNull
    private String gitUrl;

    private String privateKey;

    private String credential;

    public boolean hasPrivateKey() {
        return !Strings.isNullOrEmpty(privateKey);
    }

    public boolean hasCredentialName() {
        return !Strings.isNullOrEmpty(credential);
    }
}
