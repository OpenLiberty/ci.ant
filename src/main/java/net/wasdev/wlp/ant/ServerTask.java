/**
 * (C) Copyright IBM Corporation 2014, 2019.
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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import net.wasdev.wlp.ant.types.EmbeddedServerInfo;

/**
 * server operations task: start/stop/package/create/status/debug
 */
public class ServerTask extends AbstractTask {

    
    private String operation;
    private String timeout;
    private boolean useEmbeddedServer;

    private String wlp;
    
    private static final String[] EMPTY_ARRAY = new String[0];
    private static URLClassLoader embeddedServerClassLoader = null;
    private String embeddedServerJar = null;
    
    // The embedded server instance
    private Object embeddedServer;
    
    // Server methods
    private Method embeddedServerStart;
    private Method embeddedServerStop;
    private Method embeddedServerIsRunning;
    
    // Future result methods
    private Method embeddedServerResultSuccessful;
    private Method embeddedServerResultReturnCode;
    private Method embeddedServerResultException;

    private static final String START_MESSAGE_CODE = "CWWKF0011I";
    
    private static final long SERVER_START_TIMEOUT_DEFAULT = 30 * 1000;
    
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
    private boolean noPassword = false;
   
    // used with 'package' operation
    private String os;
    
    @Override
    protected void initTask() {
        super.initTask();
                
        String binDirectory;
        if (isWindows) {
            binDirectory = installDir + "\\bin\\";
            embeddedServerJar = binDirectory + "tools\\ws-server.jar";
            wlp = "\"" + binDirectory + "server.bat" + "\"";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            binDirectory = installDir + "/bin/";
            embeddedServerJar = binDirectory + "tools/ws-server.jar";
            wlp = binDirectory + "server";
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

        if (getRuntimeConfigurableWrapper().getAttributeMap().get("id") == null) {
            if ((operation == null || operation.length() <= 0)) {
                throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"), "operation"));
            }
        }

        if (operation != null) {
            try {
              if ("create".equals(operation)) {
                  if(useEmbeddedServer) {
                      throw new BuildException("Server cannot be created in embedded mode.");
                  }
                  doCreate();
              } else if ("run".equals(operation)) {
                  if(useEmbeddedServer) {
                      doEmbeddedRun();
                  }
                  else {
                      doRun();
                  }
              } else if ("start".equals(operation)) {
                  if(useEmbeddedServer) {
                      doEmbeddedStart();
                  }
                  else {
                      doStart();
                  }
              } else if ("stop".equals(operation)) {
                  if(useEmbeddedServer) {
                      doEmbeddedStop();
                  }
                  else {
                      doStop();
                  }
              } else if ("status".equals(operation)) {
                  if(useEmbeddedServer) {
                      throw new BuildException("Server status cannot be retrieved in embedded mode.");
                  }
                  doStatus();
              } else if ("debug".equals(operation)) {
                  // Debug seems useless in ant tasks, but still keep it.
                  if(useEmbeddedServer) {
                      throw new BuildException("Server debug cannot be run in embedded mode. Please debug from the JVM.");
                  }
                  doDebug();
              } else if ("package".equals(operation)) {
                  if(useEmbeddedServer) {
                      throw new BuildException("Server cannot be packaged in embedded mode.");
                  }
                  doPackage();
              } else if ("dump".equals(operation)) {
                  if(useEmbeddedServer) {
                      throw new BuildException("Server cannot be dumped in embedded mode.");
                  }
                  doDump();
              } else if ("javadump".equals(operation)) {
                  if(useEmbeddedServer) {
                      throw new BuildException("Server cannot be dumped in embedded mode.");
                  }
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
    }
    
    private void initEmbeddedServer() {
        URL embeddedServerJarFile;
        try {
            embeddedServerJarFile = new File(embeddedServerJar).toURI().toURL();
        } catch(IllegalArgumentException | MalformedURLException e) {
            throw new BuildException("Unable to locate ws-server.jar", e);
        }
        
        // Get the classloader instance for the runtime if we've already created it
        embeddedServerClassLoader = EmbeddedServerInfo.EmbeddedServerClassLoaders.get(embeddedServerJarFile);
        if(embeddedServerClassLoader == null) {
            // Otherwise create a new classloader and add it to our map of classloaders
            embeddedServerClassLoader = new URLClassLoader(new URL[] {embeddedServerJarFile}, this.getClass().getClassLoader());
            EmbeddedServerInfo.EmbeddedServerClassLoaders.put(embeddedServerJarFile, embeddedServerClassLoader);
        }

        try {
            String embeddedServerClassName = "com.ibm.wsspi.kernel.embeddable.Server";
            
            Class<?> embeddedServerClass = Class.forName(embeddedServerClassName, true, embeddedServerClassLoader);
            Class<?> embeddedServerBuilderClass = Class.forName("com.ibm.wsspi.kernel.embeddable.ServerBuilder", true, embeddedServerClassLoader);
            Class<?> embeddedServerFutureResultClass = null;
            
            // Server methods
            embeddedServerStart = embeddedServerClass.getMethod("start", new Class[] {String[].class});
            embeddedServerStop = embeddedServerClass.getMethod("stop", new Class[] {String[].class});
            embeddedServerIsRunning = embeddedServerClass.getMethod("isRunning");
            
            // Future result methods
            Class<?>[] embeddedServerDeclaredClasses = embeddedServerClass.getDeclaredClasses();
            for(Class<?> declaredClass : embeddedServerDeclaredClasses) {
                if((embeddedServerClassName + "$Result").equals(declaredClass.getName())) {
                    embeddedServerFutureResultClass = declaredClass;
                }
            }
            
            if(embeddedServerFutureResultClass == null) {
                throw new BuildException("Unable to load embedded server Result interface");
            }
            
            embeddedServerResultSuccessful = embeddedServerFutureResultClass.getMethod("successful");
            embeddedServerResultReturnCode = embeddedServerFutureResultClass.getMethod("getReturnCode");
            embeddedServerResultException = embeddedServerFutureResultClass.getMethod("getException");
            
            // Server builder methods
            Method serverBuilderSetName = embeddedServerBuilderClass.getMethod("setName", new Class[] {String.class});
            Method serverBuilderSetUserDir = embeddedServerBuilderClass.getMethod("setUserDir", new Class[] {File.class});
            Method serverBuilderSetOutputDir = embeddedServerBuilderClass.getMethod("setOutputDir", new Class[] {File.class});
            Method serverBuilderBuild = embeddedServerBuilderClass.getMethod("build");
            
            // Look for an existing embedded server
            EmbeddedServerInfo info = new EmbeddedServerInfo(getServerName(), getUserDir(), getOutputDir());
            embeddedServer = EmbeddedServerInfo.EmbeddedServers.get(info);
            if(embeddedServer == null) {
                // If there is no existing server, create one...
                Object serverBuilder = embeddedServerBuilderClass.newInstance(); // ServerBuilder sb = new ServerBuilder();
                serverBuilder = serverBuilderSetName.invoke(serverBuilder, getServerName()); // sb.setName(serverName)
                serverBuilder = serverBuilderSetUserDir.invoke(serverBuilder, getUserDir()); // sb.setUserDir(userDir)
                serverBuilder = serverBuilderSetOutputDir.invoke(serverBuilder, getOutputDir()); // sb.setOutputDir(outputDir)
                embeddedServer = serverBuilderBuild.invoke(serverBuilder); // sb.build()
                
                // ...and add it to the map of embedded servers
                EmbeddedServerInfo.EmbeddedServers.put(info, embeddedServer);
            }

        } catch(Exception e) {
            throw new BuildException("Unable to load embedded Server and ServerBuilder classes", e);
        }
    }
        
    private void doEmbeddedStart() throws Exception {
        initEmbeddedServer();
        Future<?> startFuture = (Future<?>) embeddedServerStart.invoke(embeddedServer, (Object)EMPTY_ARRAY);
        getEmbeddedServerResult("start", startFuture);
    }
    
    private void doEmbeddedRun() throws Exception {
        initEmbeddedServer();
        Future<?> startFuture = (Future<?>) embeddedServerStart.invoke(embeddedServer, (Object)EMPTY_ARRAY);
        getEmbeddedServerResult("run", startFuture);
        while(isEmbeddedServerRunning()) {
            Thread.sleep(100);
        }
    }
    
    private void doEmbeddedStop() throws Exception {
        initEmbeddedServer();
        Future<?> stopFuture = (Future<?>) embeddedServerStop.invoke(embeddedServer, (Object)EMPTY_ARRAY);
        getEmbeddedServerResult("stop", stopFuture);
    }
    
    private boolean isEmbeddedServerRunning() throws Exception {
        return (boolean) embeddedServerIsRunning.invoke(embeddedServer);
    }
    
    private void getEmbeddedServerResult(String action, Future<?> future) throws Exception {
        Object result = future.get();
        
        boolean success = (Boolean) embeddedServerResultSuccessful.invoke(result);
        int returnCode = (Integer) embeddedServerResultReturnCode.invoke(result);
        Object exception = embeddedServerResultException.invoke(result);
        
        if(!success) {
            throw new BuildException("Embedded " + action + " failed: rc=" + returnCode + ", ex=" + exception);
        }
    }
        
    private void doStart() throws Exception {
        // create server first if it doesn't exist        
        if (!serverConfigDir.exists()) {
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

    private void validateServerStarted(File outputFile, long startTimeout) throws Exception {

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
    
    private void doRun() throws Exception {
        // create server first if it doesn't exist        
        if (!serverConfigDir.exists()) {
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
        addOsOption(command);
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
    }
        
    private void doCreate() throws Exception {
        List<String> command = getInitialCommand("create");
        if (template != null) {
            command.add("--template=" + template);
        }
        addNoPasswordOption(command);
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
            if (isWindows) {
                command.add("--archive=" + "\"" + archive.toString() + "\"");
            } else {
                command.add("--archive=" + archive.toString().replaceAll(" ", "\\\\ "));
            }
        }
    }
    
    private void addIncludeOption(List<String> command) {
        if (include != null) {
            command.add("--include=" + include);
        }
    }
    
    private void addOsOption(List<String> command) {
        if (os != null) {
            command.add("--os=" + os);
        }
    }
    
    private void addCleanOption(List<String> command) {
        if (clean) {
            command.add("--clean");
        }
    }

    private void addNoPasswordOption(List<String> command) {
        if (noPassword) {
            command.add("--no-password");
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

    /**
     * @return the os
     */
    public String getOs() {
        return os;
    }

    /**
     * @param os the os to set
     */
    public void setOs(String os) {
        this.os = os;
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
    
    /**
     * @return the noPassword setting
     */
    public boolean isNoPassword() {
        return noPassword;
    }

    /**
     * @param noPassword
     *            the noPassword option value to set
     */
    public void setNoPassword(boolean noPassword) {
        this.noPassword = noPassword;
    }

    public void setUseEmbeddedServer(boolean useEmbeddedServer) {
        this.useEmbeddedServer = useEmbeddedServer;
    }

    /* server's exit codes  */
    public enum ReturnCode {
        OK(0),
        // started/stopped is set based on operation.
        // process will return this code if start is called when server is already running
        // or will return this code for stop/status when the server is not running
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

}
