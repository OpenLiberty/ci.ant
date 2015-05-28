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
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.tools.ant.BuildException;


/**
 * Performs operations on remote Liberty profile server using REST connections
 */

public class RemoteServerManager  {

    private MBeanServerConnection mbsc;
    private JMXConnector connector;
    private final ClassLoader classLoader;


    public RemoteServerManager(){
        this(null);
    }

    public RemoteServerManager(ClassLoader classLoader){
        this.classLoader = classLoader;
    }

    public void connect(String hostName, int httpsPort, String userName, String password, File trustStoreLocation, String trustStorePassword, boolean disableHostnameVerification) throws IOException{

        System.setProperty("javax.net.ssl.trustStore", trustStoreLocation.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

        try {
            Map<String, Object> environment = new HashMap<String, Object>();
            environment.put("jmx.remote.protocol.provider.pkgs", "com.ibm.ws.jmx.connector.client");
            environment.put(JMXConnector.CREDENTIALS, new String[] { userName, password });
            if (classLoader != null){
                environment.put("jmx.remote.protocol.provider.class.loader", classLoader);
            }
            environment.put("com.ibm.ws.jmx.connector.client.disableURLHostnameVerification", disableHostnameVerification);
            
            JMXServiceURL url;
            url = new JMXServiceURL("REST", hostName, httpsPort, "/IBMJMXConnectorREST");

            connector = JMXConnectorFactory.newJMXConnector(url, environment);
            connector.connect();
            mbsc = connector.getMBeanServerConnection();
            System.out.println("You are now connected to: "+hostName);//Add message to messages instead of hardcoding
        } catch(SocketException e) {
            throw new BuildException("There was a problem trying to make the connection please check the hostName, trustStoreLocation and trustStorePassword attributes");//Add message to messages instead of hardcoding
        } catch(IOException e) {
            throw new BuildException(e.getMessage());
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void publishApp(File file) {
        // Use the FileTransfer MBean to publish the file to the
        // folder ${server.output.dir}/dropins/ of the remote server.

        ObjectName fileTransferBeanName;
        try {
            fileTransferBeanName = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileTransfer,name=FileTransfer");

            if (mbsc.isRegistered(fileTransferBeanName)) {
                mbsc.invoke(fileTransferBeanName, "uploadFile",
                        new Object[]{file.getAbsolutePath(), "${server.config.dir}/dropins/"+file.getName(), false},
                        new String[]{String.class.getName(), String.class.getName(),Boolean.class.getName()});
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void deleteApp(String file) {

        // Use the FileTransfer MBean to publish the file to the
        // folder ${server.output.dir}/dropins/ of the remote server.

        ObjectName fileTransferBeanName;
        try {
            fileTransferBeanName = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileTransfer,name=FileTransfer");

            if (mbsc.isRegistered(fileTransferBeanName)) {
                mbsc.invoke(fileTransferBeanName, "deleteFile",
                        new Object[]{"${server.output.dir}/dropins/"+file},
                        new String[]{String.class.getName()});
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void startApp(String appName) {
        // Use the Application MBean to start an application of the remote server.
        ObjectName applicationBeanName;
        try {
            applicationBeanName = new ObjectName(
                    "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name="+appName);
            if (mbsc.isRegistered(applicationBeanName)) {
                mbsc.invoke(applicationBeanName, "start",
                        null,
                        null);
                System.out.println("The app " + appName + " has been started");//Add message to messages instead of hardcode
            } else {
                throw new BuildException("The application " + appName + " hasn't been started, it is posibble that doesn't exist");//Add message to messages instead of hardcode
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new BuildException(e);
        } 
    }

    public void stopApp(String appName) {
        // Use the Application MBean to stop an application of the remote server.
        ObjectName applicationBeanName;
        try {
            applicationBeanName = new ObjectName(
                    "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name="+appName);
            if (mbsc.isRegistered(applicationBeanName)) {
                mbsc.invoke(applicationBeanName, "stop",
                        null,
                        null);
                System.out.println("The app " + appName + " has been stoped");//Add message to messages instead of hardcode
            } else {
                throw new BuildException("The application " + appName + "hasn't been stopped, it is posibble that do not exists");//Add message to messages instead of hardcode
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new BuildException(e);
        } 
    }
    
    public void appState(String appName) {
        // Use the Application MBean check the status of an application on the remote server.
        ObjectName applicationBeanName;
        try {
            applicationBeanName = new ObjectName(
                    "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name="+appName);
            if (mbsc.isRegistered(applicationBeanName)) {
                String state = (String) mbsc.getAttribute(applicationBeanName, "State"); 
                System.out.println("The state of the application " + appName + " is: " + state);//Add message to messages instead of hardcode
            } else {
                throw new BuildException("The state of the application " + appName + " can't be resolved");//Add message to messages instead of hardcode
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new BuildException(e);
        } 
    }

    public void writeList() {
        ObjectName fileServiceBean;
        try {
            fileServiceBean = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileService,name=FileService");
                if (mbsc.isRegistered(fileServiceBean)) {
                    String[] writeList = (String[])mbsc.getAttribute(fileServiceBean, "WriteList");

                    for (String writeElement : writeList) {
                        System.out.println("Write Element: "+writeElement);//Add message to messages instead of hardcode
                    }
                }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new BuildException(e);
        }
    }

    public void readList() {
        ObjectName fileServiceBean;
        try {
            fileServiceBean = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileService,name=FileService");
                if (mbsc.isRegistered(fileServiceBean)) {
                    String[] readList = (String[])mbsc.getAttribute(fileServiceBean, "ReadList");

                    for (String readElement : readList) {
                        System.out.println("Read Element: "+readElement);//Add message to messages instead of hardcode
                    }
                }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new BuildException(e);
        }
    }

    public void appList() {
        ObjectName fileServiceBean;
        try {
            fileServiceBean = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileService,name=FileService");
                if (mbsc.isRegistered(fileServiceBean)) {
                    CompositeData[] dropinsApps = (CompositeData[]) mbsc.invoke(fileServiceBean, "getDirectoryEntries",
                                                            new Object[]{"${server.config.dir}/dropins/", Boolean.FALSE, "REQUEST_OPTIONS_ALL"},
                                                            new String[]{String.class.getName(), boolean.class.getName(), String.class.getName()});


                    for(CompositeData dropinsApp : dropinsApps ) {
                        String name[] = dropinsApp.get("fileName").toString().split("/");
                        System.out.println("Dropins Application: "+name[name.length-1]);//Add message to messages instead of hardcode
                    }
                }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new BuildException(e);
        }
    }
    public void zipLogs() {
        ObjectName FileServiceBean;
        try {
            FileServiceBean = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileService,name=FileService");
                if (mbsc.isRegistered(FileServiceBean)) {
                 boolean result=(Boolean) mbsc.invoke(FileServiceBean, "createArchive",
                            new Object[]{"${server.config.dir}/logs","${server.config.dir}/logs.zip"},
                            new String[]{String.class.getName(), String.class.getName()});
                    if (result) {
                        System.out.println("The logs where zipped");//Add message to messages instead of hardcode
                    } else {
                        System.out.println("There were some problems creating the \"logs.zip\" file, please verify that ${server.config.dir} have write access");//Add message to messages instead of hardcode
                    }
                }

        } catch(SocketTimeoutException e) {
            throw new BuildException("Read timed out");
        } catch(Exception e) {
            throw new BuildException(e);
        }
    }

    public void downloadLogs(File localTargetFile) {
        ObjectName FileTransferBean;
        try {
            FileTransferBean = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileTransfer,name=FileTransfer");
                if (mbsc.isRegistered(FileTransferBean)) {
                    mbsc.invoke(FileTransferBean, "downloadFile",
                            new Object[]{"${server.config.dir}/logs.zip", localTargetFile.getCanonicalPath()},
                            new String[]{String.class.getName(), String.class.getName()});

                        System.out.println("The logs were downloaded to " + localTargetFile.getCanonicalPath());//Add message to messages instead of hardcode
                                           System.out.println("");

                }

        } catch(Exception e) {
            throw new BuildException(e);
        }
    }

    public void disconnect() {
        if (connector != null){
            try {
                connector.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally{
                connector = null;
            }
        }
    }
}
