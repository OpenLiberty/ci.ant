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

import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public void testParse() throws Exception {
        Version v = null;
        
        v = Version.parseVersion("10.20.30");
        assertEquals(10, v.getMajor());
        assertEquals(20, v.getMinor());
        assertEquals(30, v.getMicro());
        assertEquals(null, v.getQualifier());
        
        v = Version.parseVersion("1.2.3_beta");
        assertEquals(1, v.getMajor());
        assertEquals(2, v.getMinor());
        assertEquals(3, v.getMicro());
        assertEquals("beta", v.getQualifier());
    }
    
    public void testParseWildcard() throws Exception {
        Version v = null;
                
        v = Version.parseVersion("1.+", true);
        assertEquals(1, v.getMajor());
        assertEquals(-1, v.getMinor());
        assertEquals(0, v.getMicro());
        assertEquals(null, v.getQualifier());
        
        v = Version.parseVersion("1.20.+", true);
        assertEquals(1, v.getMajor());
        assertEquals(20, v.getMinor());
        assertEquals(-1, v.getMicro());
        assertEquals(null, v.getQualifier());
        
        v = Version.parseVersion("1.20.30_+", true);
        assertEquals(1, v.getMajor());
        assertEquals(20, v.getMinor());
        assertEquals(30, v.getMicro());
        assertEquals("+", v.getQualifier());
    }
    
    public void testMatchSimple() throws Exception {
        Version v1 = Version.parseVersion("10.20.30");
        assertTrue(v1.match(v1));
        
        Version v2 = Version.parseVersion("1.2.3_beta");
        assertTrue(v2.match(v2));
        
        assertFalse(v1.match(v2));
    }
    
    public void testMatch() throws Exception {
        Version v1 = Version.parseVersion("10.20.30");
        Version v2 = Version.parseVersion("1.2.3_beta");
                
        Version wild = null;
        
        wild = Version.parseVersion("1.+", true);
        assertFalse(wild.match(v1));
        assertTrue(wild.match(v2));
        
        wild = Version.parseVersion("2.+", true);
        assertFalse(wild.match(v1));
        assertFalse(wild.match(v2));
        
        wild = Version.parseVersion("10.+", true);
        assertTrue(wild.match(v1));
        assertFalse(wild.match(v2));
        
        wild = Version.parseVersion("10.20.+", true);
        assertTrue(wild.match(v1));
        assertFalse(wild.match(v2));
        
        wild = Version.parseVersion("10.21.+", true);
        assertFalse(wild.match(v1));
        assertFalse(wild.match(v2));        
    }
}
