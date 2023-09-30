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
package io.openliberty.tools.ant.install;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Get.DownloadProgress;

import io.openliberty.tools.ant.AbstractTask;

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
    private String type;
    private String runtimeUrl;
    private String username;
    private String password;
    private long maxDownloadTime;
    private boolean offline;
    private boolean useOpenLiberty;
    
    private boolean skipAlreadyInstalledCheck = false;

    @Override
    public void execute() throws BuildException {
        try {
            doExecute();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void doExecute() throws Exception {
        if (baseDir == null) {
            baseDir = ".";
        }

        if(!skipAlreadyInstalledCheck) {
            File wlpDir = new File(baseDir, "wlp");
            if (wlpDir.exists()) {
                log("Liberty profile is already installed.");
                return;
            }
        }

        if (cacheDir == null) {
            File dir = new File(System.getProperty("java.io.tmpdir"), "wlp-cache");
            cacheDir = dir.getAbsolutePath();
        }

        if (runtimeUrl == null) {
            if(useOpenLiberty) { // Download from the Open Liberty repo
                OpenLibertyInstaller installer = new OpenLibertyInstaller();
                installer.setVersion(version);
                installer.setType(type);
                installer.install(this);
            }
            else { // Download from the Wasdev repo
                WasDevInstaller installer = new WasDevInstaller();
                installer.setVersion(version);
                installer.setLicenseCode(licenseCode);
                installer.setType(type);
                installer.install(this);
            }
        } else {
            ArchiveInstaller installer = new ArchiveInstaller();
            installer.setRuntimeUrl(runtimeUrl);
            installer.setLicenseCode(licenseCode);
            installer.install(this);
        }
    }

    protected void downloadFile(URL source, File dest) throws IOException {
        if (offline) {
            offlineDownload(source, dest);
        } else {
            onlineDownload(source, dest);
        }
    }

    private void offlineDownload(URL source, File dest) throws IOException {
        if (dest.exists()) {
            log("Offline mode. Using " + dest + " for " + source);
        } else {
            throw new BuildException("Offline mode. File " + dest.getName() + " is not available in the cache.");
        }
    }

    private void onlineDownload(URL source, File dest) throws IOException {
        Get get = (Get) getProject().createTask("get");
        DownloadProgress progress = null;
        if (verbose) {
            progress = new Get.VerboseProgress(System.out);
        }
        get.setUseTimestamp(true);
        get.setUsername(username);
        get.setPassword(password);
        get.setMaxTime(maxDownloadTime);
        get.setRetries(5);
        get.doGet(source, dest, Project.MSG_INFO, progress);
    }

    protected void installLiberty(File jarFile) throws Exception {
        Java java = (Java) getProject().createTask("java");
        java.setJar(jarFile);
        java.setFork(true);
        java.createArg().setValue("-acceptLicense");
        java.createArg().setValue(baseDir);

        int exitCode = java.executeJava();
        if (exitCode != 0) {
            throw new BuildException("Error installing Liberty.");
        }
    }

    protected void unzipLiberty(File zipFile) throws Exception {
        Unzip.unzipToDirectory(zipFile, new File(baseDir));
    }

    protected void checkLicense(String actualLicenseCode) {
        if (actualLicenseCode == null) {
            throw new BuildException("License code not found.");
        }
        if (!licenseCode.equals(actualLicenseCode)) {
            throw new BuildException("License code does not match. Expected: " + licenseCode + ", Actual: " + actualLicenseCode);
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public boolean isOffline() {
        return offline;
    }
    
    public void setUseOpenLiberty(boolean useOpenLiberty) {
        this.useOpenLiberty = useOpenLiberty;
    }
    
    public boolean getUseOpenLiberty() {
        return useOpenLiberty;
    }

    public void setSkipAlreadyInstalledCheck(boolean skipAlreadyInstalledCheck) {
        this.skipAlreadyInstalledCheck = skipAlreadyInstalledCheck;
    }

    public boolean getSkipAlreadyInstalledCheck() {
        return skipAlreadyInstalledCheck;
    }
}
