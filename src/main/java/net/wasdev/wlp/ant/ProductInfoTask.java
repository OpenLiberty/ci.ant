/**
 * (C) Copyright IBM Corporation 2018.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;

import net.wasdev.wlp.ant.install.ProductInfo;

/**
 * Product info task.
 */
public class ProductInfoTask extends AbstractTask {

    private String cmd;
    private List<ProductInfo> productInfoList;

    @Override
    protected void initTask() {
        super.initTask();

        if (isWindows) {
            cmd = "\"" + installDir + "\\bin\\productInfo.bat\"";
            processBuilder.environment().put("EXIT_ALL", "1");
        } else {
            cmd = installDir + "/bin/productInfo";
        }

        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");

        // Set active directory (install dir)
        processBuilder.directory(installDir);
        processBuilder.environment().put("JAVA_HOME", javaHome);
        processBuilder.redirectErrorStream(true);
    }

    @Override
    public void execute() {
        initTask();

        try {
            doProductInfoVersionVerbose();
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }

    }

    private void doProductInfoVersionVerbose() throws Exception {
        List<String> command = getCommand();
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue());
        productInfoList = ProductInfo.parseProductInfo(outputBuffer.toString());
    }

    private List<String> getCommand() {
        List<String> commands = new ArrayList<String>();
        commands.add(cmd);
        commands.add("version");
        commands.add("--verbose");
        return commands;
    }

    /* productInfo's exit codes */
    public enum ReturnCode {
        OK(0);

        final int val;

        ReturnCode(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }

    public List<ProductInfo> getProductInfoList() {
        return productInfoList;
    }

}
