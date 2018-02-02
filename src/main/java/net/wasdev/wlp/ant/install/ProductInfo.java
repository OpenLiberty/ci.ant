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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ProductInfo {

    private final String productVersion;
    private final String productId;
    
    public ProductInfo(String version, String productId) {
        this.productVersion = version;
        this.productId = productId;
    }

    public String getProductVersion() {
        return productVersion;
    }
    
    public String getProductId() {
    	return productId;
    }
    
    public static List<ProductInfo> parseProductInfo(String productInfo) throws IOException {
        List<ProductInfo> result = new ArrayList<ProductInfo>();

        BufferedReader productInfoReader = new BufferedReader(new StringReader(productInfo));
        try {
            String line = productInfoReader.readLine();
            while (line != null) {
                if (line.trim().endsWith(":")) {
                    StringBuilder builder = new StringBuilder();
                    // start properties section
                    line = productInfoReader.readLine();
                    while (line != null && !line.trim().endsWith(":")) {
                        builder.append(line).append(System.lineSeparator());
                        line = productInfoReader.readLine();
                    }
                    // store properties
                    BufferedReader propertiesReader = new BufferedReader(new StringReader(builder.toString()));
                    try {
                        Properties properties = new Properties();
                        properties.load(propertiesReader);
                        // get relevant properties
                        String productId = properties.getProperty("com.ibm.websphere.productId");
                        String productVersion = properties.getProperty("com.ibm.websphere.productVersion");
                        result.add(new ProductInfo(productVersion, productId));
                    } finally {
                        propertiesReader.close();
                    }
                }
            }
        } finally {
            productInfoReader.close();
        }
        return result;
    }

}
