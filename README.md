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
          
   The latest build of `wlp-anttasks.jar` can be obtained from the [Sonatype OSS Maven snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/net/wasdev/wlp/ant/wlp-anttasks/).
                 
## Tasks

### server task
---

The `server` task supports the following options to manage the status of a Liberty profile server:

* `create`, which creates a named server instance.
* `start`, which starts the named server instance. If the server instance does not exist, this option creates one by default.
* `stop`, which stops the named server.
* `status`, which checks the server status.
* `package`, which packages the named server and its deployed applications.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
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
| timeout | Waiting time before the undeployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. The value can be null when other required attributes are set. | No | 

#### Examples

    <wlp:undeploy ref="wlp.ant.test" file="SimpleOSGiApp.eba" timeout="60000" />

