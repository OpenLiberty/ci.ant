/**
 * (C) Copyright IBM Corporation 2014, 2015, 2016.
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
import java.io.File;
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

    private static final long APP_START_TIMEOUT_DEFAULT = 30 * 1000;

    private final List<FileSet> fileSets = new ArrayList<FileSet>();
    private File appFile;
    private String deployName;

    private String timeout;

    @Override
    public void execute() {

        super.initTask();

        // Check for no arguments
        if ((appFile == null) && (fileSets.size() == 0)) {
            throw new BuildException(getMessage("error.fileset.set"), getLocation());
        }
        if (deployName != null && appFile == null) {
            throw new BuildException(getMessage("error.file.set"), getLocation());
        }

        long appStartTimeout = APP_START_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            appStartTimeout = Long.valueOf(timeout);
        }

        File dropInFolder = new File(serverConfigDir, "dropins");
        // deploy app specified as a file
        if (appFile != null) {
            if (!appFile.exists()) {
                throw new BuildException(getMessage("error.deploy.file.noexist", appFile), getLocation());
            } else if (appFile.isDirectory()) {
                throw new BuildException(getMessage("error.deploy.file.isdirectory", appFile), getLocation());
            }
            File destFile = new File(dropInFolder, deployName == null ? appFile.getName() : deployName);
            deploy(appStartTimeout, appFile, destFile);
        }
        // deploy apps specified as fileSets
        List<File> files = scanFileSets();
        for (File file : files) {
            File destFile = new File(dropInFolder, file.getName());
            deploy(appStartTimeout, file, destFile);
        }
    }

    private void deploy(long appStartTimeout, File srcFile, File destFile) {
        log(getMessage("info.deploy.app", srcFile.getPath()));
        try {
            FileUtils.getFileUtils().copyFile(srcFile, destFile, null, true);
        } catch (IOException e) {
            throw new BuildException(getMessage("error.deploy.fail"));
        }
        // Check start message code
        String startMessage = START_APP_MESSAGE_CODE_REG + getFileName(destFile.getName());
        if (waitForStringInLog(startMessage, appStartTimeout, getLogFile()) == null) {
            throw new BuildException(getMessage("error.deploy.fail", srcFile.getPath()));
        }
    }

    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param fs
     *            the file set to add
     */
    public void addFileset(FileSet fs) {
        fileSets.add(fs);
    }

    public void setFile(File appFile) {
        this.appFile = appFile;
    }

    /**
     * returns the list of files (full path name) to process.
     *
     * @return the list of files included via the filesets.
     */
    private List<File> scanFileSets() {
        final List<File> list = new ArrayList<File>();

        for (int i = 0; i < fileSets.size(); i++) {
            final FileSet fs = fileSets.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            //Throw a BuildException if the directory specified as parameter is empty.
            if (names.length == 0) {
                throw new BuildException(getMessage("error.deploy.fileset.empty"), getLocation());
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
     * @return the deployName
     */
    public String getDeployName() {
        return deployName;
    }

    /**
     * @param name the deployName to set
     */
    public void setDeployName(String name) {
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                throw new BuildException(getMessage("error.parameter.empty", "deployName"));
            }
        }
        this.deployName = name;
    }

}
