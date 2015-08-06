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

/**
 * Install feature task.
 */
public abstract class FeatureManagerTask extends AbstractTask {

    protected String cmd;

    // name of the feature to install or URL
    protected String name;

    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            cmd = installDir + "\\bin\\featureManager.bat";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            cmd = installDir + "/bin/featureManager";
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);

    }

    /**
     * @return the feature name
     */
    public String getName() {
        return name;
    }

    /**
     * @param the feature name
     */
    public void setName(String name) {
        this.name = name;
    }

}
