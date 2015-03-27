/**
 * (C) Copyright IBM Corporation 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.wasdev.wlp.ant.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * A class to handle operations with xml files. The class contains methods to:
 * <ul>
 * <li>Load in memory an xml file.</li>
 * <li>Search a node.</li>
 * <li>Remove a node.</li>
 * <li>Add a node.</li>
 * <li>Save modifications in memory to the original xml file.</li>
 * </ul>
 *
 */
public class XmlCommons {
    /**
     * A variable to store the xml file
     */
    private File file;

    /**
     * The document representation of the xml file
     */
    private Document document;

    /**
     * Public constructor that loads by default the xml file.
     *
     * @param file
     *            The xml file to manage.
     * @throws IllegalArgumentException
     *             If the file is a directory
     */
    public XmlCommons(File file) {

        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "The specified file does not exists.");
        }

        if (file.isDirectory()) {
            throw new IllegalArgumentException(
                    "The file cannot be a directory.");
        }

        try {
            if (Files.probeContentType(file.toPath()) == null
                    || !(Files.probeContentType(file.toPath()).equals(
                            "text/xml") || Files
                            .probeContentType(file.toPath()).equals(
                                    "application/xml"))) {
                throw new IllegalArgumentException(
                        "The file is not a valid xml file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.file = file;
        load();
    }

    /**
     * Add a node to the DOM document.
     *
     * @param parent
     *            A parent node
     * @param child
     *            A child node to be added into the parent
     * @throws IllegalArgumentException
     *             If one of the parameters are null
     */
    public void addNode(Node parent, Node child) {
        if (parent == null || child == null) {
            throw new IllegalArgumentException(
                    "None of the parameters should be null.");
        }

        Node currentNode = parent.appendChild(child);
        Node previousSibling = currentNode.getPreviousSibling();
        // Removing text node from the removed node
        if (previousSibling != null
                && previousSibling.getNodeType() == Node.TEXT_NODE
                && previousSibling.getNodeValue().trim().length() == 0) {
            previousSibling.getParentNode().removeChild(previousSibling);
        }

    }

    /**
     * Find and retrieve the first node that is equal as the specified in the
     * parameter.
     *
     * @param node
     *            A node to compare.
     * @return The reference to the node that is equal. Otherwise a null object.
     */
    public Node findNode(Node node) {
        NodeList nodes = document.getElementsByTagName(node.getNodeName());

        if (nodes.getLength() > 0) {
            for (int i = 0; i < nodes.getLength(); i++) {

                if (nodes.item(i).isEqualNode(node)) {
                    return node;
                }
            }
        }
        return null;

    }

    /**
     * Find and retrieve the first node that has the same value of the specified
     * parameters.
     *
     * @param tag
     *            The xml tag where the node can be found.
     * @param attribute
     *            An attribute of the tag.
     * @param value
     *            The value of the attribute to be compared with the node.
     * @return The node that meet the specified criteria. Otherwise a null
     *         object.
     */
    public Node findNodeByAttribute(String tag, String attribute, String value) {

        NodeList nodes = document.getElementsByTagName(tag);

        if (nodes.getLength() > 0) {
            for (int i = 0; i < nodes.getLength(); i++) {

                NamedNodeMap attributes = nodes.item(i).getAttributes();

                if (attributes.getNamedItem(attribute).getNodeValue()
                        .equals(value)) {
                    return nodes.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Get the current document that represents the xml file.
     *
     * @return The document object from the xml file
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Load and store a DOM representation of the xml file.
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void load() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a node of the DOM document. If the specified node is not found
     * then nothing is removed.
     *
     * @param node
     *            An identical node to the node to be removed from the DOM
     *            document.
     * @throws IllegalArgumentException
     *             If the node is null
     */
    public void removeNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("The node cannot be null.");
        }

        NodeList nodes = document.getElementsByTagName(node.getNodeName());

        if (nodes.getLength() > 0) {
            for (int i = 0; i < nodes.getLength(); i++) {

                if (nodes.item(i).isEqualNode(node)) {
                    Node previousSibling = nodes.item(i).getPreviousSibling();

                    // Removing node from the DOM
                    nodes.item(i).getParentNode().removeChild(nodes.item(i));

                    // Removing text node from the removed node
                    if (previousSibling != null
                            && previousSibling.getNodeType() == Node.TEXT_NODE
                            && previousSibling.getNodeValue().trim().length() == 0) {
                        previousSibling.getParentNode().removeChild(
                                previousSibling);
                    }

                    break;
                }
            }
        }
    }

    /**
     * This method save the changes in the DOM document to the specified file
     * when the object was created.
     *
     * @throws TransformerException
     */
    public void save() throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(file);

        transformer.transform(domSource, streamResult);
    }

}
