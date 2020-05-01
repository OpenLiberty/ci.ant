/**
 * (C) Copyright IBM Corporation 2015, 2020.
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
package io.openliberty.tools.ant;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Uninstall feature task.
 */
public class UninstallFeatureTask extends FeatureManagerTask {

    @Override
    public void execute() {
        if ((name == null || name.isEmpty()) && features.isEmpty()) {
            throw new BuildException(MessageFormat.format(messages.getString("error.parameter.empty"), "name"));
        }
        
        initTask();
        
        File f = new File(cmd);
        if(f.exists()) {
            try {
                doUninstall();
            } catch (BuildException e) {
                throw e;
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
        else {
            log("The installUtility is not available on this Liberty runtime. Any features specified in the build will not be uninstalled.", Project.MSG_WARN);
        }
    }

    private void doUninstall() throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("uninstall");      
        command.add("--noPrompts");
        
        if (name != null && !name.isEmpty()) {
            // The name field can hold a comma separated list of features
            // Remove any spaces at the beginning, end or around the separator
            String[] names = name.trim().split("\\s*,\\s*");
            for (String featureName : names) {
                if (!name.isEmpty()) {
                    command.add(featureName);
                }
            }
        }
        if (!features.isEmpty()) {
            for (Feature feature : features) {
                command.add(feature.getFeature());
            }
        }
        
        processBuilder.command(command);
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();

        checkReturnCodeAndError(p, processBuilder.command().toString(), ReturnCode.OK.getValue(), ReturnCode. RUNTIME_EXCEPTION.getValue(), "CWWKF1207E");
    }

}
