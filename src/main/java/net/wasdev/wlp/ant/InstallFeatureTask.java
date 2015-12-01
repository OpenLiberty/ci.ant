/**
 * (C) Copyright IBM Corporation 2014, 2015.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;

/**
 * Install feature task.
 */
public class InstallFeatureTask extends FeatureManagerTask {

    // accept license
    private boolean acceptLicense = false;

    // install as user or product extension (usr|extension)
    private String to;

    // action to take if a file to be installed already exists (fail|ignore|replace)
    private String whenFileExists;
    
    // a single directory-based repository as the source of the assets for the installUtility command
    private String from;

    @Override
    public void execute() {

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
        List<String> command;
        command = initCommand();
        if (name != null && !name.isEmpty()) {
            command.add(name);
        } 
        if (!features.isEmpty()) {
            for (Feature feature : features) {
                command.add(feature.getFeature());
            }
        }
        if (features.isEmpty() && (name == null || name.isEmpty())) {
            command.add(serverName);
        }
        processCommand(command);
    }
    
    /** Generate a String list containing all the parameter for the command.
     * @returns A List<String> containing the command to be executed.
     */
    private List<String> initCommand(){
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("install");
        if (acceptLicense) {
            command.add("--acceptLicense");
        } else {
            command.add("--viewLicenseAgreement");
        }
        if (to != null) {
            command.add("--to=" + to);
        }
        if (from != null) {
            command.add("--from=" + from);
        }
        
        return command;
    }
    
    /** Process the command.
     * @param command A String list containing the command to be executed.
     */
    private void processCommand(List<String> command) throws Exception {
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue(), ReturnCode.ALREADY_EXISTS.getValue());
        if (!acceptLicense) {
            throw new BuildException("To install a feature, you must accept the feature's license terms and conditions.");
        }
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
     * @deprecated installUtility does not have a whenFileExist parameter.
     */
    @Deprecated
    public String getWhenFileExists() {
        return whenFileExists;
    }

    /**
     * @deprecated installUtility does not have a whenFileExist parameter.
     */
    @Deprecated
    public void setWhenFileExists(String whenFileExists) {
        this.whenFileExists = whenFileExists;
    }
    
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
