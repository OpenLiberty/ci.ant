package net.wasdev.wlp.ant.server.types;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.wasdev.wlp.ant.server.types.Application.ApplicationType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ApplicationTest {

    /**
     * An empty DOM Document to define Nodes and Elements.
     */
    private static Document document;

    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    /**
     * Test when two applications are equals with the same fields.
     */
    @Test
    public void testThatApplicationEqualsApplication() {
        Application application1 = new Application("C:\\sample.war");
        application1.setName("WebApp");

        Application application2 = new Application("C:\\sample.war");
        application2.setName("WebApp");

        Assert.assertEquals("Equals method is not working.", application1,
                application2);
    }

    /**
     * Test if a predefined application return an expected node.
     */
    @Test
    public void testThatApplicationToNodeReturnNode() {
        Application sampleApplication = new Application("C:\\sample.war");
        sampleApplication.setName("WebApp");
        sampleApplication.setType(ApplicationType.war);

        // An element is a kind of DOM node.
        Element expectedNode = document.createElement("application");
        expectedNode.setAttribute("name", "WebApp");
        expectedNode.setAttribute("location", "C:\\sample.war");
        expectedNode.setAttribute("type", "war");

        Assert.assertTrue("The application do not return the expected node.",
                expectedNode.isEqualNode(sampleApplication.toNode(document)));
    }

}
