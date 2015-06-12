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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.wasdev.wlp.ant.install.Version;

import org.apache.tools.ant.BuildException;

/**
 * Uninstall feature task.
 */
public class UninstallFeatureTask extends AbstractTask {

    private String cmd;
    private String cmdServer;

    // Skips user's confirmation and uninstalls the feature.
    private boolean noPrompts = false;

    // Name of the feature to uninstall or URL.
    private String name;

    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            cmd = installDir + "\\bin\\featureManager.bat";
            cmdServer = installDir + "\\bin\\server.bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            cmd = installDir + "/bin/featureManager";
            cmdServer = installDir + "/bin/server";
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);

    }

    @Override
    public void execute() {

        if (name == null || name.length() <= 0) {
            throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"), "name"));
        }

        initTask();

        ServerVersion utils =  new ServerVersion();

        Version serverVersion = utils.getWlpVersion(cmdServer);            
        Version referenceVersionBeta = new Version(2015, 4, 0, "0");            
        Version referenceVersionStable = new Version(8, 5, 5, "5");

        int diffVersionBeta = referenceVersionBeta.compareTo(serverVersion);
        int diffVersionStable = referenceVersionStable.compareTo(serverVersion);

        if (diffVersionStable > 0) {
            log(messages.getString("error.server.version.invalid"));
        }
        else if(diffVersionBeta > 0  && diffVersionBeta < 1000) {
            log(messages.getString("error.server.version.invalid"));
        }
        else 
        {
            try {
                    doUninstall();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private void doUninstall() throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("uninstall");      
        if (noPrompts) {
            command.add("--noPrompts");
        }
        command.add(name);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }

    /**
     * @return quiet value
     */
    public boolean isNoPrompts() {
        return noPrompts;
    }

    /**
     * @param acceptLicense the acceptLicense to set
     */
    public void setNoPrompts(boolean noPrompts) {
        this.noPrompts = noPrompts;
    }

    /**
     * @return the feature name
     */
    public String getName() {
        return name;
    }

    /**
     * @param the feature name
     */
    public void setName(String name) {
        this.name = name;
    }

} 
