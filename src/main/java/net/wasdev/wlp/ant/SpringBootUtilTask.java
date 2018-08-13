/**
 * (C) Copyright IBM Corporation 2018.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

/**
 * Install feature task.
 */
public class SpringBootUtilTask extends AbstractTask {

    // The path of the source application file to thin.
    private String sourceAppPath;

    // The directory path that is used to save the library cache.
    private String targetLibCachePath;

    // The path that is used to save the thin application file.
    private String targetThinAppPath;

    // The directory path of the parent read-only library cache.
    private String parentLibCachePath;

    private String command_name = "springBootUtility";

    // full path + name of command
    private String cmd;

    protected void initTask() {
        super.initTask();

        if (isWindows) {
            cmd = installDir + "\\bin\\" + command_name + ".bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            cmd = installDir + "/bin/" + command_name;
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);
        processBuilder.redirectErrorStream(true);
    }

    @Override
    public void execute() {

        initTask();

        File f = new File(cmd);
        if (f.exists()) {
            try {
                processCommand(buildCommand());
            } catch (BuildException e) {
                throw e;
            } catch (Exception e) {
                throw new BuildException(e);
            }
        } else {
            throw new BuildException("An invocation of the " + command_name
                    + " command failed. The command is not available on the targeted Liberty runtime.");
        }

    }

    /**
     * Build up a command string to launch in new process
     */
    private List<String> buildCommand() {
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("thin");
        command.add("--sourceAppPath=" + getSourceAppPath());
        command.add("--targetLibCachePath=" + getTargetLibCachePath());
        command.add("--targetThinAppPath=" + getTargetThinAppPath());
        if (getParentLibCachePath() != null) {
            command.add("--parentLibCachePath=" + getParentLibCachePath());
        }
        return command;
    }

    /**
     * Process the command.
     * 
     * @param command
     *            A String list containing the command to be executed.
     */
    private void processCommand(List<String> command) throws Exception {
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), 0);
    }

    public String getSourceAppPath() {
        return sourceAppPath;
    }

    public void setSourceAppPath(String sourceAppPath) {
        this.sourceAppPath = sourceAppPath;
    }

    public String getTargetLibCachePath() {
        return targetLibCachePath;
    }

    public void setTargetLibCachePath(String targetLibCachePath) {
        this.targetLibCachePath = targetLibCachePath;
    }

    public String getParentLibCachePath() {
        return parentLibCachePath;
    }

    public void setParentLibCachePath(String parentLibCachePath) {
        this.parentLibCachePath = parentLibCachePath;
    }

    public String getTargetThinAppPath() {
        return targetThinAppPath;
    }

    public void setTargetThinAppPath(String targetThinAppPath) {
        this.targetThinAppPath = targetThinAppPath;
    }
}
