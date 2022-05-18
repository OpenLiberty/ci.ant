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
        File app = new File(this.getClass().getResource("/testApp/testapp-0.0.1-SNAPSHOT.jar").toURI());
        File libCache = new File(wlpDir, "usr/shared/resources/libIndexCache");
        antTask.setTargetLibCachePath(libCache.getAbsolutePath());
        antTask.setSourceAppPath(app.getAbsolutePath());
        antTask.setTargetThinAppPath(wlpDir.getAbsolutePath() + "/testapp-0.0.1-SNAPSHOT.spring");
        antTask.execute();
        // Check SpringBootUtil outputs. Just verify that the tool has created _some_
        // outputs in the expected locations. Use a test app created with spring initializr and then 
        // stripped down in size by removing content. The resulting test app will not start but is sufficient 
        // to test the repackaging utility. 
        File thin = new File(wlpDir, "testapp-0.0.1-SNAPSHOT.spring");
        assertTrue("Thin spring boot jar not found", thin.exists());

        //check generated libIndexCache. Expected results were determined by executing the open liberty utility
        //    bin/springBootUtility thin --sourceAppPath=testapp-0.0.1-SNAPSHOT.jar
        // and examining the files output.
        String libCacheFilesToVerify[] = {
                "00/26cff293bdba389fbbbc67a20fdd5f73e091554ab46671efa654c25c807ee6/jackson-core-2.12.6.jar",
                "35/446a1421435d45e4c6ac0de3b5378527d5cc2446c07183e24447730ce1fffa/snakeyaml-1.28.jar",
                "36/02428cafcef7819ac1fc718fe5b2ab933944f9f781874cbd44a50273bbcee2/jackson-datatype-jdk8-2.12.6.jar",
                "85/fb03fc054cdf4efca8efd9b6712bbb418e1ab98241c4539c8585bbc23e1b8a/jakarta.annotation-api-1.3.5.jar",
                "b4/539d431f019239699691820dfea70a65cf8e882120a72b3a7713ed1dc66fcb/jackson-datatype-jsr310-2.12.6.jar", 
                "bc/d0e6411465100f2b90c9d7c940d191ef037079662fe82d8aba995511206d42/jackson-module-parameter-names-2.12.6.jar",
                "dd/f46e401a7d9ea3b481c263fa192285d13c50982a5882b22f806639b9645ee4/jackson-annotations-2.12.6.jar" };

        for (String p : libCacheFilesToVerify) {
            if (!new File(libCache, p).exists()) {
                fail("Did not find expected file " + wlpDir + "/" + p + " in the libIndexCache");
            }
        }
    }
}
