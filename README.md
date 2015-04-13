# ci.ant [![Build Status](https://travis-ci.org/WASdev/ci.ant.svg?branch=master)](https://travis-ci.org/WASdev/ci.ant)

Collection of Ant tasks for managing WebSphere Application Server Liberty Profile and applications.

* [Build](#build)
* [Configuration](#configuration)
* [Tasks](#tasks)

## Build

Use Maven 2.x or 3.x to build the Ant tasks.

* `mvn install` : builds the Ant tasks.
* `mvn install -Poffline-its-DwlpInstallDir=<liberty_install_directory>` : builds the Ant tasks and runs the integration tests by providing an existing installation.
* `mvn install -Ponline-its-DwlpVersion=<liberty_version> -DwlpLicense=<liberty_license_code>` : builds the Ant tasks and runs the integration tests by downloading a new server.
  * Liberty versions and their respective link to the license code can be found in the [index.yml](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/index.yml) file. You can obtain the license code by reading the current license and looking for the D/N: <license code> line.

## Configuration

To use the Liberty Ant tasks in your build scripts you need to:

1. Declare the `antlib` namespace in your `build.xml` file:

        <project xmlns:wlp="antlib:net.wasdev.wlp.ant">
        </project>

2. Make Liberty Ant tasks available in your build script by:
 * Copying `wlp-anttasks.jar` into `$ANT_HOME/lib` directory, or
 * Using the `typedef` task to load the Liberty tasks, for example:

            <typedef resource="net/wasdev/wlp/ant/antlib.xml" 
                     uri="antlib:net.wasdev.wlp.ant" 
                     classpath="target/wlp-anttasks.jar"/>
          
   The latest build of `wlp-anttasks.jar` can be obtained from the [Sonatype OSS Maven snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/net/wasdev/wlp/ant/wlp-anttasks/).
                 
## Tasks

### install-liberty task
---

The `install-liberty` task is used to download and install Liberty profile server. The task can download the Liberty runtime archive from a specified location (via `runtimeUrl`) or automatically resolve it from the [Liberty repository](https://developer.ibm.com/wasdev/downloads/) based on a version. 

The Liberty license code must always be set in order to install the runtime. If you are installing Liberty from the Liberty repository, you can obtain the license code by reading the [current license](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/8.5.5.5/lafiles/runtime/en.html) and looking for the `D/N: <license code>` line. Otherwise, download the runtime archive and execute `java -jar wlp*runtime.jar --viewLicenseInfo` command and look for the `D/N: <license code>` line.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| licenseCode | Liberty profile license code. See [above](#install-liberty-task). | Yes |
| version | Exact or wildcard version of the Liberty profile server to install. Available versions are listed in the [index.yml](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/index.yml) file. Only used if `runtimeUrl` is not set. The default value is `8.5.+`. | No |
| runtimeUrl | URL to the Liberty profile's `wlp*runtime.jar`. If not set, the Liberty repository will be used to find the Liberty runtime archive. | No |
| baseDir | The base installation directory. The actual installation directory of Liberty profile will be `${baseDir}/wlp`. The default value is `.` (current working directory). | No | 
| cacheDir | The directory used for caching downloaded files such as the license or `.jar` files. The default value is `${java.io.tmpdir}/wlp-cache`. | No | 
| username | Username needed for basic authentication. | No | 
| password | Password needed for basic authentication. | No | 
| maxDownloadTime | Maximum time in seconds the download can take. The default value is `0` (no maximum time). | No | 

#### Examples

    <!-- Install using Liberty repository -->
    <wlp:install-liberty licenseCode="<license code>" />

    <!-- Install from a specific location -->
    <wlp:install-liberty licenseCode="<license code>" runtimeUrl="<url to runtime.jar>"/>

### server task
---

The `server` task supports the following operations:

* `create` - creates a named server instance.
* `start` - starts the named server instance in background. If the server instance does not exist, this option creates one by default.
* `run` - start the named service instance in foreground. If the server instance does not exist, this option creates one by default.
* `stop` - stops the named server.
* `status` - checks the server status.
* `package` - packages the named server and its deployed applications.
* `dump` - dump diagnostic information from the named server into an archive.
* `javadump` - dump diagnostic information from the named server JVM.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| operation | Server operations available as options: `create`, `start`, `stop`, `status`, `package`, `dump`, and `javadump`. | Yes | 
| clean | Clean all cached information on server start up. The default value is `false`. Only used with the `start` operation. | No | 
| timeout | Waiting time before the server starts. The default value is 30 seconds. The unit is milliseconds. Only used with the `start` operation. | No | 
| include | A comma-delimited list of values. The valid values vary depending on the operation. For the `package` operation the valid values are `all`, `usr`, and `minify`. For the `dump` operation the valid values are `heap`, `system`, and `thread`. For the `javadump` operation the valid values are `heap` and `system`. | No. |
| archive | Location of the target archive file. Only used with the `package` or `dump` operations. | No |
| template | Name of the template to use when creating a new server. Only used with the `create` operation. | No |
| resultProperty | Name of a property in which the server status will be stored. By default the server status will be stored under `wlp.<serverName>.status` property. Only used with the `status` operation. | No |
| ref | Reference to an existing server task definition to reuse its server configuration. The value can be null when other required attributes are set. | No | 

#### Examples

    <wlp:server id="wlp.ant.test" installDir="${wlp_install_dir}" operation="start" 
            serverName="${serverName}" userDir="${wlp_usr}" outputDir="${wlp_output}" />

    <wlp:server ref="wlp.ant.test" operation="status"/>


### deploy task
---

The `deploy` task supports deployment of one or more applications to the Liberty Profile server.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| file | Location of a single application to be deployed. See [file attribute in Apache Ant](http://ant.apache.org/manual/Types/fileset.html). The application type can be war, ear, rar, eba, zip , or jar. | Yes, only when the `fileset` attribute is not specified. |
| fileset | Location of multiple applications to be deployed. See [fileset attribute in Apache Ant](http://ant.apache.org/manual/Types/fileset.html). | Yes, only when the `file` attribute is not specified.|
| timeout| Waiting time before the deployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. The value can be null when other required attributes are set. |No |

#### Examples

    <wlp:deploy ref="wlp.ant.test" >
       <fileset dir="${basedir}/resources/">
             <include name="**/*.war"/>                
       </fileset>
    </wlp:deploy>

    <wlp:deploy ref="wlp.ant.test" file="${basedir}/resources/SimpleOSGiApp.eba"  timeout="40000"/>

### undeploy task
---

The `undeploy` task supports undeployment of a single application from the Liberty Profile server.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| file | Name of the application to be undeployed. The application type can be war, ear, rar, eba, zip , or jar. | No |
| patternset | Includes and excludes patterns of applications to be undeployed. See [patternset attribute in Apache Ant](http://ant.apache.org/manual/Types/patternset.html). | No |
| timeout | Waiting time before the undeployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. The value can be null when other required attributes are set. | No | 

When `file` has been set the `patternset` parameter will be ignored, also when the `file` and `patternset` parameters are not set the task will undeploy all the deployed applications.
#### Examples
    <!-- Only undeploys the application "SimpleOSGiApp.eba" -->
    <wlp:undeploy ref="wlp.ant.test" file="SimpleOSGiApp.eba" timeout="60000" />

    <!-- This will undeploy all the applications with ".war" extension except the "example.war" file -->
    <patternset id="mypattern">
        <include name="**/*.war"/>
        <exclude name="example.war" />
    </patternset>
    <wlp:undeploy ref="wlp.ant.test"  timeout="20000" >
        <patternset refid="mypattern" />
    </wlp:undeploy>

    <!-- This will undeploy all the applications previously deployed on the server -->
    <wlp:undeploy ref="wlp.ant.test" timeout="60000" />

### install-feature task
---

The `install-feature` task installs a feature packaged as a Subsystem Archive (ESA file) to the Liberty runtime.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| acceptLicense | Accept feature license terms and conditions. The default value is `false`. | No |
| whenFileExits | Specifies the action to take if a file to be installed already exits. Use `fail` to abort the installation, `ignore` to continue the installation and ignore the file that exists, and `replace` to overwrite the existing file. | No | 
| to | Specifies feature installation location. Set to `usr` to install as a user feature. Otherwise, set it to any configured product extension location. The default value is `usr`. | No |
| name | Specifies the name of the Subsystem Archive (ESA file) to be installed. The name can a feature name, a file name or a URL. | Yes | 

#### Examples
    
	<wlp:install-feature installDir="${wlp_install_dir}" name="mongodb-2.0" whenFileExists="ignore" acceptLicense="true"/>


