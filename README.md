ci.ant
======

Collection of Ant tasks for managing WebSphere Application Server Liberty Profile and applications.

* [Build](#build)
* [Configuration](#configuration)
* [Tasks](#tasks)

## Build

Use Maven 2.x or 3.x to build the Ant tasks.

* `mvn install` : builds the Ant tasks.
* `mvn install -DwlpInstallDir=<liberty_install_directory>` : builds the Ant tasks and runs the integration tests.
  * Liberty Profile installation is required to run the integration tests.

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
             
                 
## Tasks

### server task
---

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| operation | Server operations available as options: `create`, `start`, `stop`, `status`, and `package`. | Yes | 
| clean | Attributes that determines whether to operate the server using the clean option. | No | 
| timeout | Waiting time before the server starts or stops. The default value is 30 seconds. The unit is milliseconds. | No | 
| archive | Location of the compressed file when packaging a server. The value must be a file name and only works for the `package` option. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. The value can be null when other required attributes are set. | No | 

#### Examples

    <wlp:server id="wlp.ant.test" installDir="${wlp_install_dir}" operation="start" 
            serverName="${serverName}" userDir="${wlp_usr}" outputDir="${wlp_output}" />

    <wlp:server ref="wlp.ant.test" operation="status"/>


### deploy task
---

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

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| timeout | Waiting time before the undeployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. The value can be null when other required attributes are set. | No | 

#### Examples

    <wlp:undeploy ref="wlp.ant.test" file="SimpleOSGiApp.eba" timeout="60000" />


