package net.wasdev.wlp.ant.jsp;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.junit.Test;

public class CompileJSPsTest {

    private static class MyProject extends Project {

        @Override
        public void log(Task t, String message, int level) {
            System.out.println(message);
        }
    }
    
    @Test
    public void testBasicCompile() throws URISyntaxException {
        URI url = CompileJSPsTest.class.getResource("/goodJsps/good.jsp").toURI();
        File compileDir = new File("compiledJSPs");
        CompileJSPs compile = new CompileJSPs();
        compile.setProject(new MyProject());
        compile.setSrcdir(new File(url).getParentFile());
        compile.setDestdir(compileDir);
        compile.setInstallDir(new File("/Users/nottinga/Documents/runtimes/8.5.5.9/wlp/"));
        compile.execute();
        
        File f = new File(compileDir, "com/ibm/_jsp/_good.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
        f = new File(compileDir, "com/ibm/_jsp/childDir/_good.class");
        assertTrue("The compiled JSP should exist: " + f, f.exists());
    }

    @Test(expected=BuildException.class)
    public void testCompileFailure() throws URISyntaxException {
        URI url = CompileJSPsTest.class.getResource("/badJsps/good.jsp").toURI();
        File compileDir = new File("compiledJSPs");
        CompileJSPs compile = new CompileJSPs();
        compile.setProject(new MyProject());
        compile.setSrcdir(new File(url).getParentFile());
        compile.setDestdir(compileDir);
        compile.setInstallDir(new File("/Users/nottinga/Documents/runtimes/8.5.5.9/wlp/"));
        compile.execute();
    }
}
