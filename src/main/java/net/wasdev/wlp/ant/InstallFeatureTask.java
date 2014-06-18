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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

/**
 * featureManager install operations task: install
 * <wlp:featureManager  ref="wlp.ant.test" archiveLocation="my_feature.esa" />
 * <wlp:install-feature archiveLocation=".." to=".." accept-license="true"/>
 */
public class InstallFeatureTask extends AbstractTask {

    private String operation;
    private String wlp;
    
    // used with 'install'  operation
    private boolean acceptLicense = false;
      
    // used with "install" operation
    private String to;

    // used with 'install' operation
    private String whenFileExists;
    
   // used with 'install' operation file name or URL
    private String  archiveLocation;
    
    
    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            wlp = installDir + "\\bin\\featureManager.bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            wlp = installDir + "/bin/featureManager";
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);

    }

    @Override
    public void execute() {
        if (operation == null || operation.length() <= 0) {
            throw new BuildException(MessageFormat.format(messages.getString("error.featureManager.operation.validate"), "operation"));
        }

        initTask();

        try {
        	if ("install".equals(operation)) {
                doInstall();
            } else {
                throw new BuildException("Unsupported operation: " + operation);
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
    
        private void doInstall() throws Exception {

            List<String> command = getInitialCommand(operation);
            
            addAcceptLicenseOption(command);
            addToOption(command);
            addWhenFileExistsOption((command));
            
            processBuilder.command(command);
            Process p = processBuilder.start();
            checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue(), ReturnCode.REDUNDANT_ACTION_STATUS.getValue());

           
          
        }
        
    
    private List<String> getInitialCommand(String operation) {
        List<String> commands = new ArrayList<String>();
        commands.add(wlp);
        commands.add(operation);
        if (archiveLocation != null && !archiveLocation.equals("")) {
            commands.add(archiveLocation);
        }
        
        return commands;
    }
    
    
    private void addWhenFileExistsOption(List<String> command) {
        if (whenFileExists != null) {
            command.add("--when-file-exists=" + whenFileExists);
        }
    }
    
    private void addToOption(List<String> command) {
        if (to != null) {
            command.add("--to=" + to);
        }
    }
    
    private void addAcceptLicenseOption(List<String> command) {
        if (acceptLicense) {
            command.add("--acceptLicense");
        }
    }
    
    
    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation
     *            the operation to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }   

    /**
     * @return the acceptLicense
     */
    public boolean isAcceptLicense() {
        return acceptLicense;
    }

    /**
     * @param acceptLicense
     *            the acceptLicense to set
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
     * @return the archiveLocation
     */
    public String getArchiveLocation() {
        return archiveLocation;
    }

    /**
     * @param 
     *            the archiveLocation to set
     */
    public void setArchiveLocation(String archiveLocation) {
        this.archiveLocation = archiveLocation;
    }
    
}
