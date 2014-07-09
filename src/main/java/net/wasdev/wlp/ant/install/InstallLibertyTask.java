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
package net.wasdev.wlp.ant.install;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.wasdev.wlp.ant.AbstractTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Get.DownloadProgress;
import org.apache.tools.ant.taskdefs.Java;

/*
 * Install Liberty profile server task.
 */
public class InstallLibertyTask extends AbstractTask {
   
    private String baseDir;
    private String cacheDir;
    private boolean verbose;
    private String licenseCode;
    private String version;
    private String runtimeUrl;
    private String username;
    private String password;
    private long maxDownloadTime;
    
    @Override
    public void execute() throws BuildException {
        try {
            doExecute();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void doExecute() throws Exception {
        checkLicenseSet();

        if (baseDir == null) {
            baseDir = ".";
        }
        
        File wlpDir = new File(baseDir, "wlp");
        if (wlpDir.exists()) {
            log("Liberty profile is already installed.");
            return;
        }

        if (cacheDir == null) {
            File dir = new File(System.getProperty("java.io.tmpdir"), "wlp-cache");
            cacheDir = dir.getAbsolutePath();            
        }
                
        if (runtimeUrl == null) {
            WasDevInstaller installer = new WasDevInstaller();
            installer.setVersion(version);
            installer.setLicenseCode(licenseCode);
            installer.install(this);
        } else {
            ArchiveInstaller installer = new ArchiveInstaller();
            installer.setRuntimeUrl(runtimeUrl);
            installer.setLicenseCode(licenseCode);
            installer.install(this);
        }
    }

    protected void downloadFile(URL source, File dest) throws IOException {
        Get get = (Get) getProject().createTask("get");
        DownloadProgress progress = null;
        if (verbose) {
            progress = new Get.VerboseProgress(System.out);
        }
        get.setUseTimestamp(true);
        get.setUsername(username);
        get.setPassword(password);
        get.setMaxTime(maxDownloadTime);
        get.doGet(source, dest, Project.MSG_INFO, progress);
    }

    protected int installLiberty(File jarFile) {
        Java java = (Java) getProject().createTask("java");
        java.setJar(jarFile);
        java.setFork(true);
        java.createArg().setValue("-acceptLicense");
        java.createArg().setValue(baseDir);

        int exitCode = java.executeJava();
        return exitCode;
    }

    protected void checkLicense(String actualLicenseCode) {
        if (actualLicenseCode == null) {
            throw new BuildException("License code not found.");
        }
        if (!licenseCode.equals(actualLicenseCode)) {
            throw new BuildException("License code does not match.");
        }
    }
    
    protected void checkLicenseSet() {
        if (licenseCode == null) {
            throw new BuildException("Liberty license code must be specified.");
        }
    }
    
    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }
    
    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public String getRuntimeUrl() {
        return runtimeUrl;
    }

    public void setRuntimeUrl(String runtimeUrl) {
        this.runtimeUrl = runtimeUrl;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public boolean isVerbose() {
        return verbose;
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getMaxDownloadTime() {
        return maxDownloadTime;
    }

    public void setMaxDownloadTime(long maxDownloadTime) {
        this.maxDownloadTime = maxDownloadTime;
    }
}
