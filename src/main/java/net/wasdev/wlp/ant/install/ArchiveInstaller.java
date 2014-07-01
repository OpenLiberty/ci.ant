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
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.tools.ant.BuildException;

public class ArchiveInstaller implements Installer {
    
    private static final String LICENSE_REGEX = "D/N:\\s*(.*)\\s*";
    
    private String runtimeUrl;
    private String extendedUrl;
    private String licenseCode;
    
    public String getRuntimeUrl() {
        return runtimeUrl;
    }

    public void setRuntimeUrl(String runtimeUrl) {
        this.runtimeUrl = runtimeUrl;
    }

    public String getExtendedUrl() {
        return extendedUrl;
    }

    public void setExtendedUrl(String extendedUrl) {
        this.extendedUrl = extendedUrl;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }
    
    public void install(InstallLibertyTask task) throws Exception {
        task.checkLicenseSet();
        
        if (runtimeUrl == null) {
            throw new BuildException("Rumtime URL must be specified.");
        }
        
        File cacheDir = new File(task.getCacheDir());
        InstallUtils.createDirectory(cacheDir);
        
        // download runtime file
        URL runtimeURL = new URL(runtimeUrl);
        File runtimeFile = new File(cacheDir, InstallUtils.getFile(runtimeURL));
        task.downloadFile(runtimeURL, runtimeFile);
        
        // do license check
        task.checkLicense(getLicenseCode(runtimeFile));
        
        // install runtime
        int exitCode = task.installLiberty(runtimeFile);
        if (exitCode != 0) {
            throw new BuildException("Error installing Liberty profile.");
        } 
                    
        // download extended file
        if (extendedUrl != null) {
            URL extendedURL = new URL(extendedUrl);
            File extendedFile = new File(cacheDir, InstallUtils.getFile(extendedURL));
            task.downloadFile(extendedURL, extendedFile);
            
            // do license check
            task.checkLicense(getLicenseCode(extendedFile));
            
            // install extended
            exitCode = task.installLiberty(extendedFile);
            if (exitCode != 0) {
                throw new BuildException("Error installing extended features for Liberty profile.");
            } 
        }
    }
    
    private String getLicenseCode(File jarFile) throws Exception {
        JarFile jar = new JarFile(jarFile);
        InputStream in = null;
        try {
            ZipEntry entry = jar.getEntry("wlp/lafiles/LI_en");
            if (entry == null) {
                throw new BuildException("Unable to find license file in " + jarFile);
            } 
            in = jar.getInputStream(entry);
            return InstallUtils.getLicenseCode(in, "UTF-16", LICENSE_REGEX);
        } finally {
            InstallUtils.close(in);
            jar.close();
        }
    }
}
