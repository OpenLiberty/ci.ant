package net.wasdev.wlp.ant.jsp;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.wasdev.wlp.ant.install.InstallLibertyTask;

public class CompileJSPsTest {

    private static final File installDir = new File("target/wlp");
    private File compileDir = new File("target/compiledJSPs");;

    private static class MyProject extends Project {

        @Override
        public void log(Task t, String message, int level) {
            System.out.println(message);
        }
    }

    @BeforeClass
    public static void setup() {
        InstallLibertyTask install = new InstallLibertyTask();
        install.setProject(new Project());
        install.setBaseDir(installDir.getAbsolutePath());
        install.setType("webProfile7");
        install.execute();
    }

    @AfterClass
    public static void tearDown() {
        delete(installDir);
    }

    @After
    public void cleanCompileDir() {
        delete(compileDir);
    }

    private static void delete(File f) {
        if (f.isFile()) {
            f.delete();
        } else if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
            f.delete();
        }
    }

    @Test
    public void testBasicCompile() throws URISyntaxException {
        URI url = CompileJSPsTest.class.getResource("/goodJsps/good.jsp").toURI();
        createTask(url).execute();

        File f = new File(compileDir, "_good.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
        f = new File(compileDir, "childDir/_good.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());

        f = new File(compileDir, "_switch.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
        f = new File(compileDir, "childDir/_switch.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());

        f = new File(compileDir, "_X_2D__5F__2B__2E__20AC__25_.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
        f = new File(compileDir, "childDir/_X_2D__5F__2B__2E__20AC__25_.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());

        f = new File(compileDir, "_ÄÖÜäöüß.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
        f = new File(compileDir, "childDir/_ÄÖÜäöüß.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
    }

    private CompileJSPs createTask(URI url) {
        CompileJSPs compile = new CompileJSPs();
        compile.setProject(new MyProject());
        compile.setSrcdir(new File(url).getParentFile());
        compile.setDestdir(compileDir);
        compile.setInstallDir(new File(installDir, "wlp"));
        return compile;
    }

    @Test(expected = BuildException.class)
    public void testCompileFailure() throws URISyntaxException {
        URI url = CompileJSPsTest.class.getResource("/badJsps/good.jsp").toURI();
        createTask(url).execute();
    }
}
