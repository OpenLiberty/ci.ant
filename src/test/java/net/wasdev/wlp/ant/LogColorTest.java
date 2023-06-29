package net.wasdev.wlp.ant;

import org.junit.Assert;
import org.junit.Test;
import io.openliberty.tools.ant.AbstractTask;

public class LogColorTest {
    @Test
    public void testWarningColor() {
        String logMessage = "[WARNING ] CWWKS3103W: There are no users defined for the BasicRegistry configuration of ID com.ibm.ws.security.registry.basic.config[basic].";
        String line = AbstractTask.addColor(logMessage);
        Assert.assertTrue(line.contains(AbstractTask.ANSI_YELLOW));
        Assert.assertTrue(line.contains(AbstractTask.ANSI_RESET));
    }

    @Test
    public void testErrorColor() {
        String logMessage = "[ERROR   ] SRVE9990E: The class mvn.demo.rest.RestApplication has a @WebServlet annotation but does not implement the jakarta.servlet.http.HttpServlet interface.";
        String line = AbstractTask.addColor(logMessage);
        Assert.assertTrue(line.contains(AbstractTask.ANSI_RED));
        Assert.assertTrue(line.contains(AbstractTask.ANSI_RESET));
    }

    @Test
    public void testInfoColor() {
        String logMessage = "[AUDIT   ] CWWKT0016I: Web application available (default_host): http://localhost:9080/openapi/";
        String line = AbstractTask.addColor(logMessage);
        Assert.assertEquals(logMessage, line); // should not have any ANSI colors
    }}
