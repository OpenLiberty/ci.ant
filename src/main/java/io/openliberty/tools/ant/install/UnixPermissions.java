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
package io.openliberty.tools.ant.install;

/*
 * Parse out Unix permission mask.
 * Details: http://stackoverflow.com/questions/15055634/understanding-and-decoding-the-file-mode-value-from-stat-function-output.
 */
public class UnixPermissions  {

    private static final int PERM_MASK = 07777;

    private static final int S_IRUSR = 0400;
    private static final int S_IWUSR = 0200;
    private static final int S_IXUSR = 0100;
    private static final int S_IRGRP =  040;
    private static final int S_IWGRP =  020;
    private static final int S_IXGRP =  010;
    private static final int S_IROTH =   04;
    private static final int S_IWOTH =   02;
    private static final int S_IXOTH =   01;

    private int permission;

    public UnixPermissions(int permission) {
        this.permission = permission & PERM_MASK;
    }

    public boolean isOwnerRead() {
        return isPermissionEnabled(permission, S_IRUSR);
    }

    public boolean isOwnerWrite() {
        return isPermissionEnabled(permission, S_IWUSR);
    }

    public boolean isOwnerExecute() {
        return isPermissionEnabled(permission, S_IXUSR);
    }

    public boolean isGroupRead() {
        return isPermissionEnabled(permission, S_IRGRP);
    }

    public boolean isGroupWrite() {
        return isPermissionEnabled(permission, S_IWGRP);
    }

    public boolean isGroupExecute() {
        return isPermissionEnabled(permission, S_IXGRP);
    }

    public boolean isOtherRead() {
        return isPermissionEnabled(permission, S_IROTH);
    }

    public boolean isOtherWrite() {
        return isPermissionEnabled(permission, S_IWOTH);
    }

    public boolean isOtherExecute() {
        return isPermissionEnabled(permission, S_IXOTH);
    }

    private static boolean isPermissionEnabled(int permission, int mask) {
        return ((permission & mask) == mask);
    }

}
