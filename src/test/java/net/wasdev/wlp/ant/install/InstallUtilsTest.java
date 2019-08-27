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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.openliberty.tools.ant.install.InstallUtils;
import io.openliberty.tools.ant.install.LibertyInfo;
import io.openliberty.tools.ant.install.Version;
import junit.framework.TestCase;

public class InstallUtilsTest extends TestCase {

    private LibertyInfo createEntry(String version, String uri, String licenseUri) {
        LibertyInfo info = new LibertyInfo(version, new HashMap<String, String>());
        return info;
    }
    
    public void testSelect() throws Exception {
        List<LibertyInfo> versions = new ArrayList<LibertyInfo>();
        
        versions.add(createEntry("8.5.5_4", null, null));
        versions.add(createEntry("8.5.5_2", null, null));
        versions.add(createEntry("8.5.5_3", null, null));
        versions.add(createEntry("9.0.0", null, null));
        versions.add(createEntry("9.5.0", null, null));
                
        Version wild = null;
        
        wild = Version.parseVersion("9.0.+", true);
        assertEquals(versions.get(3), InstallUtils.selectVersion(wild, versions));
        
        wild = Version.parseVersion("9.+", true);
        assertEquals(versions.get(4), InstallUtils.selectVersion(wild, versions));
        
        wild = Version.parseVersion("+", true);
        assertEquals(versions.get(4), InstallUtils.selectVersion(wild, versions));
        
        wild = Version.parseVersion("8.5.+", true);
        assertEquals(versions.get(0), InstallUtils.selectVersion(wild, versions));   
        
        wild = Version.parseVersion("8.5.5_4", true);
        assertEquals(versions.get(0), InstallUtils.selectVersion(wild, versions));
    }
   
}
