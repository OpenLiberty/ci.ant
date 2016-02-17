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
package net.wasdev.wlp.ant.install;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

public class LibertyYamlTest extends TestCase {

    public void testParse() throws Exception {
        String yml = 
                "# version: uri\n" +
                "---\n" +
                "8.5.5_07:\n" +
                "  uri: http://foobar1\n" +
                "  license: http://license1\n" +
                "  webProfile7: http://webProfile1\n" +
                "  kernel: http://kernel1\n" +
                "  \n" +
                "8.5.5_06:\n" +
                "  uri: http://foobar2\n" +
                "  license: http://license2\n" +
                "\n";
        
        List<LibertyInfo> versions = LibertyYaml.parse(new BufferedReader(new StringReader(yml)));
        
        assertEquals(2, versions.size());
        
        LibertyInfo info;
        
        info = versions.get(0);        
        assertEquals(Version.parseVersion("8.5.5_07"), info.getVersion());
        assertEquals("http://foobar1", info.getUri());
        assertEquals("http://license1", info.getLicenseUri());
        assertEquals("http://kernel1", info.getProperty("kernel"));
        assertEquals("http://webProfile1", info.getProperty("webProfile7"));
        
        info = versions.get(1);        
        assertEquals(Version.parseVersion("8.5.5_06"), info.getVersion());
        assertEquals("http://foobar2", info.getUri());    
        assertEquals("http://license2", info.getLicenseUri());
        assertNull(info.getProperty("kernel"));
        assertNull(info.getProperty("webProfile7"));
    }
   
}
