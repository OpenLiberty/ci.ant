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

package net.wasdev.wlp.ant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.wasdev.wlp.ant.AbstractTask.ReturnCode;
import net.wasdev.wlp.ant.install.Version;

public class ServerVersion extends AbstractTask {
    
    public String cmdServer;
    
    public Version getWlpVersion(String cmd) {

        List<String> command = new ArrayList<String>();
        command.add(0, cmd);
        command.add(1, "version");
        
        processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        
        Process p;
        try {
            p = processBuilder.start();
            checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        

        Pattern pattern = null;
        pattern = Pattern.compile("^((\\w*\\s)+)(?<major>\\d+)(\\.(?<minor>\\d+))?(\\.(?<micro>\\d+))?(\\.(?<qualifier>(.)+))?(\\s\\(\\d)(.)+$");
        Matcher matcher = pattern.matcher(result);
        
        String qualifier = null;
        int major = 0;
        int minor = 0;
        int micro = 0;

        if (matcher.matches()) {
            major = parseComponent(matcher.group("major"));
            minor = parseComponent(matcher.group("minor"));
            micro = parseComponent(matcher.group("micro"));
            qualifier = matcher.group("qualifier");
        }
  
        Version runtimeVersion = new Version(major, minor, micro, qualifier);

        return runtimeVersion;
    }


    public int parseComponent(String version) {
        if (version == null) {
            return 0;
        }  else {
            return Integer.parseInt(version);
        }
    }
}
