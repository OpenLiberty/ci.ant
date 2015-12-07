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

import java.io.File;

import org.apache.tools.ant.BuildException;



/**
 * Remote server operations task: 
 * ziplogs
 * downloadLogs
 */
public class RemoteServerTask extends AbstractRemoteTask {

    String operation;
    File localTargetFile;
    
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

            if ("zipLogs".equals(operation)) {
                rso.zipLogs();
            } else if ("downloadLogs".equals(operation)) {
                rso.downloadLogs(localTargetFile);
            } else if ("writeList".equals(operation)) {
                rso.writeList();
            } else if ("readList".equals(operation)) {
                rso.readList();
            }  else if ("appList".equals(operation)) {
                rso.appList();
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

    public void setLocalTargetFile(String localTargetFile){
       this.localTargetFile = new File(localTargetFile);
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    
}
