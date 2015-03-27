/**
 * (C) Copyright IBM Corporation 2014, 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.wasdev.wlp.ant;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.wasdev.wlp.ant.server.ServerXml;
import net.wasdev.wlp.ant.server.types.Application;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * Deploy ant tasks
 */
public class DeployTask extends AbstractTask {

    private static final long APP_START_TIMEOUT_DEFAULT = 30 * 1000;
    private final List<FileSet> apps = new ArrayList<FileSet>();
    private String filePath;
    private String timeout;

    /**
     * A list of applications to deploy
     */
    private List<Application> applications = new ArrayList<Application>();

    /**
     * This variable indicates if the deploy is going to be to the server xml
     * file. By default is false to create compatibility with previous versions.
     */
    private boolean toServerXml = false;

    /**
     * Add an application to be deployed to a list.
     *
     * @param application
     *            The application to add.
     */
    public void addApplication(Application application) {
        applications.add(application);
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

    @Override
    public void execute() {
        super.initTask();

        long appStartTimeout = APP_START_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            appStartTimeout = Long.valueOf(timeout);
        }

        if (toServerXml == false) {
            if (filePath == null && apps.size() == 0) {
                throw new BuildException(
                        messages.getString("error.fileset.set"), getLocation());
            }

            final List<File> files = scanFileSets();

            File dropInFolder = new File(serverConfigRoot, "dropins");
            for (File file : files) {
                File destFile = new File(dropInFolder, file.getName());
                log(MessageFormat.format(messages.getString("info.deploy.app"),
                        file.getPath()));
                try {
                    FileUtils.getFileUtils().copyFile(file, destFile, null,
                            true);
                } catch (IOException e) {
                    throw new BuildException(
                            messages.getString("error.deploy.fail"));
                }
                // Check start message code
                String startMessage = START_APP_MESSAGE_CODE_REG
                        + getFileName(file.getName());
                if (waitForStringInLog(startMessage, appStartTimeout,
                        getLogFile()) == null) {
                    throw new BuildException(MessageFormat.format(
                            messages.getString("error.deploy.fail"),
                            file.getPath()));
                }
            }
        } else {
            if (applications.size() < 1) {
                log(messages.getString("info.deploy.serverxml.emptylist"));
            } else {

                StringBuilder buffer = new StringBuilder();
                int size = applications.size();

                for (int i = 0; i < size; i++) {
                    buffer.append(applications.get(i).getLocation());
                    if (i + 1 < size) {
                        buffer.append(", ");
                    }
                }

                log(MessageFormat.format(
                        messages.getString("info.deploy.serverxml"),
                        buffer.toString()));

                for (Application application : applications) {
                    if (application == null) {
                        log(messages.getString("info.deploy.serverxml.nullapp"),
                                Project.MSG_DEBUG);
                    } else if (application.getLocation() == null
                            || application.getLocation().isEmpty()) {
                        log(messages
                                .getString("info.deploy.serverxml.invalidapp"),
                                Project.MSG_DEBUG);
                    } else {
                        // Deploying app to the server xml
                        ServerXml server = new ServerXml(new File(
                                serverConfigRoot, "/server.xml"));
                        server.addApplication(application);

                        // Verifying the application in the console log
                        String startMessage = null;

                        if (application.getName() != null
                                && !application.getName().isEmpty()) {
                            startMessage = START_APP_MESSAGE_CODE_REG
                                    + application.getName();
                        } else {
                            String applicationName = new File(
                                    application.getLocation()).getName();
                            applicationName = applicationName.substring(0,
                                    applicationName.length() - 4);
                            startMessage = START_APP_MESSAGE_CODE_REG
                                    + applicationName;
                        }

                        if (waitForStringInLog(startMessage, appStartTimeout,
                                getLogFile()) == null) {
                            throw new BuildException(MessageFormat.format(
                                    messages.getString("error.deploy.fail"),
                                    application.getName()));
                        }
                    }

                }
            }

        }
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
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
                throw new BuildException(MessageFormat.format(
                        messages.getString("error.parameter.invalid"), "file"),
                        getLocation());
            }
            File fileToDeploy = new File(filePath);
            if (!fileToDeploy.exists()) {
                throw new BuildException(MessageFormat.format(
                        messages.getString("error.deploy.file.noexist"),
                        filePath), getLocation());
            } else if (fileToDeploy.isDirectory()) {
                throw new BuildException(
                        messages.getString("error.deploy.file.isdirectory"),
                        getLocation());
            } else {
                list.add(fileToDeploy);
            }
        }

        for (int i = 0; i < apps.size(); i++) {
            final FileSet fs = apps.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            // Throw a BuildException if the directory specified as parameter is
            // empty.
            if (names.length == 0) {
                throw new BuildException(
                        messages.getString("error.deploy.fileset.invalid"),
                        getLocation());
            }

            for (String element : names) {
                list.add(new File(ds.getBasedir(), element));
            }
        }

        return list;
    }

    public void setFile(File app) {
        filePath = app.getAbsolutePath();
    }

    /**
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * Add a flag to perform the deploy to the server xml.
     *
     * @param toServerXml
     *            True if the applications are going to be deployed to the
     *            server xml. Otherwise, false.
     */
    public void setToServerXml(boolean toServerXml) {
        this.toServerXml = toServerXml;
    }

}
