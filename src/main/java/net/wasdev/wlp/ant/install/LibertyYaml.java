/**
 * (C) Copyright IBM Corporation 2015.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * A very simple utility class to parse Liberty's index.yml file.
 */
public class LibertyYaml  {

    public static List<LibertyInfo> parse(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return parse(new BufferedReader(new InputStreamReader(in, "UTF-8")));
        } finally {
            InstallUtils.close(in);
        }
    }

    public static List<LibertyInfo> parse(BufferedReader reader) throws IOException {
        List<LibertyInfo> values = new ArrayList<LibertyInfo>();

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("---")) {
                continue;
            }
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(":");
            String version = parts[0].trim();
            Map<String, String> map = parseContent(reader);

            LibertyInfo info = new LibertyInfo(version, map);
            values.add(info);
        }

        return values;
    }

    private static Map<String, String> parseContent(BufferedReader reader) throws IOException {
        Map<String, String> map = new HashMap<String, String>();

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                break;
            }

            if (line.startsWith(" ")) {
                int pos = line.indexOf(':');
                if (pos != -1) {
                    String key = line.substring(0, pos).trim();
                    String value = line.substring(pos+1).trim();
                    map.put(key.trim(), value.trim());
                }
            }
        }

        return map;
    }

}
