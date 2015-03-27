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
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import net.wasdev.wlp.ant.server.types.Application;
import net.wasdev.wlp.ant.server.types.Application.ApplicationType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to handle operations over the Liberty server.xml file. This class
 * provides methods to:
 * <ul>
 * <li>Add/remove an application</li>
 * <li>Get available applications in the server.xml file</li>
 * </ul>
 */
public class ServerXml extends XmlCommons {

    /**
     * A variable to control the applications in the server.xml
     */
    private List<Application> applications = new ArrayList<Application>();

    public ServerXml(File file) {
        super(file);
        loadApplications();
    }

    /**
     * Add an application to the server xml file. If the provided application
     * has the same location as any other application in the server xml file
     * then this method will not add the new application.
     *
     * @param application
     *            An application to add
     * @throws IllegalArgumentException
     *             If the application is null
     */
    public void addApplication(Application application) {

        if (application == null) {
            throw new IllegalArgumentException(
                    "The application cannot be null.");
        }

        // If the application does not have the same location as any other
        // application in the server xml then add it
        if (findNodeByAttribute("application", "location",
                application.getLocation()) == null) {
            // Adding the new application
            applications.add(application);

            addNode(getDocument().getElementsByTagName("server").item(0),
                    application.toNode(getDocument()));
            try {
                save();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Application> getApplications() {
        return applications;
    }

    /**
     * Load all current applications found in the server.xml file into a list.
     */
    private final void loadApplications() {

        NodeList applications = getDocument().getElementsByTagName(
                "application");

        for (int i = 0; i < applications.getLength(); i++) {
            if (!applications.item(i).hasAttributes()) {
                break; // No attributes are defined , so don't do anything else
            }

            NamedNodeMap attributes = applications.item(i).getAttributes();

            if (attributes.getNamedItem("location") != null) {
                Application application = new Application(attributes
                        .getNamedItem("location").getNodeValue());

                if (attributes.getNamedItem("context-root") != null) {
                    application.setContextRoot(attributes.getNamedItem(
                            "context-root").getNodeValue());
                }

                if (attributes.getNamedItem("id") != null) {
                    application.setId(attributes.getNamedItem("id")
                            .getNodeValue());
                }

                if (attributes.getNamedItem("location") != null) {
                    application.setLocation(attributes.getNamedItem("location")
                            .getNodeValue());
                }

                if (attributes.getNamedItem("name") != null) {
                    application.setName(attributes.getNamedItem("name")
                            .getNodeValue());
                }

                if (attributes.getNamedItem("type") != null) {
                    ApplicationType type;

                    if (attributes.getNamedItem("type").getNodeValue()
                            .equals("ear")) {
                        type = ApplicationType.ear;
                    } else if (attributes.getNamedItem("type").getNodeValue()
                            .equals("eba")) {
                        type = ApplicationType.eba;
                    } else if (attributes.getNamedItem("type").getNodeValue()
                            .equals("war")) {
                        type = ApplicationType.war;
                    } else {
                        throw new IllegalArgumentException(
                                "Application type not supported.");
                    }

                    application.setType(type);
                }

                this.applications.add(application);
            }

        }

    }

    /**
     * Remove the exact application that represents the Application object from
     * the server.xml file. If the application is not found then no changes are
     * performed.
     *
     * @param application
     *            An application to remove
     */
    public void removeApplication(Application application) {

        if (application == null) {
            throw new IllegalArgumentException(
                    "The application cannot be null.");
        }

        // Looking for the node in the xml file
        Node node = findNode(application.toNode(getDocument()));

        // If node was not found then is not removed
        if (node != null) {
            removeNode(node);
            try {
                save(); // Removed and updated the server.xml
                applications.remove(application); // Removed from the list
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }

    }

}
