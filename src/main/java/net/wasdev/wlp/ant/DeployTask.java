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
import java.io.File;
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

    private final List<FileSet> apps = new ArrayList<FileSet>();

    private String filePath;

    private File dropInFolder;

    private String timeout;

    private static long APP_START_TIMEOUT_DEFAULT = 30 * 1000;

    @Override
    public void execute() {

        super.initTask();

        // Check for no arguments
        if ((filePath == null) && (apps.size() == 0)) {
            throw new BuildException(messages.getString("error.fileset.set"), getLocation());
        }

        final List<File> files = scanFileSets();

            dropInFolder = new File(serverConfigRoot, "dropins/");
            for (File f : files) {

                File destFile = new File(dropInFolder, f.getName());
                log(MessageFormat.format(messages.getString("info.deploy.app"), f.getPath()));
                try {
                    FileUtils.getFileUtils().copyFile(f, destFile, null, true);
                } catch (IOException e) {
                    throw new BuildException(messages.getString("error.deploy.fail"));
                }
                // Check start message code
                String appName = f.getName();

                long appStartTimeout = APP_START_TIMEOUT_DEFAULT;
                if (timeout != null && !timeout.equals("")) {
                    appStartTimeout = Long.valueOf(timeout);
                }

                String startMessage = START_APP_MESSAGE_CODE_REG + appName.substring(0, appName.indexOf("."));
                if (waitForStringInLog(startMessage, appStartTimeout, getLogFile()) == null) {
                    throw new BuildException(MessageFormat.format(messages.getString("error.deploy.fail"), f.getPath()));
                }
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
            }
            
            else if (fileToDeploy.isDirectory()) {
                    throw new BuildException(messages.getString("error.deploy.file.isdirectory"), getLocation());
                }
                else {
                    list.add(fileToDeploy);
                }
            }


        for (int i = 0; i < apps.size(); i++) {
            final FileSet fs = apps.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            //Throw a BuildException if the directory specified as parameter is empty.
            if(names.length == 0) {
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

}
