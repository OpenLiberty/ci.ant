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
package net.wasdev.wlp.ant.remote;

import org.apache.tools.ant.BuildException;

/**
 * Add task description
 */
public class RemoteApplicationManagerTask extends AbstractRemoteTask {

    private String appName;
    private String operation;
    

    @Override
    public void execute() {

        super.initTask();

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newLoader = null;
        RemoteServerManager rso = null;

        try {
            
            newLoader = getNewClassLoader(oldLoader);
            rso = new RemoteServerManager(newLoader);
            rso.connect(hostName, httpsPort, userName, password, trustStoreLocation, trustStorePassword, disableHostnameVerification);

            if ("start".equals(operation)) {
                rso.startApp(appName);
            } else if ("stop".equals(operation)) {
                rso.stopApp(appName);
            } else if ("state".equals(operation)) {
                rso.appState(appName);
            } else {
                throw new BuildException("Unsupported operation: " + operation);
            }

        } catch (Exception e) {
            throw new BuildException(e);
        }
        finally{
            if (rso != null) {
                rso.disconnect();
            }
        }
    }


 

    public void setApplicationName(String appName) {
        this.appName = appName;
    }

    public String getApplicationName() {
        return appName;
    }
   

    public void setOperation(String operation) {
        this.operation = operation;
    }


}
