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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

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
    
    @Override
    public void execute() {

        super.initTask();

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
                System.out.println("Uploading: "+f.getName());//Add message to messages instead of hardcode
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

        if (filePath != null) {
            filePath = filePath.trim();
            if (filePath.length() == 0) {
                throw new BuildException(MessageFormat.format(messages.getString("error.parameter.invalid"), "file"), getLocation());
            }
            File fileToDeploy = new File(filePath);
            if (!fileToDeploy.exists()) {
                throw new BuildException(MessageFormat.format(messages.getString("error.deploy.file.noexist"), filePath), getLocation());
            } else if (fileToDeploy.isDirectory()) {
                throw new BuildException(messages.getString("error.deploy.file.isdirectory"), getLocation());
            } else {
                list.add(fileToDeploy);
            }
        }

        for (int i = 0; i < apps.size(); i++) {
            final FileSet fs = apps.get(i);
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();

            final String[] names = ds.getIncludedFiles();

            //Throw a BuildException if the directory specified as parameter is empty.
            if (names.length == 0) {
                throw new BuildException(messages.getString("error.deploy.fileset.invalid"), getLocation());
            }

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

}
