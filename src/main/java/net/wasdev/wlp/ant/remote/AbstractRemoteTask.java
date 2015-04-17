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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;

import net.wasdev.wlp.ant.AbstractTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;


/**
 * Base class for tasks that perform operations on remote liberty profile servers.
 * The operations are performed using the REST connector feature of liberty
 */

public abstract class AbstractRemoteTask extends AbstractTask{

    private Path classpath;
    //Connection parameters
    protected String hostName;
    protected int httpsPort = 9443;
    protected String userName;
    protected String password;
    protected File trustStoreLocation;
    protected String trustStorePassword;
    protected boolean disableHostnameVerification = true;

    @Override
    protected void initTask() {

        if (ref != null) {
            Object serverRef = getProject().getReference(ref);
            if (serverRef != null && (serverRef instanceof RemoteServerTask)) {
                RemoteServerTask remoteServerRef = (RemoteServerTask) serverRef;
                setInstallDir(remoteServerRef.getInstallDir());
                setClasspath(remoteServerRef.getClasspath());
                setHostName(remoteServerRef.getHostName());
                setHttpsPort(remoteServerRef.getHttpsPort());
                setUserName(remoteServerRef.getUserName());
                setPassword(remoteServerRef.getPassword());
                setTrustStoreLocation(remoteServerRef.getTrustStoreLocation());
                setTrustStorePassword(remoteServerRef.getTrustStorePassword());
                setDisableHostnameVerification(remoteServerRef.isDisableHostnameVerification());
            }
        }
        try {

            if (installDir != null) {
                installDir = installDir.getCanonicalFile();
                    
                // Quick sanity check
                File file = new File(installDir, "lib/ws-launch.jar");
                if (!file.exists()) {
                    throw new BuildException(messages.getString("error.installDir.set"));
                }

                log(MessageFormat.format(messages.getString("info.variable"), "installDir", installDir));

            } else if (classpath != null) {
                log(MessageFormat.format(messages.getString("info.variable"), "classpath", classpath));
            } else {
                    throw new BuildException(messages.getString("error.installDir.validate"));
                }
            

        } catch (IOException e) {
            throw new BuildException(e);
        }

    }


    protected ClassLoader getNewClassLoader(ClassLoader parent) {
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
    
    /**
     * Returns the host name to which the application will be deployed. 
     * @return the host name.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the host name to which the application will be deployed. 
     * @param hostName the host name
     */

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the https port in which the liberty server is listening
     * @return the https port
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Sets the https port in which the liberty server is listening
     * @param httpsPortthe http port
     */
    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }


    /**
     * Returns the user name configured in the liberty server to which the files will be deployed
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name configured in the liberty server to which the files will be deployed
     * @param userName the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the password of the user configured in the liberty server to which the files will be deployed
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user configured in the liberty server to which the files will be deployed
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the location of the trust store with the certificate used to connect to the liberty server
     * @return the trust store location (absolute path)
     */
    public File getTrustStoreLocation() {
        return trustStoreLocation;
    }

    /**
     *  Sets the location of the trust store with the certificate used to connect to the liberty server
     * @param trustStoreLocation the trust store location (absolute path)
     */
    public void setTrustStoreLocation(File trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    /**
     * Returns the password of the trust store with the certificate used to connect to the liberty server
     * @return the password
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Sets the password of the trust store with the certificate used to connect to the liberty server
     * @param trustStorePassword the password
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }


    public boolean isDisableHostnameVerification() {
        return disableHostnameVerification;
    }

    public void setDisableHostnameVerification(boolean disableHostnameVerification) {
        this.disableHostnameVerification = disableHostnameVerification;
    }

    public void setClasspath(Path classpath) {
         this.classpath = classpath;
    }
    
    public Path getClasspath() {
        return classpath;
    }
    
}
