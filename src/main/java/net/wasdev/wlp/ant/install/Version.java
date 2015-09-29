/**
 * (C) Copyright IBM Corporation 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.ant.install;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    private final static int WILDCARD = -1;

    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;

    private Version(int major, int minor, int micro, String qualifier) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
    }

    public static Version parseVersion(String version) {
        return parseVersion(version, false);
    }

    public static Version parseVersion(String version, boolean wildcard) {
        Pattern p = null;
        if (wildcard) {
            p = Pattern.compile("^([\\d\\+]+)(?:\\.([\\d\\+]+))?(?:\\.([\\d\\+]+))?(?:\\_(.*))?$");
        } else {
            p = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:\\_(.*))?$");
        }
        Matcher m = p.matcher(version);

        if (m.find()) {
            int major = parseComponent(m.group(1));
            int minor = parseComponent(m.group(2));
            int micro = parseComponent(m.group(3));
            String qualifier = m.group(4);
            return new Version(major, minor, micro, qualifier);
        } else {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
    }

    private static int parseComponent(String version) {
        if (version == null) {
            return 0;
        } else if ("+".equals(version)) {
            return WILDCARD;
        } else {
            return Integer.parseInt(version);
        }
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Version)) {
            return false;
        }
        return compareTo((Version)other) == 0;
    }

    public int compareTo(Version other) {
        if (other == this) {
            return 0;
        }

        int result = major - other.major;
        if (result != 0) {
            return result;
        }
        result = minor - other.minor;
        if (result != 0) {
            return result;
        }
        result = micro - other.micro;
        if (result != 0) {
            return result;
        }
        return qualifier.compareTo(other.qualifier);
    }

    public boolean match(Version version) {
        if (major == WILDCARD) {
            return true;
        } else if (major != version.major) {
            return false;
        }

        if (minor == WILDCARD) {
            return true;
        } else if (minor != version.minor) {
            return false;
        }

        if (micro == WILDCARD) {
            return true;
        } else if (micro != version.micro) {
            return false;
        }

        if ("+".equals(qualifier)) {
            return true;
        } else if (qualifier == null) {
            return version.qualifier == null;
        } else {
            return qualifier.equals(version.qualifier);
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getMicro() {
        return micro;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        if (major == WILDCARD) {
            result.append('+');
        } else {
            result.append(major);
            result.append('.');

            if (minor == WILDCARD) {
                result.append('+');
            } else {
                result.append(minor);
                result.append('.');

                if (micro == WILDCARD) {
                    result.append('+');
                } else {
                    result.append(micro);
                    if (qualifier != null) {
                        result.append('_');
                        result.append(qualifier);
                    }
                }
            }
        }
        return result.toString();
    }
}
