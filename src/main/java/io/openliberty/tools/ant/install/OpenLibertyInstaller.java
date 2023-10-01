package io.openliberty.tools.ant.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenLibertyInstaller implements Installer {
    
    private String version;
    private String type;

    @Override
    public void install(InstallLibertyTask task) throws Exception {
        task.log("Installing from Open Liberty repository...");
        String baseUrl = "https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/release/";
        URL versionInfoUrl = new URL(baseUrl + "info.json");
                
        File cacheDir = new File(task.getCacheDir());
        InstallUtils.createDirectory(cacheDir);
        
        // Download version json file
        File versionInfoFile = new File(cacheDir, "openliberty-versions.json");
        task.downloadFile(versionInfoUrl, versionInfoFile);
                
        // Parse JSON
        InputStream versionInfoIs =  new FileInputStream(versionInfoFile);
        String versionInfoTxt = IOUtils.toString(versionInfoIs, StandardCharsets.UTF_8);
        JSONObject versionInfoJson = new JSONObject(versionInfoTxt);
                
        // Validate the runtime version if it is provided
        List<Object> versions = versionInfoJson.getJSONArray("versions").toList();
        if(version != null && !version.isEmpty()) {
            if(!versions.contains(version.trim())) {
                throw new BuildException("Runtime version " + version + " was not found in the Open Liberty repository.");
            }
        }
        else {
            version = String.valueOf(versions.get(versions.size() - 1));
        }
        
        task.log("Using runtime version: " + version);
        
        // Download runtime version details
        String versionUrl = baseUrl + version + "/";
        URL runtimeInfoUrl = new URL(versionUrl + "info.json");
        File runtimeInfoFile = new File(cacheDir, version + ".json");
        task.downloadFile(runtimeInfoUrl, runtimeInfoFile, true);
        
        // Parse JSON
        InputStream runtimeInfoIs =  new FileInputStream(runtimeInfoFile);
        String runtimeInfoTxt = IOUtils.toString(runtimeInfoIs, StandardCharsets.UTF_8);
        JSONObject runtimeInfoJson = new JSONObject(runtimeInfoTxt);
                
        // Get the runtime url. Use the default driver if no runtime type is specified. Otherwise look for the type. 
        String runtimeUrlString = null;
        if(type == null || type.isEmpty()) {
            runtimeUrlString = versionUrl + runtimeInfoJson.getString("driver_location");
        }
        else {
            try {
                JSONArray packageLocations = runtimeInfoJson.getJSONArray("package_locations");
                for(Object p : packageLocations) {
                    String packageName = String.valueOf(p);
                    if(packageName.contains(type)) {
                        runtimeUrlString = versionUrl + packageName;
                        break;
                    }
                }
            }
            catch(JSONException e) {
                throw new BuildException("The specified version only contains the default runtime. Remove the \"type\" parameter and try again.", e);
            }
        }
        
        if(runtimeUrlString == null || runtimeUrlString.isEmpty()) {
            throw new BuildException("Unable to resolve Open Liberty runtime URL.");
        }
        
        task.setRuntimeUrl(runtimeUrlString);
                
        // Download and install the zip from the runtime URL
        File versionCacheDir = new File(task.getCacheDir(), version);
        InstallUtils.createDirectory(versionCacheDir);
        
        URL runtimeUrl = new URL(runtimeUrlString);
        File runtimeFile = new File(versionCacheDir, InstallUtils.getFile(runtimeUrl));
        task.downloadFile(runtimeUrl, runtimeFile, true);
                
        if(runtimeUrlString.endsWith(".jar")) {
            task.installLiberty(runtimeFile);
        }
        else if(runtimeUrlString.endsWith(".zip")){
            task.unzipLiberty(runtimeFile);
        }
        else {
            throw new BuildException("Invalid runtime extension.");
        }
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }

}
