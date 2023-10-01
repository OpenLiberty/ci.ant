/**
 * (C) Copyright IBM Corporation 2023.
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
package io.openliberty.tools.ant;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;

/*
 * Install Liberty interim fix(es) task.
 */
public class InstallLibertyInterimFixTask extends AbstractTask {

    private File interimFixDirectory;
    private boolean suppressInfo = true;
    
    @Override
    public void execute() throws BuildException {
        super.initTask();

        try {
            doExecute();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void doExecute() throws Exception {

        if (interimFixDirectory == null) {
            throw new BuildException("The interimFixDirectory was not specified.");
        }

        if (!interimFixDirectory.exists() || !interimFixDirectory.isDirectory()) {
            throw new BuildException("The interimFixDirectory does not exist or is not a directory.");
        }

        // Find all .jar files in the ifixDir
        File[] jarFiles = findFilesEndsWithInDirectory(interimFixDirectory, ".jar");
        if (jarFiles == null || jarFiles.length == 0) {
            throw new BuildException("The interimFixDirectory does not contain any interim fix JAR files.");
        }
       
        List<String> failedInstallIfixes = new ArrayList<String>();

        for (File nextJarFile : jarFiles) {
            if (!installInterimFix(nextJarFile)) {
                failedInstallIfixes.add(nextJarFile.getName());
            }
        }

        if (!failedInstallIfixes.isEmpty()) {
            throw new BuildException("The following Liberty interim fixes failed to install: "+failedInstallIfixes.toString());
        }
    }

    protected boolean installInterimFix(File jarFile) throws Exception {
        Java java = (Java) getProject().createTask("java");
        java.setJar(jarFile);
        java.setFork(true);
        java.createArg().setValue("-installLocation");
        java.createArg().setValue(installDir.getCanonicalPath());
        if (suppressInfo) {
            java.createArg().setValue("-suppressInfo");
        }

        int exitCode = java.executeJava();
        return (exitCode == 0);
    }

    /**
     * Search the dir path for all files that end with a name or extension. If none are found,
     * an empty List is returned.
     * 
     * @param dir File dir to search under
     * @param ext String to match
     * @return File[] collection of File that match the given ext in the specified dir.
     */
    protected File[] findFilesEndsWithInDirectory(File dir, String ext) throws IOException {

        final String extension = ext;
        File[] matchingFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        });

        return matchingFiles;
    }

    public File getInterimFixDirectory() {
        return interimFixDirectory;
    }

    public void setInterimFixDirectory(File interimFixDirectory) {
        this.interimFixDirectory = interimFixDirectory;
    }

    public boolean isSuppressInfo() {
        return suppressInfo;
    }

    public void setSuppressInfo(boolean suppressInfo) {
        this.suppressInfo = suppressInfo;
    }

}
