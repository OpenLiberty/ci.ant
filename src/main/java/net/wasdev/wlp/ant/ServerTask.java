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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * server operations task: start/stop/package/create/status/debug
 */
public class ServerTask extends AbstractTask {

    private String operation;
    private String timeout;

    private String wlp;

    private static final long SERVER_START_TIMEOUT_DEFAULT = 30 * 1000;
    private static final long SERVER_STOP_TIMEOUT_DEFAULT = 30 * 1000;
    
    // used with 'start' and 'debug' operations
    private boolean clean = false;
    
    // used with 'status' operation
    private String resultProperty;

    // used with 'dump', and 'package' operations
    private File archive;
    
    // used with 'dump', 'javadump', and 'package' operations
    private String include;
    
    // used with 'create' operation
    private String template;
    
    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            wlp = installDir + "\\bin\\server.bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            wlp = installDir + "/bin/server";
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
            throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"), "operation"));
        }

        initTask();

        try {
            if ("create".equals(operation)) {
                doCreate();
            } else if ("run".equals(operation)) {
                doRun();
            } else if ("start".equals(operation)) {
                doStart();
            } else if ("stop".equals(operation)) {
                doStop();
            } else if ("status".equals(operation)) {
                doStatus();
            } else if ("debug".equals(operation)) {
                // Debug seems useless in ant tasks, but still keep it.
                doDebug();
            } else if ("package".equals(operation)) {
                doPackage();
            } else if ("dump".equals(operation)) {
                doDump();
            } else if ("javadump".equals(operation)) {
                doJavaDump();
            } else {
                throw new BuildException("Unsupported operation: " + operation);
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
        
    private void doStart() throws Exception {
        // create server first if it doesn't exist        
        if (!serverConfigRoot.exists()) {
            log(MessageFormat.format(messages.getString("info.server.create"), serverName));
            doCreate();
        }
        List<String> command = getInitialCommand(operation);
        addCleanOption(command);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue(), ReturnCode.REDUNDANT_ACTION_STATUS.getValue());

        // check server started message code.
        long startTimeout = SERVER_START_TIMEOUT_DEFAULT;
        if (timeout != null && !timeout.equals("")) {
            startTimeout = Long.valueOf(timeout);
        }
        validateServerStarted(getLogFile(), startTimeout);
    }

    private void doRun() throws Exception {
        // create server first if it doesn't exist        
        if (!serverConfigRoot.exists()) {
            log(MessageFormat.format(messages.getString("info.server.create"), serverName));
            doCreate();
        }
        List<String> command = getInitialCommand(operation);
        addCleanOption(command);
        processBuilder.command(command);
        final Process p = processBuilder.start();
        final AtomicBoolean shutdown = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown.set(true);                
                List<String> stopCommand = getInitialCommand("stop");
                processBuilder.command(stopCommand);
                try {
                    Process p = processBuilder.start();
                    p.waitFor();
                } catch (Exception e) {
                    log("Error stopping server", e, Project.MSG_WARN);
                }
            }
        });
        int exitCode = getReturnCode(p, processBuilder.command().toString());
        if (!shutdown.get() && exitCode != ReturnCode.OK.getValue()) {
            throw new BuildException(MessageFormat.format(messages.getString("error.invoke.command"), processBuilder.command().toString(), exitCode, ReturnCode.OK.getValue()));
        }
    }
    
    private void doStop() throws Exception {
        List<String> command = getInitialCommand(operation);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue(), ReturnCode.REDUNDANT_ACTION_STATUS.getValue());
    }
    
    private void doStatus() throws Exception {
        List<String> command = getInitialCommand(operation);
        processBuilder.command(command);
        Process p = processBuilder.start();
        int exitCode = getReturnCode(p, processBuilder.command().toString());
        if (resultProperty == null) {
            resultProperty = "wlp." + serverName + ".status";                                       
        }
        getProject().setUserProperty(resultProperty, String.valueOf(exitCode));
    }
    
    private void doDump() throws Exception {
        List<String> command = getInitialCommand(operation);
        addArchiveOption(command);
        addIncludeOption(command);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }
    
    private void doJavaDump() throws Exception {
        List<String> command = getInitialCommand(operation);
        addIncludeOption(command);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }
    
    private void doPackage() throws Exception {
        List<String> command = getInitialCommand(operation);
        addArchiveOption(command);
        addIncludeOption(command);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }
        
    private void doCreate() throws Exception {
        List<String> command = getInitialCommand("create");
        if (template != null) {
            command.add("--template=" + template);
        }
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }
    
    private void doDebug() throws Exception {
        List<String> command = getInitialCommand(operation);
        addCleanOption(command);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }

    private List<String> getInitialCommand(String operation) {
        List<String> commands = new ArrayList<String>();
        commands.add(wlp);
        commands.add(operation);
        if (serverName != null && !serverName.equals("")) {
            commands.add(serverName);
        }
        return commands;
    }
    
    private void addArchiveOption(List<String> command) {
        if (archive != null) {
            if (archive.isDirectory()) {
                throw new BuildException("The archive attribute must specify a file");
            }
            command.add("--archive=" + archive);
        }
    }
    
    private void addIncludeOption(List<String> command) {
        if (include != null) {
            command.add("--include=" + include);
        }
    }
    
    private void addCleanOption(List<String> command) {
        if (clean) {
            command.add("--clean");
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
     * @return the archive
     */
    public File getArchive() {
        return archive;
    }

    /**
     * @param archive
     *            the archive to set
     */
    public void setArchive(File archive) {
        this.archive = archive;
    }

    /**
     * @return the clean
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * @param clean
     *            the clean to set
     */
    public void setClean(boolean clean) {
        this.clean = clean;
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
    
    public String getResultProperty() {
        return resultProperty;
    }

    public void setResultProperty(String resultProperty) {
        this.resultProperty = resultProperty;
    }
    
    public String getInclude() {
        return include;
    }
    
    public void setInclude(String include) {
        this.include = include;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
            
}
