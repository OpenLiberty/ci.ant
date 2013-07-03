package net.wasdev.wlp.ant;

import java.io.File;
import java.text.MessageFormat;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;

/**
 * undeploy ant task
 */
public class UndeployTask extends AbstractTask {
    private String fileName;

    private String timeout;

    private static final long APP_STOP_TIMEOUT_DEFAULT = 30 * 1000;

    @Override
    public void execute() {

        super.initTask();

        if (fileName == null) {
            stopServer(getTimeout());
            throw new BuildException(messages.getString("erro.undeploy.filename.set"));
        }

        try {

            File fileUndeploy = new File(serverConfigRoot, "dropins/" + fileName);
            if (!fileUndeploy.exists()) {
                throw new BuildException(MessageFormat.format(messages.getString("error.undeploy.file.noexist"), fileUndeploy.getCanonicalPath()));
            }

            log(MessageFormat.format(messages.getString("info.undeploy"), fileUndeploy.getCanonicalPath()));

            FileUtils.delete(fileUndeploy);

            //check stop message code
            String stopMessage = STOP_APP_MESSAGE_CODE_REG + fileName.substring(0, fileName.length() - 4);
            long appStopTimeout = APP_STOP_TIMEOUT_DEFAULT;
            if (timeout != null && !timeout.equals("")) {
                appStopTimeout = Long.valueOf(timeout);
            }
            if (waitForStringInLog(stopMessage, appStopTimeout, getLogFile()) == null) {
                stopServer(getTimeout());
                throw new BuildException(MessageFormat.format(messages.getString("error.undeploy.fail"), fileName));
            }

        } catch (Exception e) {
            stopServer(getTimeout());
            throw new BuildException(e);
        }
    }

    public void setFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFile() {
        return this.fileName;
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
     * @return the ref
     */
    @Override
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    @Override
    public void setRef(String ref) {
        this.ref = ref;
    }

}
