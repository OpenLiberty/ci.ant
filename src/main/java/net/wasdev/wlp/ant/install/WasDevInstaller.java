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
import java.net.URL;
import java.util.List;

import org.apache.tools.ant.BuildException;

public class WasDevInstaller implements Installer {

    private static final String LICENSE_REGEX = "D/N:\\s*(.*?)\\s*\\<";

    private String url;
    private String licenseCode;
    private String version;
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void install(InstallLibertyTask task) throws Exception {
        if (url == null) {
            url = "https://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/index.yml";
        }

        if (version == null) {
            version = "16.+";
        }

        if (type == null) {
            type = (licenseCode == null) ? "webProfile7" : "webProfile6";
        }

        File cacheDir = new File(task.getCacheDir());
        InstallUtils.createDirectory(cacheDir);

        // download yml file
        URL ymlURL = new URL(url);
        File ymlFile = new File(cacheDir, InstallUtils.getFile(ymlURL));
        task.downloadFile(ymlURL, ymlFile);

        // parse yml
        List<LibertyInfo> versions = LibertyYaml.parse(ymlFile);

        // select version from yml
        Version baseVersion = Version.parseVersion(version, true);
        LibertyInfo selectedVersion = InstallUtils.selectVersion(baseVersion, versions);

        File versionCacheDir = new File(task.getCacheDir(), selectedVersion.getVersion().toString());
        InstallUtils.createDirectory(versionCacheDir);

        String uri = getRuntimeURI(selectedVersion);
        if (uri.endsWith(".jar")) {
            // ensure licenseCode is set
            task.checkLicenseSet();

            // download license
            URL licenseURL = new URL(selectedVersion.getLicenseUri());
            File licenseFile = new File(versionCacheDir, InstallUtils.getFile(licenseURL));
            task.downloadFile(licenseURL, licenseFile);

            // do license check
            task.checkLicense(InstallUtils.getLicenseCode(licenseFile, LICENSE_REGEX));

            // download Liberty jar
            URL libertyURL = new URL(uri);
            File libertyFile = new File(versionCacheDir, InstallUtils.getFile(libertyURL));
            task.downloadFile(libertyURL, libertyFile);

            // install Liberty jar
            task.installLiberty(libertyFile);
        } else {
            // download zip file
            URL libertyURL = new URL(uri);
            File libertyFile = new File(versionCacheDir, InstallUtils.getFile(libertyURL));
            task.downloadFile(libertyURL, libertyFile);

            // unzip
            task.unzipLiberty(libertyFile);
        }
    }

    private String getRuntimeURI(LibertyInfo selected) {
        String propertyName = ("webProfile6".equalsIgnoreCase(type)) ? "uri" : type;
        String value = selected.getProperty(propertyName);
        if (value == null) {
            throw new BuildException("Archive type " + propertyName + " is not available for Liberty version " + selected.getVersion());
        }
        return value;
    }
}
