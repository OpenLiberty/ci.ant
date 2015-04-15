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

import java.text.MessageFormat;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

/**
 * Remote server operations task: (none)
 */
public class RemoteServerTask extends AbstractRemoteTask {

    private String operation;
    private String timeout;
    private boolean disableHostnameVerification = true;

    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);
        
        System.out.println("initOperation"+operation);
    }

    @Override
    public void execute() {

        if (operation == null) {
            throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"), "operation"));
        }

        try {
            if("connect".equals(operation)) {
                RemoteServerManager server = new RemoteServerManager();
                server.connect(hostName, httpsPort, userName, password, trustStoreLocation, trustStorePassword, disableHostnameVerification);
            }
            else {
                System.out.println("This has not operation");
            }
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
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
