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
package net.wasdev.wlp.ant.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.wasdev.wlp.ant.ProductInfoTask;

public class ProductInfoTest {

    private static final File installDir = new File("target/wlp");

    private static class MyProject extends Project {

        @Override
        public void log(Task t, String message, int level) {
            System.out.println(message);
        }
    }

    @BeforeClass
    public static void setup() {
        InstallLibertyTask install = new InstallLibertyTask();
        install.setProject(new Project());
        install.setBaseDir(installDir.getAbsolutePath());
        install.setType("webProfile7");
        install.execute();
    }

    @AfterClass
    public static void tearDown() {
        delete(installDir);
    }

    private static void delete(File f) {
        if (f.isFile()) {
            f.delete();
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
            f.delete();
        }
    }

    @Test
    public void testProductInfoTask() throws URISyntaxException {
        ProductInfoTask task = createTask();
        task.execute();

        List<ProductInfo> productInfoList = task.getProductInfoList();
        String productId = productInfoList.get(0).getProductId();
        String version = productInfoList.get(0).getProductVersion();
        Assert.assertEquals("com.ibm.websphere.appserver", productId);
        Assert.assertNotNull("version should not be null", version);
    }
    
    @Test
    public void testParseProductInfo() throws URISyntaxException, IOException {
        
        URI url = ProductInfoTest.class.getResource("/productInfoOutput.txt").toURI();
        File testFile = new File(url);
        Assert.assertTrue(testFile.exists());

        String content = null;
        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        try {
            StringBuilder builder = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                builder.append(line).append(System.lineSeparator());
            }
            content = builder.toString();
        } finally {
            reader.close();
        }
        
        List<ProductInfo> productInfoList = ProductInfo.parseProductInfo(content);
        Assert.assertEquals(2, productInfoList.size());
        
        String productId = productInfoList.get(0).getProductId();
        String version = productInfoList.get(0).getProductVersion();
        Assert.assertEquals("com.ibm.websphere.appserver", productId);
        Assert.assertEquals("17.0.0.4", version);
        
        productId = productInfoList.get(1).getProductId();
        version = productInfoList.get(1).getProductVersion();
        Assert.assertEquals("example.extension", productId);
        Assert.assertEquals("1.0.0", version);
    }

    private ProductInfoTask createTask() {
        ProductInfoTask productVersion = new ProductInfoTask();
        productVersion.setProject(new MyProject());
        productVersion.setInstallDir(new File(installDir, "wlp"));
        return productVersion;
    }

}
