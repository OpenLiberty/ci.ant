package net.wasdev.wlp.ant;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

/**
 * server operations task: start/stop/package/create/status/debug
 */
public class ServerTask extends AbstractTask {

    private String operation;
    private boolean clean = true;
    private File archive;

    private String timeout;

    private String wlp;

    private static final long SERVER_START_TIMEOUT_DEFAULT = 30 * 1000;
    private static final long SERVER_STOP_TIMEOUT_DEFAULT = 30 * 1000;

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

    @Override
    public void execute() {
        if (operation == null || operation.length() <= 0) {
            throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"), "operation"));
        }

        initTask();

        Process p = null;
        try {

            List<String> commands = new ArrayList<String>();
            commands.add(wlp);
            commands.add(operation);
            if (serverName != null && !serverName.equals("")) {
                //start a server doesn't exist, create it first
                if (!serverConfigRoot.exists() && operation.equals("start")) {
                    log(MessageFormat.format(messages.getString("info.server.create"), serverName));
                    commands.clear();
                    commands.add(wlp);
                    commands.add("create");
                    commands.add(serverName);
                    processBuilder.command(commands);
                    p = processBuilder.start();
                    checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
                    commands.clear();
                    commands.add(wlp);
                    commands.add(operation);
                }
                commands.add(serverName);
            }

            if (operation.equals("package") && archive != null) {
                if (archive.isDirectory()) {
                    throw new BuildException(messages.getString("error.server.packag"));
                }
                commands.add("--archive=" + getArchive());
            }

            if (isClean()) {
                commands.add("--clean");
            }

            processBuilder.command(commands);

            p = processBuilder.start();

            if (operation.equals("start")) {
                checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());

                // check server started message code.
                long startTimeout = SERVER_START_TIMEOUT_DEFAULT;
                if (timeout != null && !timeout.equals("")) {
                    startTimeout = Long.valueOf(timeout);
                }
                validateServerStarted(getLogFile(), startTimeout);

            } else if (operation.equals("stop")) {
                checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());

            } else if (operation.equals("package")) {
                checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());

            } else if (operation.equals("create")) {
                checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());

            } else if (operation.equals("status")) {
                checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());

            } else if (operation.equals("debug")) {
                //Debug seems useless in ant tasks, but still keep it.
                checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());

            }

        } catch (Exception e) {
            throw new BuildException(e);
        }

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

}
