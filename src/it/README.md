## Integration Tests  

### setup folder 
Contains two modules that build an eba application and a war application respectively. These applications will be used later by the tests in deploy and undeploy operations. 

### tests folder
Contains many separated tests in different folders. In order to run all the tests it is necessary to run the install-server one first, because it is in this test where the Liberty server is downloaded and installed, after that, the server is ready to be used for all the another tests.

The tests contained in this folder are the following:

* `install-server-it`: Download and install a WAS Liberty server.
* `update-server-from-xml-it`: Takes the server.xml configuration and install the missing features in the server.
* `deploy-eba-it`: Install the required features, deploy an eba application, undeploy the application and uninstall the features installed previously. 
* `deploy-war-it`: Deploy and undeploy a web application.  
* `install-features-it`: Install and uninstall features in the Liberty server. 
* `package-server-it`: Package a server into zip file.  

Besides of these tests, this folder contains the basic-it folder. This folder contains the most of the operations supported by the `liberty-ant-tasks` described just in one build.xml file.
Some of these tests are: download and create a server, start the server, deploy and undeploy an application, package a server, stop the server and clean the server.