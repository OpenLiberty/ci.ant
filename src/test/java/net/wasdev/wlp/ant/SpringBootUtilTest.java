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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.openliberty.tools.ant.SpringBootUtilTask;
import io.openliberty.tools.ant.install.InstallLibertyTask;
import net.wasdev.wlp.common.plugins.util.OSUtil;

public class SpringBootUtilTest {

    static private final Boolean isWindows = OSUtil.isWindows();

    private SpringBootUtilTask antTask;

    private static File wlpDir;

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void setupOpenLiberty() throws Exception {
        // Install open-liberty kernel
        InstallLibertyTask install = new InstallLibertyTask();
        install.setProject(new Project());
        install.setBaseDir(testFolder.getRoot().getAbsolutePath());
        wlpDir = new File(testFolder.getRoot(), "wlp");
        install.setType("kernel");
        install.execute();

        // Install springBoot-1.5 feature
        ProcessBuilder pb = new ProcessBuilder();
        Properties sysp = System.getProperties();
        String javaHome = sysp.getProperty("java.home");
        pb.directory(wlpDir);
        pb.environment().put("JAVA_HOME", javaHome);
        pb.redirectErrorStream(true);
        String installUtility;
        if (isWindows) {
            installUtility = wlpDir + "\\bin\\installUtility.bat";
            pb.environment().put("EXIT_ALL", "1");
        } else {
            installUtility = wlpDir + "/bin/installUtility";
        }

        pb.command(installUtility, "install", "springBoot-1.5", "--acceptLicense");
        Process p = pb.start();
        /*
         * wait for installUtility to install feature from Liberty repository. Normally
         * takes 5-10 seconds.
         */
        p.waitFor();
        if (p.exitValue() != 0) {
            fail("Test setup failed installing springBoot-1.5 feature.");
        }
    }

    @Before
    public void setUpNewTask() {
        antTask = new SpringBootUtilTask();
        antTask.setInstallDir(wlpDir.getAbsoluteFile());
    }

    @Test
    public void testOnSpringBootSampleApp() throws Exception {
        File app = new File(this.getClass().getResource("/testApp/wasdev.springBoot-1.0-SNAPSHOT.jar").toURI());
        File libCache = new File(wlpDir, "usr/shared/resources/libIndexCache");
        antTask.setTargetLibCachePath(libCache.getAbsolutePath());
        antTask.setSourceAppPath(app.getAbsolutePath());
        antTask.setTargetThinAppPath(wlpDir.getAbsolutePath() + "/wasdev.springBoot-1.0-SNAPSHOT.spring");
        antTask.execute();
        // Check SpringBootUtil outputs. Just verify that the tool has created _some_
        // outputs in the expected locations.
        // using a stripped down, small jar which will not start but is sufficient to
        // test the repackaging utility
        File thin = new File(wlpDir, "wasdev.springBoot-1.0-SNAPSHOT.spring");
        assertTrue("Thin spring boot jar not found", thin.exists());

        String libCacheFilesToVerify[] = {
                "09/f2ee1404726a06ddd7beeb061a58c0dfe15d6b7c516542d28b6e3521f5589e/spring-boot-starter-web-1.5.15.RELEASE.jar",
                "18/c4a0095d5c1da6b817592e767bb23d29dd2f560ad74df75ff3961dbde25b79/slf4j-api-1.7.25.jar",
                "2c/5d9ed201011c4a1bbe1c4d983645f3c68e6db9ed6267066d204cc1d12e4758/spring-boot-starter-1.5.15.RELEASE.jar",
                "41/6c5a0c145ad19526e108d44b6bf77b75412d47982cce6ce8d43abdbdbb0fac/jul-to-slf4j-1.7.25.jar",
                "f3/9d7ba7253e35f5ac48081ec1bc28c5df9b32ac4b7db20853e5a8e76bf7b0ed/validation-api-1.1.0.Final.jar" };

        for (String p : libCacheFilesToVerify) {
            if (!new File(libCache, p).exists()) {
                fail("Did not find expected file " + wlpDir + "/" + p + " in the libIndexCache");
            }
        }
    }
}
