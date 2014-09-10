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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

//import com.ibm.websphere.filetransfer.FileTransferMBean;
//import com.ibm.websphere.jmx.connector.rest.ConnectorSettings;

/**
 * Performs operations on remote Liberty profile server using REST connections
 */

public class RemoteServerManager  {

    private MBeanServerConnection mbsc;
    private JMXConnector jmxConnector;
    private final ClassLoader classLoader;


    public RemoteServerManager(){
        this(null);
    }

    public RemoteServerManager(ClassLoader classLoader){
        this.classLoader = classLoader;
    }


    public void connect(String hostName, int httpsPort, String userName, String password, String trustStoreLocation, String trustStorePassword) throws IOException{

        System.setProperty("javax.net.ssl.trustStore", trustStoreLocation);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put("jmx.remote.protocol.provider.pkgs", "com.ibm.ws.jmx.connector.client");
        environment.put(JMXConnector.CREDENTIALS, new String[] { userName, password });
        if (classLoader != null){
            environment.put("jmx.remote.protocol.provider.class.loader", classLoader);
        }
        //environment.put(ConnectorSettings.DISABLE_HOSTNAME_VERIFICATION, true); //optional setting (replace with "com.ibm.ws.jmx.connector.client.disableURLHostnameVerification")
        //environment.put(ConnectorSettings.READ_TIMEOUT, 2 * 60 * 1000); //optional setting (replace with "com.ibm.ws.jmx.connector.client.rest.readTimeout")

        JMXServiceURL url;
        url = new JMXServiceURL("REST", hostName, httpsPort , "/IBMJMXConnectorREST");
        jmxConnector = JMXConnectorFactory.connect(url, environment);
        mbsc = jmxConnector.getMBeanServerConnection();
        jmxConnector = JMXConnectorFactory.newJMXConnector(url, environment);
        jmxConnector.connect();
        mbsc = jmxConnector.getMBeanServerConnection();
    }


    public void publishApp(File file) {
        // Use the FileTransfer MBean to publish the file to the
        // folder ${server.output.dir}/dropins/ of the remote server.
        // TODO: (rsanchez: Ask Jarek if we should use ${server.output.dir} or ${server.config.dir})

        ObjectName fileTransferBeanName;
        try {
            fileTransferBeanName = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileTransfer,name=FileTransfer");

            if (mbsc.isRegistered(fileTransferBeanName)) {
                mbsc.invoke(fileTransferBeanName, "uploadFile",
                        new Object[]{file.getAbsolutePath(), "${server.config.dir}/dropins/"+file.getName(), false},
                        new String[]{String.class.getName(),String.class.getName(),Boolean.class.getName()});
                /*FileTransferMBean fileTransferBean = JMX.newMBeanProxy(mbsc, fileTransferBeanName, FileTransferMBean.class);
                fileTransferBean.uploadFile(file.getAbsolutePath(), "${server.config.dir}/dropins/"+file.getName(), false);*/
                //fileTransferBean.uploadFile(file.getAbsolutePath(), "${server.output.dir}/dropins/"+file.getName(), false);
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

    public void deleteApp(File file) {

        // Use the FileTransfer MBean to publish the file to the
        // folder ${server.output.dir}/dropins/ of the remote server.

        ObjectName fileTransferBeanName;
        try {
            fileTransferBeanName = new ObjectName(
                    "WebSphere:feature=restConnector,type=FileTransfer,name=FileTransfer");

            if (mbsc.isRegistered(fileTransferBeanName)) {
                mbsc.invoke(fileTransferBeanName, "deleteFile",
                        new Object[]{"${server.output.dir}/dropins/"+file.getName()},
                        new String[]{String.class.getName()});
                /*FileTransferMBean fileTransferBean = JMX.newMBeanProxy(mbsc, fileTransferBeanName, FileTransferMBean.class);
                fileTransferBean.deleteFile("${server.output.dir}/dropins/"+file.getName());*/
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



    public void startApp() {
        // TODO Auto-generated method stub

    }

    public void stopApp() {
        // TODO Auto-generated method stub

    }

    public void disconnect() {
        if (jmxConnector != null){
            try {
                jmxConnector.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally{
                jmxConnector = null;
            }
        }

    }


    public static void main(String[] args){
        String hostName = args[0];
        int httpsPort = Integer.parseInt(args[1]);
        String userName = args[2];
        String password = args[3];
        String trustStoreLocation = args[4];
        String trustStorePassword = args[5];
        String fileToDeploy = args[6];

        RemoteServerManager server = new RemoteServerManager() ;
        File file = new File(fileToDeploy);
        try {
            server.connect(hostName, httpsPort, userName, password, trustStoreLocation, trustStorePassword);
            server.publishApp(file);
            //server.deleteApp(file);
            server.disconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
