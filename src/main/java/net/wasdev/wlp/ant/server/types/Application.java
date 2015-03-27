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
package net.wasdev.wlp.ant.server.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An abstract representation of an application node from the server.xml
 */
public class Application {

    /**
     * A public enum with supported application types.
     */
    public enum ApplicationType {
        /**
         * An Enterprise Archive.
         */
        ear,

        /**
         * A Web Application Archive.
         */
        war,

        /**
         * An Enterprise Bundle Archive.
         */
        eba;
    }

    private String id;

    private String location;
    private String name;
    private ApplicationType type;
    private String contextRoot;

    /**
     * Instantiate a new application with a defined location.
     *
     * @param location
     *            The path to the application.
     */
    public Application(String location) {
        this.location = location;
    }

    /**
     * Instantiate a new application with null fields.
     */
    @Deprecated
    public Application() {
        //This constructor is used to keep compatibility with ant
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Application)) {
            return false;
        }
        Application other = (Application) obj;
        if (contextRoot == null) {
            if (other.contextRoot != null) {
                return false;
            }
        } else if (!contextRoot.equals(other.contextRoot)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public ApplicationType getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (contextRoot == null ? 0 : contextRoot.hashCode());
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (location == null ? 0 : location.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        return result;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(String location) {
        if (location != null && !location.isEmpty()) {
            this.location = location;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ApplicationType type) {
        this.type = type;
    }

    /**
     * This method constructs a DOM node that represents the server application.
     *
     * @param document
     *            The Document where this node will belong.
     * @return A DOM node. If the method cannot construct the node then return a
     *         null object.
     */
    public Node toNode(Document document) {
        Element element = document.createElement("application");
        List<Attr> attibutesList = new ArrayList<Attr>();

        Map<String, String> attributes = new HashMap<String, String>();

        attributes.put("context-root", contextRoot);
        attributes.put("id", id);
        attributes.put("location", location);
        attributes.put("name", name);

        if (type != null) {
            attributes.put("type", type.toString());
        }

        for (String key : attributes.keySet()) {

            // If the attribute is null or is not set then is not necessary to
            // add it
            if (attributes.get(key) != null && !attributes.get(key).isEmpty()) {

                Attr attribute = document.createAttribute(key);
                attribute.setValue(attributes.get(key));
                attibutesList.add(attribute);
            }
        }

        for (Attr attribute : attibutesList) {
            element.setAttributeNode(attribute);
        }

        return element;
    }

}
