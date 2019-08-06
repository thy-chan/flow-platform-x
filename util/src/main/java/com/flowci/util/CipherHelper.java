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

package com.flowci.util;

/**
 * @author yang
 */
public class CipherHelper {

    private final static String RsaPrivateKeyStart = "-----BEGIN RSA PRIVATE KEY-----";

    private final static String RsaPrivateKeyEnd = "-----END RSA PRIVATE KEY-----";

    public static boolean isRsaPrivateKey(String src) {
        src = src.trim();
        return src.startsWith(RsaPrivateKeyStart) && src.endsWith(RsaPrivateKeyEnd);
    }

    private CipherHelper() {

    }
}
