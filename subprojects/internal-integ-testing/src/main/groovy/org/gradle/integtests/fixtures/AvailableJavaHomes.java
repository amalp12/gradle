/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.integtests.fixtures;

import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.util.GFileUtils;

import java.io.File;

/**
 * Allows the tests to get hold of an alternative Java installation when needed.
 */
abstract public class AvailableJavaHomes {

    private static File getJavaHome(String label) {
        String value = System.getenv().get(String.format("JDK_%s", label));
        return value == null ? null : GFileUtils.canonicalise(new File(value));
    }

    public static File getBestAlternative() {
        Jvm jvm = Jvm.current();

        // Use environment variables
        File javaHome = null;
        if (jvm.getJavaVersion().isJava6Compatible()) {
            javaHome = firstAvailable("15", "17");
        } else if (jvm.getJavaVersion().isJava5Compatible()) {
            javaHome = firstAvailable("16", "17");
        }
        if (javaHome != null) {
            return javaHome;
        }

        if (OperatingSystem.current().isMacOsX()) {
            File registeredJvms = new File("/Library/Java/JavaVirtualMachines");
            if (registeredJvms.isDirectory()) {
                for (File candidate : registeredJvms.listFiles()) {
                    javaHome = GFileUtils.canonicalise(new File(candidate, "Contents/Home"));
                    if (!javaHome.equals(jvm.getJavaHome()) && javaHome.isDirectory() && new File(javaHome, "bin/java").isFile()) {
                        return javaHome;
                    }
                }
            }
        } else if (OperatingSystem.current().isLinux()) {
            // Ubuntu specific
            File installedJvms = new File("/usr/lib/jvm");
            if (installedJvms.isDirectory()) {
                for (File candidate : installedJvms.listFiles()) {
                    javaHome = GFileUtils.canonicalise(candidate);
                    if (!javaHome.equals(jvm.getJavaHome()) && javaHome.isDirectory() && new File(javaHome, "bin/java").isFile()) {
                        return javaHome;
                    }
                }
            }
        } else if (OperatingSystem.current().isWindows()) {
            //very simple algorithm trying to find java on windows
            File installedJavas = new File("c:/Program Files/Java");
            if (!installedJavas.isDirectory()) {
                // Attempt to look for 32-bit version under 64-bit OS
                installedJavas = new File("c:/Program Files (x86)/Java");
            }
            if (installedJavas.isDirectory()) {
                for (File file : installedJavas.listFiles()) {
                    if (file.getName().startsWith("jdk")) {
                        if (jvm.getJavaVersion().isJava6() && !file.getName().contains("1.6")) {
                            return file;
                        }
                        if (jvm.getJavaVersion().isJava7() && !file.getName().contains("1.7")) {
                            return file;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Locates a JRE for the current JVM. Prefers a stand-alone JRE installation over one that is part of a JDK install.
     *
     * @return The JRE home directory, or null if not found
     */
    public static File getBestJre() {
        Jvm jvm = Jvm.current();

        // Use environment variables
        File jreHome = null;

        if (OperatingSystem.current().isWindows()) {
            //very simple algorithm trying to find java on windows
            File installedJavas = new File("c:/Program Files/Java");
            if (!installedJavas.isDirectory()) {
                // Attempt to look for 32-bit version under 64-bit OS
                installedJavas = new File("c:/Program Files (x86)/Java");
            }
            if (installedJavas.isDirectory()) {
                for (File file : installedJavas.listFiles()) {
                    if (file.getName().startsWith("jre")) {
                        if (jvm.getJavaVersion().isJava6() && !file.getName().contains("1.6")) {
                            return file;
                        }
                        if (jvm.getJavaVersion().isJava7() && !file.getName().contains("1.7")) {
                            return file;
                        }
                    }
                }
            }
        }
        jreHome = new File(jvm.getJavaHome(), "jre");
        if (jreHome.isDirectory()) {
            return jreHome;
        }
        return null;
    }

    public static File firstAvailable(String... labels) {
        for (String label : labels) {
            File found = getJavaHome(label);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
