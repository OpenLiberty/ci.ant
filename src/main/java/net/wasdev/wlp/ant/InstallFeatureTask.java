/**
 * (C) Copyright IBM Corporation 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.ant;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Install feature task.
 */
public class InstallFeatureTask extends FeatureManagerTask {

    // accept license
    private boolean acceptLicense = false;

    // install as user or product extension (usr|extension)
    private String to;

    // action to take if a file to be installed already exists (fail|ignore|replace)
    private String whenFileExists;
    
    // local source directory from which features can be installed
    private String from;

    // install features from server.xml
    private boolean installFromServer = false;

    @Override
    public void execute() {
        if ((name == null || name.length() <= 0) && features.isEmpty() && !installFromServer) {
            throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"),
                    "'nested features', 'install from server' or the 'name'"));
        }

        initTask();

        try {
            doInstall();
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void doInstall() throws Exception {
        List<String> command;
        if (!(name == null || name.length() <= 0)) {
            command = initCommand();
            command.add(name);
            processCommand(command);
        } 
        if (!features.isEmpty()) {
            for (Feature feature : features) {
                command = initCommand();
                command.add(feature.getFeature());
                processCommand(command);
            }
        }
        if (installFromServer) {
            if (serverName == null || serverName.isEmpty()) {
                throw new BuildException(MessageFormat.format(messages.getString("error.server.operation.validate"), "serverName"));
            }
            String serverXml;
            if (isWindows) {
                serverXml = userDir + "\\servers\\" + serverName + "\\server.xml";
            } else {
                serverXml = userDir + "/servers/" + serverName + "/server.xml";
            }
            List<String> featuresInstalled = getFeaturesInstalled();
            List<String> serverFeatures = getFeatures(serverXml, true);
            serverFeatures.removeAll(featuresInstalled);

            for (String feature : serverFeatures) {
                command = initCommand();
                command.add(feature);
                processCommand(command);
            }
        }
    }
    
    private List<String> getFeaturesInstalled() throws Exception {
        List<String> features = new ArrayList<String>();
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("featureList");
        command.add("featureList.xml");
        processCommand(command);
        String xml;
        if (isWindows) {
            xml = installDir + "\\featureList.xml";
        } else {
            xml = installDir + "/featureList.xml";
        }
        features = getFeatures(xml, false);
        File xmlFile = new File(xml);
        xmlFile.delete();
        return features;
    }

    private List<String> getFeatures(String xml, boolean isServerXml) throws Exception {
        List<String> features = new ArrayList<String>();
        File fileXml = new File(xml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document docXml = builder.parse(fileXml);
        NodeList nodeFeatures = docXml.getElementsByTagName("feature");

        for (int i = 0; i < nodeFeatures.getLength(); i++) {
            Element feature = (Element) nodeFeatures.item(i);
            if (isServerXml) {
                features.add(feature.getTextContent());
            } else {
                features.add(feature.getAttribute("name"));
            }
        }

        return features;
    }
    
    private List<String> initCommand(){
        List<String> command = new ArrayList<String>();
        command.add(cmd);
        command.add("install");
        if (acceptLicense) {
            command.add("--acceptLicense");
        } else {
            command.add("--viewLicenseAgreement");
        }
        if (to != null) {
            command.add("--to=" + to);
        }
        if (from != null) {
            command.add("--location=" + from);
        }
        if (whenFileExists != null) {
            command.add("--when-file-exists=" + whenFileExists);
        }
        
        return command;
    }
    
    private void processCommand(List<String> command) throws Exception {
        processBuilder.command(command);
        Process p = processBuilder.start();
        checkReturnCode(p, processBuilder.command().toString(), ReturnCode.OK.getValue(), ReturnCode.ALREADY_EXISTS.getValue());
        if (!acceptLicense) {
            throw new BuildException("To install a feature, you must accept the feature's license terms and conditions.");
        }
    }

    /**
     * @return the acceptLicense
     */
    public boolean isAcceptLicense() {
        return acceptLicense;
    }

    /**
     * @param acceptLicense the acceptLicense to set
     */
    public void setAcceptLicense(boolean acceptLicense) {
        this.acceptLicense = acceptLicense;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getWhenFileExists() {
        return whenFileExists;
    }

    public void setWhenFileExists(String whenFileExists) {
        this.whenFileExists = whenFileExists;
    }
    
    public void setInstallFromServer(boolean installFromServer) {
        this.installFromServer = installFromServer;
    }

    public boolean isInstallFromServer() {
        return installFromServer;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
