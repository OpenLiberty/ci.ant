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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.wasdev.wlp.ant.server.ServerXml;
import net.wasdev.wlp.ant.server.types.Application;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * Undeploy ant task
 */
public class UndeployTask extends AbstractTask {

    private static final long APP_STOP_TIMEOUT_DEFAULT = 30 * 1000;
    private String fileName;
    private PatternSet pattern;
    private String timeout;

    /**
     * A list of applications to undeploy
     */
    private List<Application> applications = new ArrayList<Application>();

    /**
     * This variable indicates if the undeploy is going to be from the server
     * xml file. By default is false to create compatibility with previous
     * versions.
     */
    private boolean fromServerXml = false;

    /**
     * Add an application to be undeployed from the server xml file.
     *
     * @param application
     *            The application to remove.
     */
    public void addApplication(Application application) {
        applications.add(application);
    }

    public void addPatternset(PatternSet pattern) {
        this.pattern = pattern;
    }

    @Override
    public void execute() {
        super.initTask();

        long appStopTimeout = APP_STOP_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            appStopTimeout = Long.valueOf(timeout);
        }

        if (fromServerXml == false) {
            final List<File> files = scanFileSets();

            for (File file : files) {
                log(MessageFormat.format(messages.getString("info.undeploy"),
                        file.getName()));
                FileUtils.delete(file);

                // check stop message code
                String stopMessage = STOP_APP_MESSAGE_CODE_REG
                        + getFileName(file.getName());
                if (waitForStringInLog(stopMessage, appStopTimeout,
                        getLogFile()) == null) {
                    throw new BuildException(MessageFormat.format(
                            messages.getString("error.undeploy.fail"),
                            file.getPath()));
                }
            }
        } else {
            if (applications.size() < 1) {
                log(messages.getString("info.undeploy.serverxml.emptylist"));
            }

            StringBuilder buffer = new StringBuilder();
            int size = applications.size();

            for (int i = 0; i < size; i++) {
                buffer.append(applications.get(i).getLocation());
                if (i + 1 < size) {
                    buffer.append(", ");
                }
            }

            log(MessageFormat.format(
                    messages.getString("info.undeploy.serverxml"),
                    buffer.toString()));

            for (Application application : applications) {
                if (application == null) {
                    log(messages.getString("info.deploy.serverxml.nullapp"),
                            Project.MSG_DEBUG);
                } else if (application.getLocation() == null
                        || application.getLocation().isEmpty()) {
                    log(messages.getString("info.deploy.serverxml.invalidapp"),
                            Project.MSG_DEBUG);
                } else {
                    // Removing app from the server xml
                    ServerXml server = new ServerXml(new File(serverConfigRoot,
                            "/server.xml"));
                    server.removeApplication(application);

                    // Verifying the removed application in the console log
                    String stopMessage = null;

                    if (application.getName() != null
                            && !application.getName().isEmpty()) {
                        stopMessage = STOP_APP_MESSAGE_CODE_REG
                                + application.getName();
                    } else {
                        String applicationName = new File(
                                application.getLocation()).getName();
                        applicationName = applicationName.substring(0,
                                applicationName.length() - 4);
                        stopMessage = STOP_APP_MESSAGE_CODE_REG
                                + applicationName;
                    }

                    if (waitForStringInLog(stopMessage, appStopTimeout,
                            getLogFile()) == null) {
                        throw new BuildException(MessageFormat.format(
                                messages.getString("error.undeploy.fail"),
                                application.getName()));
                    }
                }
            }

        }

    }

    public String getFile() {
        return fileName;
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return timeout;
    }

    private List<File> scanFileSets() throws BuildException {
        File dropinsDir = new File(serverConfigRoot, "dropins");
        final List<File> list = new ArrayList<File>();

        if (fileName != null) {
            File fileUndeploy = new File(dropinsDir, fileName);
            if (fileUndeploy.exists()) {
                list.add(fileUndeploy);
            } else {
                throw new BuildException(MessageFormat.format(
                        messages.getString("error.undeploy.file.noexist"),
                        fileUndeploy.getPath()));
            }
        } else {
            FileSet dropins = new FileSet();
            dropins.setDir(dropinsDir);

            if (pattern != null) {
                dropins.appendIncludes(pattern.getIncludePatterns(getProject()));
                dropins.appendExcludes(pattern.getExcludePatterns(getProject()));
            }

            final DirectoryScanner ds = dropins
                    .getDirectoryScanner(getProject());
            ds.scan();
            final String[] names = ds.getIncludedFiles();

            if (names.length == 0) {
                throw new BuildException(
                        messages.getString("error.undeploy.fileset.invalid"));
            }

            for (String element : names) {
                list.add(new File(ds.getBasedir(), element));
            }
        }
        return list;
    }

    public void setFile(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Add a flag to perform the undeploy from the server xml.
     *
     * @param undeployFromServerXml
     *            True if the applications are going to be undeployed from the
     *            server xml. Otherwise, false.
     */
    public void setFromServerXml(boolean fromServerXml) {
        this.fromServerXml = fromServerXml;
    }

    /**
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

}
