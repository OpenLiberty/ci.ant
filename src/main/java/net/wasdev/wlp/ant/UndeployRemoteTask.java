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

package net.wasdev.wlp.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

public class UndeployRemoteTask extends AbstractRemoteTask{
    private String fileName;
//    private PatternSet pattern;
    private String timeout;
    private static final long APP_STOP_TIMEOUT_DEFAULT = 30 * 1000;

    private Path classpath;
    
    @Override
    public void execute() {
        super.initTask();

        long appStopTimeout = APP_STOP_TIMEOUT_DEFAULT;

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newLoader = null;
        RemoteServerManager rso = null;

        System.out.println("my file is:" + fileName);

        if (timeout != null && !timeout.equals("")) {
            appStopTimeout = Long.valueOf(timeout);
        }

        try {
            newLoader = getNewClassLoader(oldLoader);
            rso = new RemoteServerManager(newLoader);
            rso.connect(hostName, httpsPort, userName, password, trustStoreLocation, trustStorePassword, disableHostnameVerification);
            rso.deleteApp(fileName);
        } catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            if(rso == null) {
                System.out.println("This connection did not work");
                rso.disconnect();
            }
        }
        
        
    }

    private ClassLoader getNewClassLoader(ClassLoader parent) {
        ArrayList<URL> urls = new ArrayList<URL>();

        if (installDir != null){
            File f = new File(installDir + "/clients/restConnector.jar");
            try {
                urls.add(f.toURI().toURL());
                System.out.println(urls);
            } catch (MalformedURLException e) {
                throw new BuildException(e);
            }
            return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
        }
        if (classpath == null)
            return null;

        String[] pathElements = classpath.list();
        for (String pathElement:pathElements){
            try {
                File f = new File(pathElement);
                urls.add(f.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new BuildException(e);
            }
        }
        if (urls.size() == 0)
            return null;
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
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
