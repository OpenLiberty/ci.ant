package net.wasdev.wlp.ant.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlCommonsTest {

    /**
     * A sample xml file.
     */
    private final File sampleXmlFile = new File(getClass().getClassLoader()
            .getResource("sample.xml").getFile());

    /**
     * Test that the add node method append a node.
     */
    @Test
    public void testThatAddsNode() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // Defining new node to add
            Node nodeToAdd = document.createElement("sampleNode");
            nodeToAdd.setTextContent("Sample Content");

            // Adding the node as a child of the DOM document
            XmlCommons xml = new XmlCommons(sampleXmlFile);
            xml.addNode(xml.getDocument().getDocumentElement(), xml
                    .getDocument().importNode(nodeToAdd, true));

            // Saving the modification to the DOM document to a temporary
            // DOM document to verify the changes.
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource domSource = new DOMSource(xml.getDocument());
            DOMResult domResult = new DOMResult();

            transformer.transform(domSource, domResult);

            document = (Document) domResult.getNode();

            Assert.assertEquals("Add function hasn't added the node.", 1,
                    document.getElementsByTagName("sampleNode").getLength());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test if the constructor throws an {@link IllegalArgumentException}
     * whether the file parameter is a directory.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testThatConstructorThrowsIllegalArgumentExceptionIfDirectory() {
        XmlCommons xml = new XmlCommons(new File(getClass().getClassLoader()
                .getResource("").getPath()));
    }

    /**
     * Test if the constructor throws an {@link IllegalArgumentException}
     * whether the file parameter is not a valid xml file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testThatConstructorThrowsIllegalArgumentExceptionIfNotXml() {
        try {
            File tmpFile = Files.createTempFile("tmpFile", ".tmp").toFile();
            tmpFile.deleteOnExit();
            XmlCommons xml = new XmlCommons(tmpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test whether the constructor load the xml file in a document object.
     */
    @Test
    public void testThatDocumentIsLoaded() {
        XmlCommons xml = new XmlCommons(sampleXmlFile);
        Assert.assertNotNull(xml.getDocument());
    }

    /**
     * Test if a node is found in an xml file by a key attribute.
     */
    @Test
    public void testThatFindsNodeByKeyAttribute() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;

            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(sampleXmlFile);

            // Expecting catalog_item node with key attribute "Men's"
            Node expectedNode = document.getElementsByTagName("catalog_item")
                    .item(0);

            XmlCommons xml = new XmlCommons(sampleXmlFile);

            Assert.assertTrue("The node function has not found the node.",
                    expectedNode.isEqualNode(xml.findNodeByAttribute(
                            "catalog_item", "gender", "Men's")));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Test if a node is found in an xml file when a node is provided.
     */
    @Test
    public void testThatFindsNodeByProvidingNode() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;

            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(sampleXmlFile);

            // Expecting node with item number = RRX9856
            Node expectedNode = document.getElementsByTagName("item_number")
                    .item(1);

            // Defining the node to find
            Node nodeToFind = document.createElement("item_number");
            nodeToFind.setTextContent("RRX9856");

            XmlCommons xml = new XmlCommons(sampleXmlFile);

            Assert.assertTrue("The node function has not found the node.",
                    expectedNode.isEqualNode(xml.findNode(nodeToFind)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Test that a node is removed from the DOM document in memory.
     */
    @Test
    public void testThatRemovesNode() {
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(sampleXmlFile);

            NodeList nodes = document.getElementsByTagName("catalog_item");

            XmlCommons xml = new XmlCommons(sampleXmlFile);
            xml.removeNode(nodes.item(1));

            // Saving the modification to the DOM document to a temporary
            // DOM document to verify the changes.
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource domSource = new DOMSource(xml.getDocument());
            DOMResult domResult = new DOMResult();

            transformer.transform(domSource, domResult);

            document = (Document) domResult.getNode();

            Assert.assertEquals(
                    "removeNode method has failed while removing a node.", 1,
                    document.getElementsByTagName("catalog_item").getLength());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test if the save function save the performed changes over the xml file.
     */
    @Test
    public void testThatSavesChanges() {
        try {
            File tmpXml = Files.createTempFile("tmpXml", ".xml").toFile();
            tmpXml.deleteOnExit();

            Files.copy(sampleXmlFile.toPath(), tmpXml.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // Defining new node to add
            Node nodeToAdd = document.createElement("sampleNode");
            nodeToAdd.setTextContent("Sample Content");

            // Modifying and saving
            XmlCommons xml = new XmlCommons(tmpXml);
            xml.addNode(xml.getDocument().getDocumentElement(), xml
                    .getDocument().importNode(nodeToAdd, true));
            xml.save();

            document = documentBuilder.parse(tmpXml);

            Assert.assertEquals(
                    "Changes were not performed by the save function.", 1,
                    document.getElementsByTagName("sampleNode").getLength());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
