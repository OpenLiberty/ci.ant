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

import junit.framework.TestCase;

public class UnixPermissionsTest extends TestCase {

    public void test644() throws Exception {
        UnixPermissions perm = new UnixPermissions(33188); // 644 or -rw-r--r--
        
        assertTrue(perm.isOwnerRead());
        assertTrue(perm.isOwnerWrite());
        assertFalse(perm.isOwnerExecute());
        
        assertTrue(perm.isGroupRead());
        assertFalse(perm.isGroupWrite());
        assertFalse(perm.isGroupExecute());
                
        assertTrue(perm.isOtherRead());
        assertFalse(perm.isOtherWrite());
        assertFalse(perm.isOtherExecute());
    }
    
    public void test754() throws Exception {
        UnixPermissions perm = new UnixPermissions(33260); // 754 or -rwxr-xr--
        
        assertTrue(perm.isOwnerRead());
        assertTrue(perm.isOwnerWrite());
        assertTrue(perm.isOwnerExecute());
        
        assertTrue(perm.isGroupRead());
        assertFalse(perm.isGroupWrite());
        assertTrue(perm.isGroupExecute());
                
        assertTrue(perm.isOtherRead());
        assertFalse(perm.isOtherWrite());
        assertFalse(perm.isOtherExecute());
    }
       
}
