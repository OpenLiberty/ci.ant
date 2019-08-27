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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/*
 * Unzip utility. Ensures executable file permissions are preserved.
 */
public class Unzip  {

    public static void unzipToDirectory(File file, File destDir) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        try {
            unzipToDirectory(zipFile, destDir);
        } finally {
            ZipFile.closeQuietly(zipFile);
        }
    }

    private static void unzipToDirectory(ZipFile zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[4096];
        Enumeration entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.isDirectory()) {
                File dir = new File(destDir, entry.getName());
                InstallUtils.createDirectory(dir);
            } else {
                File file = new File(destDir, entry.getName());
                InstallUtils.createDirectory(file.getParentFile());
                OutputStream out = null;
                InputStream in = null;
                try {
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    in = zipFile.getInputStream(entry);
                    Unzip.copy(in, out, buffer);
                } finally {
                    InstallUtils.close(in);
                    InstallUtils.close(out);
                }
                Unzip.setFilePermissions(file, entry);
            }
        }
    }

    private static void copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }

    private static void setFilePermissions(File file, ZipEntry entry) {
        int permissions = entry.getUnixMode();
        if (permissions != 0) {
            UnixPermissions perm = new UnixPermissions(permissions);
            if (perm.isOwnerExecute() || perm.isGroupExecute() || perm.isOwnerExecute()) {
                file.setExecutable(true);
            }
        }
    }
}
