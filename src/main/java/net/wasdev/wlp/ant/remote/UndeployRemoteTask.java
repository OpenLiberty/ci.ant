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


public class UndeployRemoteTask extends AbstractRemoteTask{
    private String fileName;
    private String timeout;

    
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
            System.out.println("Deleting: "+fileName);//Add message to messages instead of hardcode
            rso.deleteApp(fileName);
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            if(rso != null) {
                rso.disconnect();
            }
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

}
