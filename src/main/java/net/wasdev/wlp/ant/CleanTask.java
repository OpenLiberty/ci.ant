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

import java.io.File;
import java.text.MessageFormat;

import org.apache.tools.ant.BuildException;

/** 
 * Removes the apps deployed to the apps/dropins folder, the logs in the serverOutputDir and every file in the workarea folder depending
 * on the type of cleaning.
 */
public class CleanTask extends AbstractTask {

    private boolean logs = true;
    private boolean workarea = true;
    private boolean dropins = false;
    private boolean apps = false;

    @Override
    public void execute() throws BuildException {

        initTask();
        try {
            doClean();
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public void doClean() throws Exception {
        if (logs) {
            cleanLogs();
        }
        if (workarea) {
            cleanWorkArea();
        }
        if (dropins) {
            cleanDropins();
        }
        if (apps) {
            cleanApps();
        }
    }

    /** Delete every file/folder in the serverOutputDir/logs dir.
     *  @throws Exception
     */
    private void cleanLogs() throws Exception {
        File logsDir = new File(serverOutputDir.getAbsolutePath(), "logs");
        if (logsDir.exists()) {
            cleanDir(logsDir);
            log(MessageFormat.format(messages.getString("info.element.cleaned"), "logs"));
        } else {
            log(MessageFormat.format(messages.getString("info.directory.noexist"), logsDir.getAbsolutePath()));
        }
    }

    /** Delete every file/folder in the serverOutputDir/workarea dir.
     */
    private void cleanWorkArea() {
        File workareaDir = new File(serverOutputDir.getAbsolutePath(), "workarea");
        if (workareaDir.exists()) {
            cleanDir(workareaDir);
            log(MessageFormat.format(messages.getString("info.element.cleaned"), "workarea"));
        } else {
            log(MessageFormat.format(messages.getString("info.directory.noexist"), workareaDir.getAbsolutePath()));
        }
    }


    /** Delete every file/folder in the serverConfigDir/dropins dir.
     */
    private void cleanDropins() {
        File dropinsDir = new File(serverConfigDir.getAbsolutePath(), "dropins");
        if (dropinsDir.exists()) {
            cleanDir(dropinsDir);
            log(MessageFormat.format(messages.getString("info.element.cleaned"), "dropins"));
        } else {
            log(MessageFormat.format(messages.getString("info.directory.noexist"), dropinsDir.getAbsolutePath()));
        }
    }

    /** Delete every file/folder in the serverConfigDir/apps dir.
     */
    private void cleanApps() {
        File AppsDir = new File(serverConfigDir.getAbsolutePath(), "apps");
        if (AppsDir.exists()) {
            cleanDir(AppsDir);
            log(MessageFormat.format(messages.getString("info.element.cleaned"), "apps"));
        } else {
            log(MessageFormat.format(messages.getString("info.directory.noexist"), AppsDir.getAbsolutePath()));
        }
    }

    /** Delete every file/folder inside the dir directory.
     * @param dir The directory to be cleaned.
     */
    private void cleanDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                // if file is a directory, first delete all the files inside, then delete the directory itself.
                if (file.isDirectory()) {
                    cleanDir(file);
                }
                if (!file.delete()) {
                    throw new BuildException(MessageFormat.format(messages.getString("error.cannot.delete.file"), file.getAbsolutePath()));
                }
            }
        }
    }

    /**
     * @return the logs
     */
    public boolean isLogs() {
        return logs;
    }

    /**
     * @param logs Clean the serverOutputDir/logs dir
     */
    public void setLogs(boolean logs) {
        this.logs = logs;
    }

    /**
     * @return the workarea
     */
    public boolean isWorkarea() {
        return workarea;
    }

    /**
     * @param workarea Clean the serverOutputDir/workarea dir
     */
    public void setWorkarea(boolean workarea) {
        this.workarea = workarea;
    }

    /**
     * @return the dropins
     */
    public boolean isDropins() {
        return dropins;
    }

    /**
     * @param dropins Clean the serverConfigDir/dropins dir
     */
    public void setDropins(boolean dropins) {
        this.dropins = dropins;
    }

    /**
     * @return the apps
     */
    public boolean isApps() {
        return apps;
    }

    /**
     * @param apps Clean the serverConfigDir/apps dir
     */
    public void setApps(boolean apps) {
        this.apps = apps;
    }

}