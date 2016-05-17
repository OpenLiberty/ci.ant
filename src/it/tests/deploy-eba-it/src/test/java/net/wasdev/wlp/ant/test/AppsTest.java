package net.wasdev.wlp.ant.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

public class AppsTest {

    @Test
    public void testSimpleEBA() {
        runTest("test-wab/index.jsp");
    }

    private void runTest(String test) {
        String port = System.getProperty("HTTP_default", "9080");
        try {
            URL url = new URL("http://localhost:" + port + "/" + test);
            InputStream in = url.openConnection().getInputStream();
            try {
                while (in.read() != -1);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }
}
