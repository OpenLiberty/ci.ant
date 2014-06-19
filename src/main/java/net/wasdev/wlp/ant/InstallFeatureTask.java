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
package net.wasdev.wlp.ant;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

/**
 * Install feature task.
 */
public class InstallFeatureTask extends AbstractTask {

    private String cmd;

    // accept license
    private boolean acceptLicense = false;

    // install as user or product extension (usr|extension)
    private String to;

    // action to take if a file to be installed already exists (fail|ignore|replace)
    private String whenFileExists;

    // name of the feature to install or URL
    private String name;

    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            cmd = installDir + "\\bin\\featureManager.bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            cmd = installDir + "/bin/featureManager";
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
        
        try {
            doInstall();
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void doInstall() throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("install");      
        if (acceptLicense) {
            command.add("--acceptLicense");
        }
        if (to != null) {
            command.add("--to=" + to);
        }
        if (whenFileExists != null) {
            command.add("--when-file-exists=" + whenFileExists);
        }
        command.add(name);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }

    /**
     * @return the acceptLicense
     */
    public boolean isAcceptLicense() {
        return acceptLicense;
    }

    /**
     * @param acceptLicense the acceptLicense to set
     */
    public void setAcceptLicense(boolean acceptLicense) {
        this.acceptLicense = acceptLicense;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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

    public String getWhenFileExists() {
        return whenFileExists;
    }

    public void setWhenFileExists(String whenFileExists) {
        this.whenFileExists = whenFileExists;
    }

}
