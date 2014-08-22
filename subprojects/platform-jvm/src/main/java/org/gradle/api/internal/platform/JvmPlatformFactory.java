/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.platform;

import java.util.Arrays;

public class JvmPlatformFactory {
    public static JvmPlatform create(String... versions) {
        final String version;
        if (versions == null) {
            throw new PlatformTargetException("Could not find a valid JVM Platform target version.");
        } else if (versions.length != 1) {
            throw new PlatformTargetException("Could not find exactly one JVM Platform target version. Found: " + versions);
        } else {
            version = Arrays.asList(versions).get(0);
        }
        return new DefaultJvmPlatform(version);
    }
}
