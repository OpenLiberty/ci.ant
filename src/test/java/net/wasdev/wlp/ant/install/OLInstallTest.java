package net.wasdev.wlp.ant.install;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class OLInstallTest {
    
    private InstallLibertyTask install;

    private File wlpDir;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Before
    public void setup() {        
        wlpDir = new File(testFolder.getRoot(), "wlp");
        install = new InstallLibertyTask();
        install.setProject(new Project());
        install.setBaseDir(testFolder.getRoot().getAbsolutePath());
        install.setInstallDir(wlpDir.getAbsoluteFile());
        install.setUseOpenLiberty(true);
    }
    
    @After
    public void clean() {
        install.setVersion(null);
        install.setType(null);
    }
    
    @Test(expected=BuildException.class)
    public void testBadVersion() {
        install.setVersion("bad");
        install.execute();
    }
    
    @Test
    public void testGoodVersion() {
        install.setVersion("2017-09-27_1951");
        install.execute();
        assertTrue(new File(wlpDir, "lib/ws-launch.jar").exists());
    }
    
    @Test
    public void testDefaultRuntimeUrl() {
        install.execute();
        assertTrue(install.getRuntimeUrl().length() > 0);
        assertTrue(new File(wlpDir, "lib/ws-launch.jar").exists());
    }
    
    @Test
    public void testSpecificRuntimeUrl() {
        install.setVersion("2018-06-19_0502");
        install.setType("javaee8");
        install.execute();
        assertTrue(install.getRuntimeUrl().length() > 0);
        assertTrue(new File(wlpDir, "lib/ws-launch.jar").exists());
    }
    
    @Test(expected=BuildException.class)
    public void testBadType() {
        install.setVersion("2018-06-19_0502");
        install.setType("bad");
        install.execute();
    }

}