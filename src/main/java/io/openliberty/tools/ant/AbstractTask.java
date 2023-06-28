/**
 * (C) Copyright IBM Corporation 2014, 2023.
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
package io.openliberty.tools.ant;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.StringBuilder;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public abstract class AbstractTask extends Task {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    protected File installDir;
    protected File userDir;
    protected File outputDir;
    protected File serverConfigDir = null;
    protected File serverOutputDir = null;
    protected String serverName;

    protected String ref;

    protected static String osName;
    protected static boolean isWindows;

    protected ProcessBuilder processBuilder;

    private static final String errorRegex = ".*\\[ERROR.*\\].*";
    private static final Pattern errorPattern = Pattern.compile(errorRegex);
    private static final String warnRegex = ".*\\[WARN.*\\].*";
    private static final Pattern warnPattern = Pattern.compile(warnRegex);
    private static final String LIBERTY_MESSAGE_TYPE_REGEX = "(.*\\[)(.*)(\\] [\\S]*?(\\S):.*)";
    private static final Pattern LIBERTY_MESSAGE_TYPE_PATTERN = Pattern.compile(LIBERTY_MESSAGE_TYPE_REGEX);

    protected static final String DEFAULT_SERVER = "defaultServer";
    protected static final String DEFAULT_LOG_FILE = "logs/messages.log";

    protected static final String WLP_USER_DIR_VAR = "WLP_USER_DIR";
    protected static final String WLP_OUTPUT_DIR_VAR = "WLP_OUTPUT_DIR";

    protected static final ResourceBundle messages = ResourceBundle.getBundle("io.openliberty.tools.ant.AntMessages");

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

                // Quick check to ensure a Liberty installation exists at installDir
                File file = new File(installDir, "lib/ws-launch.jar");
                if (!file.exists()) {
                    throw new BuildException(messages.getString("error.installDir.set"));
                }

                log(MessageFormat.format(messages.getString("info.variable"), "installDir", installDir.getCanonicalPath()),
                        Project.MSG_VERBOSE);
            } else {
                throw new BuildException("Liberty installation directory must be set.");
            }

            if (serverName == null) {
                setServerName(DEFAULT_SERVER);
            }

            processBuilder = new ProcessBuilder();

            if (userDir != null) {
                log(MessageFormat.format(messages.getString("info.variable"), "userDir", userDir.getCanonicalPath()), Project.MSG_VERBOSE);
                processBuilder.environment().put(WLP_USER_DIR_VAR, userDir.getCanonicalPath());
                serverConfigDir = new File(userDir, "servers/" + serverName);
            } else {
                String wlpUserDir = processBuilder.environment().get(WLP_USER_DIR_VAR);
                if (wlpUserDir != null) {
                    log(MessageFormat.format(messages.getString("info.variable"), "WLP_USER_DIR", wlpUserDir), Project.MSG_VERBOSE);
                    serverConfigDir = new File(wlpUserDir, "servers/" + serverName);
                } else {
                    serverConfigDir = new File(installDir, "usr/servers/" + serverName);
                }
            }

            log(MessageFormat.format(messages.getString("info.variable"), "server.config.dir", serverConfigDir.getCanonicalPath()),
                    Project.MSG_VERBOSE);

            if (outputDir != null) {
                log(MessageFormat.format(messages.getString("info.variable"), "outputDir", outputDir.getCanonicalPath()),
                        Project.MSG_VERBOSE);
                processBuilder.environment().put(WLP_OUTPUT_DIR_VAR, outputDir.getCanonicalPath());
                serverOutputDir = new File(outputDir, serverName);
            } else {
                String wlpOutputDir = processBuilder.environment().get(WLP_OUTPUT_DIR_VAR);
                if (wlpOutputDir != null) {
                    log(MessageFormat.format(messages.getString("info.variable"), "WLP_OUTPUT_DIR", wlpOutputDir), Project.MSG_VERBOSE);
                    serverOutputDir = new File(wlpOutputDir, serverName);
                } else {
                    serverOutputDir = serverConfigDir;
                }
            }

            log(MessageFormat.format(messages.getString("info.variable"), "server.output.dir", serverOutputDir.getCanonicalPath()),
                    Project.MSG_VERBOSE);
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

    public File getServerOutputDir() {
        return serverOutputDir;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName the serverName to set
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
        copier.cleanup();

        return exitVal;
    }

    public void checkReturnCode(Process p, String commandLine, int... expectedExitCodes) throws InterruptedException {
        int exitVal = getReturnCode(p, commandLine);

        for (int expectedExitCode : expectedExitCodes) {
            if (exitVal == expectedExitCode) {
                return;
            }
        }

        sendErrorInvokeCommand(commandLine, exitVal, expectedExitCodes);
    }

    public void checkReturnCodeAndError(Process p, String commandLine, int expectedExitCode, int allowedExitCode,
            String allowedErrorMessage) throws InterruptedException {
        log(MessageFormat.format(messages.getString("info.variable"), "Invoke command", commandLine, Project.MSG_VERBOSE));

        StreamCopier copier = new StreamCopier(p.getInputStream());
        copier.start();

        int exitVal = p.waitFor();
        copier.doJoin();
        String stdOutAndError = copier.getOutput();
        copier.cleanup();

        if (exitVal == expectedExitCode) {
            return;
        } else if ((exitVal == allowedExitCode) && stdOutAndError.startsWith(allowedErrorMessage)) {
            log("The command " + commandLine + " failed with return code 21 with this error message: " + stdOutAndError, Project.MSG_WARN);
            return;
        }

        sendErrorInvokeCommand(commandLine, exitVal, expectedExitCode);
    }

    public void sendErrorInvokeCommand(String commandLine, int returnCode, int... expectedExitCodes) {
        throw new BuildException(MessageFormat.format(messages.getString("error.invoke.command"), commandLine, returnCode,
                Arrays.toString(expectedExitCodes)));
    }

    private class StreamCopier extends Thread {
        private final BufferedReader reader;
        private boolean joined;
        private boolean terminated;
        private StringBuilder sb;

        StreamCopier(InputStream input) {
            this.reader = new BufferedReader(new InputStreamReader(input));
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                sb = new StringBuilder();
                for (String line; (line = reader.readLine()) != null;) {
                    synchronized (this) {
                        if (joined) {
                            // The main thread was notified that the process
                            // ended and has already given up waiting for
                            // output from the foreground process.
                            break;
                        }
                        sb.append(line);
                        logWithColor(line);
                    }
                }
            } catch (IOException ex) {
                sb.setLength(0);
                throw new BuildException(ex);
            } finally {
                if (isWindows) {
                    synchronized (this) {
                        terminated = true;
                        notifyAll();
                    }
                }
                try {
                    this.reader.close();
                } catch (IOException e) {
                }
            }
        }

        private void logWithColor(String line) {
            Matcher m = LIBERTY_MESSAGE_TYPE_PATTERN.matcher(line);
            // Group 2 - liberty log severity text
            // Group 4 - liberty log identifier code
            if (m.find()) {
                String identifier = m.group(4);
                switch (identifier) {
                    case "E":
                        log(m.group(1) + ANSI_RED + m.group(2) + ANSI_RESET + m.group(3));
                        break;
                    case "W":
                        log(m.group(1) + ANSI_YELLOW + m.group(2) + ANSI_RESET + m.group(3));
                        break;
                    default:
                        log(line);
                }
            }
        }

        public String getOutput() {
            if (sb != null) {
                return sb.toString();
            }
            return "";
        }

        public void cleanup() {
            if (sb != null) {
                sb.setLength(0);
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
                    long end = begin + TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
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
     * @param regexp a regular expression to search for
     * @param timeout a timeout, in milliseconds
     * @param outputFile file to check
     * 
     * @return line that matched the regexp
     */
    public String waitForStringInLog(String regexp, long timeout, File outputFile) {
        long waited = 0;
        final long waitIncrement = 500;

        log(MessageFormat.format(messages.getString("info.search.string"), regexp, outputFile.getAbsolutePath(), timeout / 1000));

        while (waited <= timeout) {
            String string = findStringInFile(regexp, outputFile, Project.MSG_INFO);
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

        return null;

    }

    /**
     * Searches the given file for the given regular expression.
     *
     * @param regexp a regular expression (or just a text snippet) to search for
     * @param fileToSearch the file to search
     * 
     * @return The first line which includes the pattern, or null if the pattern isn't found or if the file doesn't exist
     */
    public String findStringInFile(String regexp, File fileToSearch) {
        return findStringInFile(regexp, fileToSearch, Project.MSG_VERBOSE);
    }

    /**
     * Searches the given file for the given regular expression.
     *
     * @param regexp a regular expression (or just a text snippet) to search for
     * @param fileToSearch the file to search
     * 
     * @return List of lines which includes the pattern, or null if the pattern isn't found or if the file doesn't exist
     */
    public List<String> findStringsInFile(String regexp, File fileToSearch) {
        return findStringsInFileCommon(regexp, false, -1, fileToSearch, Project.MSG_VERBOSE);
    }

    /**
     * Searches the given file for the given regular expression.
     *
     * @param regexp a regular expression (or just a text snippet) to search for
     * @param fileToSearch the file to search
     * @param msgLevel the log level for the match number message
     * 
     * @return The first line which includes the pattern, or null if the pattern isn't found or if the file doesn't exist
     */
    private String findStringInFile(String regexp, File fileToSearch, int msgLevel) {
        String foundString = null;
        List<String> matches = findStringsInFileCommon(regexp, true, -1, fileToSearch, msgLevel);

        if (matches != null && !matches.isEmpty()) {
            foundString = matches.get(0);
        }

        return foundString;
    }

    /**
     * Searches the given file for the given regular expression.
     *
     * @param regexp a regular expression (or just a text snippet) to search for
     * @param fileToSearch the file to search
     * @param msgLevel the log level for the match number message
     * 
     * @return List of Strings which match the pattern. No match results in an empty list.
     */
    private List<String> findStringsInFileCommon(String regexp, boolean stopOnFirst, int searchLimit, File fileToSearch, int msgLevel) {
        if (fileToSearch == null) {
            log(messages.getString("info.file.validated"));
            return null;
        }

        if (!fileToSearch.exists()) {
            try {
                log(MessageFormat.format(messages.getString("info.file.validate.noexist"), fileToSearch.getCanonicalPath()));
            } catch (IOException e) {
                // file doesn't exist anyways, so just print the absolute path for the message
                log(MessageFormat.format(messages.getString("info.file.validate.noexist"), fileToSearch.getAbsolutePath()));
            }
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
                    log(MessageFormat.format(messages.getString("info.match.string"), matches.size(), line), msgLevel);
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
                // Ignore
            }

        }

        return matches;
    }

    /**
     * Searches the given file for the given regular expression, and returns the number of times it occurs.
     * 
     * @param regexp a regular expression (or just a text snippet) to search for
     * @param fileToSearch the file to search
     * 
     * @return the number of occurrences of the regular expression
     */
    public int countStringOccurrencesInFile(String regexp, File fileToSearch) {
        List<String> lines = findStringsInFileCommon(regexp, false, -1, fileToSearch, Project.MSG_VERBOSE);

        if (lines == null) {
            return 0;
        }
        return lines.size();
    }

    /**
     * Wait until the number of occurrences of the regular expression in the file increases above the given number of previous
     * occurrences.
     *
     * @param regexp a regular expression to search for
     * @param timeout a timeout, in milliseconds
     * @param outputFile file to check
     * @param previousOccurrences . the previous number of occurrences of the regular expression
     * 
     * @return updated line that matched the regexp
     */
    public String waitForUpdatedStringInLog(String regexp, long timeout, File outputFile, int previousOccurrences) {
        long waited = 0;
        final long waitIncrement = 500;

        log(MessageFormat.format(messages.getString("info.search.string"), regexp, outputFile.getAbsolutePath(), timeout / 1000));

        while (waited <= timeout) {
            List<String> matches = findStringsInFileCommon(regexp, false, -1, outputFile, Project.MSG_VERBOSE);
            if (matches == null || matches.size() <= previousOccurrences) {
                try {
                    Thread.sleep(waitIncrement);
                } catch (InterruptedException e) {
                    // Ignore and carry on
                }
                waited += waitIncrement;
            } else {
                String line = matches.get(matches.size() - 1);
                log(MessageFormat.format(messages.getString("info.match.string"), matches.size(), line), Project.MSG_INFO);
                return line;
            }
        }
        log(MessageFormat.format(messages.getString("error.serch.string.timeout"), regexp, outputFile.getAbsolutePath()));

        return null;

    }

    /*
     * Returns file name without the extension.
     */
    protected String getFileName(String fileName) {
        if (fileName.endsWith(".xml")) { // Handle loose app case for deploy
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }

        int index = fileName.lastIndexOf('.');
        return (index == -1) ? fileName : fileName.substring(0, index);
    }

    protected void stopServer(String timeout) {
        // Stop server if exception happens.
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

    protected String getMessage(String key, Object... args) {
        return MessageFormat.format(messages.getString(key), args);
    }
}
