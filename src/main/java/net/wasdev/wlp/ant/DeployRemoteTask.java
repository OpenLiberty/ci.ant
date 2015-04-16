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
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Deploy remote ant task. This task copies a file or files to ${server.config.dir}/dropins of a remote Liberty server.
 *
 * The following configuration needs to be done in the target Liberty profile server:
 *  <ol>
 *      <li> Add the feature restConnector-1.0 </li>
 *      <li> Add a key store, for example: <keyStore id="defaultKeyStore" password="Liberty"/> </li>
 *      <li> Configure a user which will run the remote deploy task, for example: <quickStartSecurity userName="theUser" userPassword="thePassword"/> </li>
 *      <li> Give write remote file access to the dropins folder, for example: <remoteFileAccess><writeDir>${server.config.dir}/dropins</writeDir></remoteFileAccess></li>
 *  </ol>
 */
public class DeployRemoteTask extends AbstractRemoteTask {

    private final List<FileSet> apps = new ArrayList<FileSet>();
    private String filePath;

    private String timeout;
    private Path classpath;

    @Override
    public void execute() {

        super.initTask();

        // TODO: Validate the server is not null.
        // Check for no arguments
        if ((filePath == null) && (apps.size() == 0)) {
            throw new BuildException(messages.getString("error.fileset.set"), getLocation());
        }

        final List<File> files = scanFileSets();

        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newLoader = null;
        RemoteServerManager rso = null;

        try {
            newLoader = getNewClassLoader(oldLoader);
            rso = new RemoteServerManager(newLoader);
            rso.connect(hostName, httpsPort, userName, password, trustStoreLocation, trustStorePassword, disableHostnameVerification);

            for (File f : files) {
                rso.publishApp(f);
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



    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param aFS
     *            the file set to add
     */
    public void addFileset(FileSet fs) {
        apps.add(fs);
    }

    public void setFile(File app) {
        filePath = app.getAbsolutePath();
    }

    /**
     * returns the list of files (full path name) to process.
     *
     * @return the list of files included via the filesets.
     */
    private List<File> scanFileSets() {
        final List<File> list = new ArrayList<File>();
        if (filePath != null && (new File(filePath)).exists()) {
            list.add(new File(filePath));
        }
        for (int i = 0; i < apps.size(); i++) {
            final FileSet fs = apps.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            for (String element : names) {
                list.add(new File(ds.getBasedir(), element));
            }
        }

        return list;
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

    public void setClasspath(Path classpath) {
        if(classpath == null) {
            this.classpath = classpath;
         } else {
         this.classpath.append(classpath);
       }
    }


    public void setClasspathRef(Reference r) {
        Path path = createClasspath();
        path.setRefid(r);
        path.toString();
    }


    public Path createClasspath() {
        if(classpath == null) {
            classpath = new Path(getProject());
        }
        Path result = classpath.createPath();
        return result;
    }

}
