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

import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

/**
 * Install feature task.
 */
public abstract class FeatureManagerTask extends AbstractTask {

    protected String cmd;

    // name of the feature to install or URL
    protected String name;
    
    // list of features to be installed/uninstalled.
    protected List<Feature> features = new ArrayList<Feature>();
    
    /** Add a Feature object
     * @param feature The Feature object.
     */
    public void addFeature(Feature feature) {
        features.add(feature);
    }

    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            cmd = installDir + "\\bin\\installUtility.bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            cmd = installDir + "/bin/installUtility";
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);
        processBuilder.redirectErrorStream(true);
    }

    /**
     * @return the feature name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the feature name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the features
     */
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * @param features the features to set
     */
    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    /** featureManager's exit codes. */
    public enum ReturnCode {
        OK(0),
        // Jump a few numbers for error return codes
        BAD_ARGUMENT(20),
        RUNTIME_EXCEPTION(21),
        ALREADY_EXISTS(22),
        BAD_FEATURE_DEFINITION(23),
        MISSING_CONTENT(24),
        IO_FAILURE(25),
        PRODUCT_EXT_NOT_FOUND(26),
        PRODUCT_EXT_NOT_DEFINED(27),
        PRODUCT_EXT_NO_FEATURES_FOUND(28),
        NOT_VALID_FOR_CURRENT_PRODUCT(29);

        final int val;

        ReturnCode(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }
    
    /** Class for the nested 'feature' element. */
    public static class Feature {
        private String feature;
        
        /**
         * @return the name of the feature.
         */
        public String getFeature() {
            return feature;
        }
        
        public void addText(String txt) {
            feature = txt;
        }
        
    }
}
