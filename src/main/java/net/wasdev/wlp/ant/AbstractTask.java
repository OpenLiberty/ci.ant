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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public abstract class AbstractTask extends Task {

    protected File installDir;
    protected File userDir;
    protected File outputDir;
    protected File serverConfigRoot = null;
    protected File serverOutputDir = null;
    protected String serverName;

    protected String ref;

    protected static String osName;
    protected static boolean isWindows;

    protected ProcessBuilder processBuilder;
    public static String result;

    protected static final String DEFAULT_SERVER = "defaultServer";
    protected static final String DEFAULT_LOG_FILE = "logs/console.log";

    protected static final String WLP_USER_DIR_VAR = "WLP_USER_DIR";
    protected static final String WLP_OUTPUT_DIR_VAR = "WLP_OUTPUT_DIR";

    protected static final String START_MESSAGE_CODE = "CWWKF0011I";
    protected static final String STOP_MESSAGE_CODE = "CWWKE0036I";
    protected static final String START_APP_MESSAGE_CODE_REG = "CWWKZ0001I.*";
    protected static final String STOP_APP_MESSAGE_CODE_REG = "CWWKZ0009I.*";

    protected static final ResourceBundle messages = ResourceBundle.getBundle("net.wasdev.wlp.ant.AntMessages");

    /**
     * Pick and use a consistent set of return codes across all platforms. Most
     * common range is 0 to 256.
     */
    public enum ReturnCode {
        OK(0),
        // started/stopped is set based on operation.
        // process will return this code if start is called when server is
        // already running
        // or will return this code for stop/status when the server is not
        // running
        REDUNDANT_ACTION_STATUS(1), SERVER_NOT_EXIST_STATUS(2), SERVER_ACTIVE_STATUS(
                                                                                     3), SERVER_INACTIVE_STATUS(4),
        // Jump a few numbers for error return codes-- see readInitialConfig
        BAD_ARGUMENT(20), ERROR_SERVER_STOP(21), ERROR_SERVER_START(22), LOCATION_EXCEPTION(
                                                                                            23), LAUNCH_EXCEPTION(24), RUNTIME_EXCEPTION(25), UNKNOWN_EXCEPTION(
                                                                                                                                                                26),
        PROCESS_CLIENT_EXCEPTION(27), ERROR_SERVER_PACKAGE(28), ERROR_SERVER_DUMP(
                                                                                  29), ERROR_SERVER_ATTACH(30);

        final int val;

        ReturnCode(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }

    protected void initTask() {
        if (ref != null) {
            Object serverRef = getProject().getReference(ref);
            if (serverRef != null && (serverRef instanceof ServerTask)) {
                setInstallDir(((ServerTask) serverRef).getInstallDir());
                setServerName(((ServerTask) serverRef).getServerName());
                setUserDir(((ServerTask) serverRef).getUserDir());
                setOutputDir(((ServerTask) serverRef).getOutputDir());
            }

        }
        try {

            if (installDir != null) {
                installDir = installDir.getCanonicalFile();

                // Quick sanity check
                File file = new File(installDir, "lib/ws-launch.jar");
                if (!file.exists()) {
                    throw new BuildException(messages.getString("error.installDir.set"));
                }

                log(MessageFormat.format(messages.getString("info.variable"), "installDir", installDir));

            } else {
                //throw new BuildException("You must set installDir attribute pointing to where your server is located.");
                throw new BuildException(messages.getString("error.installDir.validate"));
            }

            if (serverName == null) {
                setServerName(DEFAULT_SERVER);
            }

            processBuilder = new ProcessBuilder();

            if (userDir != null && userDir.isDirectory()) {
                log(MessageFormat.format(messages.getString("info.variable"), "userDir", userDir.getCanonicalPath()), Project.MSG_VERBOSE);
                processBuilder.environment().put(WLP_USER_DIR_VAR, userDir.getCanonicalPath());
                serverConfigRoot = new File(userDir, "servers/" + serverName);
            } else {
                String wlpUserDir = processBuilder.environment().get(WLP_USER_DIR_VAR);
                if (wlpUserDir != null) {
                    log(MessageFormat.format(messages.getString("info.variable"), "WLP_USER_DIR", wlpUserDir), Project.MSG_VERBOSE);
                    serverConfigRoot = new File(wlpUserDir, "servers/" + serverName);
                } else {
                    serverConfigRoot = new File(installDir, "usr/servers/" + serverName);
                }
            }

            log(MessageFormat.format(messages.getString("info.variable"), "ConfigDir", serverConfigRoot.getCanonicalPath()));

            if (outputDir != null && outputDir.isDirectory()) {
                log(MessageFormat.format(messages.getString("info.variable"), "outputDir", outputDir.getCanonicalPath()), Project.MSG_VERBOSE);
                processBuilder.environment().put(WLP_OUTPUT_DIR_VAR, outputDir.getCanonicalPath());
                serverOutputDir = new File(outputDir, serverName);
            } else {
                String wlpOutputDir = processBuilder.environment().get(WLP_OUTPUT_DIR_VAR);
                if (wlpOutputDir != null) {
                    log(MessageFormat.format(messages.getString("info.variable"), "WLP_OUTPUT_DIR", wlpOutputDir), Project.MSG_VERBOSE);
                    serverOutputDir = new File(wlpOutputDir, serverName);
                } else {
                    serverOutputDir = serverConfigRoot;
                }
            }

            log(MessageFormat.format(messages.getString("info.variable"), "OutputDir", serverOutputDir.getCanonicalPath()));

        } catch (IOException e) {
            throw new BuildException(e);
        }

        // Check for windows..
        osName = System.getProperty("os.name", "unknown").toLowerCase();
        isWindows = osName.indexOf("windows") >= 0;

    }

    public File getInstallDir() {
        return installDir;
    }

    public void setInstallDir(File installDir) {
        this.installDir = installDir;
    }

    public File getUserDir() {
        return userDir;
    }

    public void setUserDir(File userDir) {
        this.userDir = userDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName
     *            the serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public File getLogFile() {
        return new File(serverOutputDir, DEFAULT_LOG_FILE);
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }
    
    protected int getReturnCode(Process p, String commandLine) throws InterruptedException {
        log(MessageFormat.format(messages.getString("info.variable"), "Invoke command", commandLine, Project.MSG_VERBOSE));

        StreamCopier copier = new StreamCopier(p.getInputStream());
        copier.start();

        int exitVal = p.waitFor();
        copier.doJoin();
        return exitVal;        
    }
    
    public void checkReturnCode(Process p, String commandLine, int... expectedExitCodes) throws InterruptedException {
        int exitVal = getReturnCode(p, commandLine);
        
        for (int expectedExitCode : expectedExitCodes) {
            if (exitVal == expectedExitCode) {
                return;
            }
        }

        throw new BuildException(MessageFormat.format(messages.getString("error.invoke.command"), commandLine, exitVal, Arrays.toString(expectedExitCodes)));        
    }

    protected void validateServerStarted(File outputFile, long startTimeout) throws Exception {

        boolean serverStarted = false;

        log("Waiting up to " + (startTimeout / 1000)
            + " seconds for server confirmation:  "
            + START_MESSAGE_CODE.toString() + " to be found in "
            + outputFile);

        try {

            final String startMessage = waitForStringInLog(START_MESSAGE_CODE, startTimeout, outputFile);
            serverStarted = (startMessage != null);

        } catch (Exception e) {
            throw new BuildException(e);
        }

        if (!!!serverStarted) {
            throw new BuildException(messages.getString("error.server.fail"));

        }

    }

    private class StreamCopier extends Thread {
        private final BufferedReader reader;
        private boolean joined;
        private boolean terminated;

        StreamCopier(InputStream input) {
            this.reader = new BufferedReader(new InputStreamReader(input));
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                for (String line; (line = reader.readLine()) != null;) {
                    synchronized (this) {
                        if (joined) {
                            // The main thread was notified that the process
                            // ended and has already given up waiting for
                            // output from the foreground process.
                            break;
                        }
                        if (line != null && !line.equals(""))
                            AbstractTask.result=line;
                        log(line);
                    }
                }
            } catch (IOException ex) {
                throw new BuildException(ex);
            } finally {
                if (isWindows) {
                    synchronized (this) {
                        terminated = true;
                        notifyAll();
                    }
                }
            }
        }

        public void doJoin() throws InterruptedException {
            if (isWindows) {
                // Windows doesn't disconnect background processes (start /b)
                // from the console of foreground processes, so waiting until
                // the end of output from server.bat means waiting until the
                // server process itself ends. We can't wait that long, so we
                // wait one second after .waitFor() ends. Hopefully this will
                // be long enough to copy all the output from the script.

                synchronized (this) {
                    long begin = System.nanoTime();
                    long end = begin
                               + TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
                    long duration = end - begin;
                    while (!terminated && duration > 0) {
                        TimeUnit.NANOSECONDS.timedWait(this, duration);
                        duration = end - System.nanoTime();
                    }

                    // If the thread didn't end after waiting for a second,
                    // then assume it's stuck in a blocking read. Oh well,
                    // it's a daemon thread, so it'll go away eventually. Let
                    // it know that we gave up to avoid spurious output in case
                    // it eventually wakes up.
                    joined = true;
                }
            } else {
                super.join();
            }
        }
    }

    /**
     * Check for a number of strings in a potentially remote file
     * 
     * @param regexp
     *            a regular expression to search for
     * @param timeout
     *            a timeout, in milliseconds
     * @param outputFile
     *            file to check
     * @return line that matched the regexp
     */
    public String waitForStringInLog(String regexp, long timeout,
                                     File outputFile) {
        int waited = 0;
        final int waitIncrement = 500;

        log(MessageFormat.format(messages.getString("info.search.string"), regexp, outputFile.getAbsolutePath(), timeout / 1000));

        try {
            while (waited <= timeout) {
                String string = findStringInFile(regexp, outputFile);
                if (string == null) {
                    try {
                        Thread.sleep(waitIncrement);
                    } catch (InterruptedException e) {
                        // Ignore and carry on
                    }
                    waited += waitIncrement;
                } else {
                    return string;
                }
            }
            log(MessageFormat.format(messages.getString("error.serch.string.timeout"), regexp, outputFile.getAbsolutePath()));
        } catch (Exception e) {
            // I think we can assume if we can't read the file it doesn't
            // contain our string
            throw new BuildException(e);
        }
        return null;

    }

    /**
     * Searches the given file for the given regular expression.
     * 
     * @param regexp
     *            a regular expression (or just a text snippet) to search for
     * @param fileToSearch
     *            the file to search
     * @return The first line which includes the pattern, or null if the pattern
     *         isn't found or if the file doesn't exist
     * @throws Exception
     */
    protected String findStringInFile(String regexp, File fileToSearch) throws Exception {
        String foundString = null;
        List<String> matches = findStringsInFileCommon(regexp, true, -1, fileToSearch);

        if (matches != null && !matches.isEmpty()) {
            foundString = matches.get(0);
        }

        return foundString;

    }

    /**
     * Searches the given file for the given regular expression.
     * 
     * @param regexp
     *            a regular expression (or just a text snippet) to search for
     * @param fileToSearch
     *            the file to search
     * @return List of Strings which match the pattern. No match results in an
     *         empty list.
     * @throws Exception
     */
    protected List<String> findStringsInFileCommon(String regexp,
                                                   boolean stopOnFirst, int searchLimit, File fileToSearch)
                    throws Exception {
        if (fileToSearch == null) {
            log(messages.getString("info.file.validated"));
            return null;
        }

        if (!fileToSearch.exists()) {
            log(MessageFormat.format(messages.getString("info.file.validate.noexist"), fileToSearch.getCanonicalPath()));
            return null;
        }

        InputStream serverOutput = null;
        InputStreamReader in = null;
        Scanner s = null;
        List<String> matches = null;
        try {
            // Read file and search
            serverOutput = new FileInputStream(fileToSearch);
            in = new InputStreamReader(serverOutput);
            s = new Scanner(in);

            log(MessageFormat.format(messages.getString("info.look.string.infile"), regexp, fileToSearch.getName()), Project.MSG_VERBOSE);

            String foundString = null;
            Pattern pattern = Pattern.compile(regexp);

            matches = new ArrayList<String>();
            while (s.hasNextLine()) {
                if (foundString != null && stopOnFirst) {
                    break;
                }

                if (searchLimit <= 0 && searchLimit >= matches.size()) {
                    break;
                }

                String line = s.nextLine();
                if (pattern.matcher(line).find()) {
                    foundString = line;
                    matches.add(line);
                    log(MessageFormat.format(messages.getString("info.match.string"), matches.size(), line));
                }
            }
        } catch (Exception e) {
            log(e.toString());
        } finally {
            try {
                s.close();
            } catch (Exception e) {
                // Ignore
            }

            try {
                serverOutput.close();
            } catch (Exception e) {
                // Ignore
            }

            try {
                in.close();
            } catch (Exception e) {
                //Ignore
            }

        }

        return matches;
    }

    /*
     * Returns file name without the extension.
     */
    protected String getFileName(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? fileName : fileName.substring(0, index);
    }

    protected void stopServer(String timeout) {
        //Stop server if exception happens.
        ServerTask st = new ServerTask();
        st.setProject(getProject());
        st.setInstallDir(getInstallDir());
        st.setUserDir(getUserDir());
        st.setOutputDir(getOutputDir());
        st.setServerName(getServerName());
        st.setTimeout(timeout);
        st.setOperation("stop");
        st.execute();
    }
}
