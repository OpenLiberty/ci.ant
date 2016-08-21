# ci.ant [![Build Status](https://travis-ci.org/WASdev/ci.ant.svg?branch=master)](https://travis-ci.org/WASdev/ci.ant)

Collection of Ant tasks for managing WebSphere Application Server Liberty Profile and applications.

* [Build](#build)
* [Configuration](#configuration)
* [Tasks](#tasks)

## Build

Use Maven 2.x or 3.x to build the Ant tasks.

* `mvn install` : builds the Ant tasks.
* `mvn install -Poffline-its -DwlpInstallDir=<liberty_install_directory>` : builds the Ant tasks and runs the integration tests by providing an existing installation.
* `mvn install -Ponline-its -DwlpVersion=<liberty_version> -DwlpLicense=<liberty_license_code>` : builds the Ant tasks and runs the integration tests by downloading a new server.
  * Liberty versions and their respective link to the license code can be found in the [index.yml](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/index.yml) file. You can obtain the license code by reading the current license and looking for the D/N: <license code> line.

## Configuration

To use the Liberty Ant tasks in your build scripts you need to:

1. Declare the `antlib` namespace in your `build.xml` file:

 ```ant
<project xmlns:wlp="antlib:net.wasdev.wlp.ant">
</project>
 ```

2. Make Liberty Ant tasks available in your build script by:
 * Copying `wlp-anttasks.jar` into `$ANT_HOME/lib` directory, or
 * Using the `typedef` task to load the Liberty tasks, for example:

   ```ant
   <typedef resource="net/wasdev/wlp/ant/antlib.xml" 
         uri="antlib:net.wasdev.wlp.ant" 
         classpath="target/wlp-anttasks.jar"/>
   ```

   The latest build of `wlp-anttasks.jar` can be obtained from the [Sonatype OSS Maven snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/net/wasdev/wlp/ant/wlp-anttasks/).
                 
## Tasks

The `wlp-anttasks.jar` provides the following tasks.

| Task | Description |
| --------- | ------------ |
| [install-liberty](/docs/install-liberty.md#install-liberty-task) | The `install-liberty` task is used to download and install Liberty profile server. The task can download the Liberty runtime archive from a specified location (via `runtimeUrl`) or automatically resolve it from the [Liberty repository](https://developer.ibm.com/wasdev/downloads/) based on a version and a runtime type. |
| [server](docs/server.md#server-task) | The `server` task supports the operations: `create`, `start`, `run`, `stop`, `status`, `package`, `dump` and `javadump`. |
| [deploy](docs/deploy.md#deploy-task) | The `deploy` task supports deployment of one or more applications to the Liberty Profile server. |
| [undeploy](docs/undeploy.md#undeploy-task) | The `undeploy` task supports undeployment of a single application from the Liberty Profile server. |
| [install-feature](docs/install-feature.md#install-feature-task) | The `install-feature` task installs a feature packaged as a Subsystem Archive (ESA file) to the Liberty runtime. |
| [uninstall-feature](docs/uninstall-feature.md#uninstall-feature-task) | The `uninstall-feature` task uninstalls a feature from the Liberty runtime. |
| [clean](docs/clean.md#clean-task) | The `clean` task deletes every file in the `${wlp_output_dir}/logs`, `${wlp_output_dir}/workarea`, `${wlp_user_dir}/dropins` or `${wlp_user_dir}/apps`. |
| [compileJSPs](docs/compileJSPs#compileJSPs-task) | The `compileJSPs` task compiles JSPs so they wont be done on demand at runtime.
