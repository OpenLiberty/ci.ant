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

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * deploy ant tasks
 */
public class DeployTask extends AbstractTask {

    private static final String START_APP_MESSAGE_CODE_REG = "CWWKZ0001I.*";
    
    private final List<FileSet> apps = new ArrayList<FileSet>();
    private String filePath;
    private String timeout;
    private static final long APP_START_TIMEOUT_DEFAULT = 30 * 1000;
    // Deploy destination
    private String deployTo = "dropins";
    private long appStartTimeout;

    @Override
    public void execute() {
        if (!deployTo.equals("dropins") && !deployTo.equals("configDropins")) {
            throw new BuildException(MessageFormat.format(messages.getString("error.parameter.type.invalid"), "deployTo"));
        }

        super.initTask();

        // Check for no arguments
        if ((filePath == null) && (apps.size() == 0)) {
            throw new BuildException(messages.getString("error.fileset.set"), getLocation());
        }

        final List<File> files = scanFileSets();

        appStartTimeout = APP_START_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            appStartTimeout = Long.valueOf(timeout);
        }
        
        if (deployTo.equals("dropins")) {
            deployToDropins(files);
        } else {
            deployToXml(files);
        }
    }

    private void deployToDropins(List<File> files) {
        // Copy the files to serverConfigDir/dropins
        File dropInFolder = new File(serverConfigDir, "dropins");
        for (File file : files) {
            File destFile = new File(dropInFolder, file.getName());
            log(MessageFormat.format(messages.getString("info.deploy.app"), file.getPath()));
            try {
                FileUtils.getFileUtils().copyFile(file, destFile, null, true);
            } catch (IOException e) {
                throw new BuildException(messages.getString("error.deploy.fail"));
            }
            
            // Check the deploy, if it is not correct, don't delete the app file.
            checkDeploy(destFile, false);
        }
    }
    
    private void deployToXml(List<File> files) {
        // Create a new file appName.extension.xml in serverConfigDir/configDropins/overrides
        File overridesFolder = new File(serverConfigDir, "configDropins/overrides");
        
        if (!overridesFolder.exists()) {
            // If directory does not exist, create it
            if (!overridesFolder.mkdirs()) {
                // Fail if it can not be created
                throw new BuildException(MessageFormat.format(messages.getString("error.deploy.fail"), overridesFolder.getPath()));
            }
        }
        
        for (File app : files) {
            // For each file...
            String appName = app.getName();
            String appLocation = app.getAbsolutePath();
            
            // Create a new file in the configDropins/overrides with extension xml
            File xmlApp = new File(overridesFolder, appName + ".xml");
            
            if (xmlApp.exists()) {
                // If app already deployed, send a log and continue to the next one
                log(MessageFormat.format(messages.getString("info.app.already.deployed"), appName));
                continue;
            }
            
            // The xml code to put in the file
            String xml = "<server>\n"
                    + "<application name=\"" + appName + "\" location=\"" + appLocation + "\" />\n"
                    + "</server>";
            
            try {
                // Create the file and add the xml
                xmlApp.createNewFile();
                FileWriter fileWriter = new FileWriter(xmlApp.getAbsoluteFile());
                BufferedWriter buffer = new BufferedWriter(fileWriter);
                buffer.write(xml);
                buffer.close();
            } catch (IOException e) {
                throw new BuildException(MessageFormat.format(messages.getString("error.deploy.fail"), xmlApp.getPath()));
            }
            
            // Check the deploy, if it is not correct, delete the xml file created
            checkDeploy(xmlApp, true);
            
        }
    }
    
    /**
     * Checks the messages.log to verify the deploy of the app
     * @param file The file deployed.
     * @param deleteIfFail If the start log is not found, delete the app file.
     */
    private void checkDeploy(File file, boolean deleteIfFail) {
        // Check start message code
        String startMessage = START_APP_MESSAGE_CODE_REG + getFileName(file.getName());
        if (waitForStringInLog(startMessage, appStartTimeout, getLogFile()) == null) {
            if (deleteIfFail) {
                file.delete();
            }
            throw new BuildException(MessageFormat.format(messages.getString("error.deploy.fail"), file.getPath()));
        }
    }

    /**
     * Adds a set of files (nested fileset attribute).
     * 
     * @param fs
     *            the file set to add
     */
    public void addFileset(FileSet fs) {
        apps.add(fs);
    }

    public void setFile(File app) {
        filePath = app.getAbsolutePath();
    }

    /**
     * returns the list of files (full path name) to process.
     * 
     * @return the list of files included via the filesets.
     */
    private List<File> scanFileSets() {
        final List<File> list = new ArrayList<File>();

        if (filePath != null) {
            filePath = filePath.trim();
            if (filePath.length() == 0) {
                throw new BuildException(MessageFormat.format(messages.getString("error.parameter.invalid"), "file"), getLocation());
            }
            File fileToDeploy = new File(filePath);
            if (!fileToDeploy.exists()) {
                throw new BuildException(MessageFormat.format(messages.getString("error.deploy.file.noexist"), filePath), getLocation());
            } else if (fileToDeploy.isDirectory()) {
                throw new BuildException(messages.getString("error.deploy.file.isdirectory"), getLocation());
            } else {
                list.add(fileToDeploy);
            }
        }

        for (int i = 0; i < apps.size(); i++) {
            final FileSet fs = apps.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            //Throw a BuildException if the directory specified as parameter is empty.
            if (names.length == 0) {
                throw new BuildException(messages.getString("error.deploy.fileset.invalid"), getLocation());
            }

            for (String element : names) {
                list.add(new File(ds.getBasedir(), element));
            }
        }

        return list;
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
    
    /**
     * @return the deploy location
     */
    public String getDeployTo() {
        return deployTo;
    }

    /**
     * @param deployTo The deploy destination. If it is set to 'dropins', the apps will be copied to the dropins folder.
     * Else if the value is 'xml', an entry of type 'application' will be added in the configDropins/overrides/appName.xml
     * file.
     */
    public void setDeployTo(String deployTo) {
        this.deployTo = deployTo;
    }

}
