package net.wasdev.wlp.ant.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.wasdev.wlp.ant.server.types.Application;
import net.wasdev.wlp.ant.server.types.Application.ApplicationType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class ServerXmlTest {

    /**
     * A sample server xml file.
     */
    private final File serverXmlFile = new File(getClass().getClassLoader()
            .getResource("sample_server.xml").getFile());

    /**
     * A variable to define a temporary server xml for modications.
     */
    private File tmpServerXml = null;

    /**
     * Assign a temporary server xml file to perform modifications without
     * modifying the original.
     */
    @Before
    public void setUp() {
        try {
            tmpServerXml = Files.createTempFile("tmpServerXml", ".xml")
                    .toFile();
            tmpServerXml.deleteOnExit();

            Files.copy(serverXmlFile.toPath(), tmpServerXml.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test if the add application method throw an exception if the application
     * parameter is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testThatAddApplicationThrowsExceptionIfApplicationIsNull() {
        Application nullApplication = null;

        ServerXml server = new ServerXml(tmpServerXml);
        server.addApplication(nullApplication);
    }

    /**
     * Test if adds a new application.
     */
    @Test
    public void testThatAddsApplication() {
        try {
            Application newApplication = new Application(
                    "C:\\NewApplication.war");

            // Adding the application to the server.xml
            ServerXml server = new ServerXml(tmpServerXml);
            server.addApplication(newApplication);

            Assert.assertEquals(
                    "Failure while trying to add the application to the list of applications.",
                    4, server.getApplications().size());

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(tmpServerXml);

            Assert.assertEquals(
                    "Failure while trying to add the application to the server.xml.",
                    4, document.getElementsByTagName("application").getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test if the default constructor load the applications.
     */
    @Test
    public void testThatApplicationsAreLoadedByConstructor() {
        ServerXml server = new ServerXml(tmpServerXml);

        Assert.assertEquals(
                "The default constructor  does not load the applications", 3,
                server.getApplications().size());

    }

    /**
     * Test if the remove application method throw an exception if the
     * application parameter is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testThatRemoveApplicationThrowsExceptionIfApplicationIsNull() {
        Application nullApplication = null;

        ServerXml server = new ServerXml(tmpServerXml);
        server.removeApplication(nullApplication);
    }

    /**
     * Test if removes an application.
     */
    @Test
    public void testThatRemovesApplication() {
        try {
            Application newApplication = new Application("C:\\DemoEAR.ear");
            newApplication.setId("12345");
            newApplication.setType(ApplicationType.ear);

            // Adding the application to the server.xml
            ServerXml server = new ServerXml(tmpServerXml);
            server.removeApplication(newApplication);

            Assert.assertEquals(
                    "Failure while trying to remove the application to the list of applications.",
                    2, server.getApplications().size());

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(tmpServerXml);

            Assert.assertEquals(
                    "Failure while trying to remove the application to the server.xml.",
                    2, document.getElementsByTagName("application").getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
