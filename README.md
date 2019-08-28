# ci.ant [![Build Status](https://travis-ci.com/OpenLiberty/ci.ant.svg?branch=master)](https://travis-ci.org/WASdev/ci.ant) [![Maven Central Latest](https://maven-badges.herokuapp.com/maven-central/io.openliberty.tools/liberty-ant-tasks/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.openliberty.tools%22%20AND%20a%3A%22liberty-ant-tasks%22)

Collection of Ant tasks for managing Open Liberty and WebSphere Liberty servers and applications.

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
<project xmlns:wlp="antlib:io.openliberty.tools.ant">
</project>
 ```

2. Make Liberty Ant tasks available in your build script by:
 * Copying `liberty-ant-tasks.jar` into `$ANT_HOME/lib` directory, or
 * Using the `typedef` task to load the Liberty tasks, for example:

   ```ant
   <typedef resource="io/openliberty/tools/ant/antlib.xml"
         uri="antlib:io.openliberty.tools.ant"
         classpath="target/liberty-ant-tasks.jar"/>
   ```

   The latest build of `liberty-ant-tasks.jar` can be obtained from the [Sonatype OSS Maven snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/io/openliberty/tools/liberty-ant-tasks/).

## Tasks

The `liberty-ant-tasks.jar` provides the following tasks.

| Task | Description |
| --------- | ------------ |
| [install-liberty](/docs/install-liberty.md#install-liberty-task) | The `install-liberty` task is used to download and install a Liberty server. The task can download the Liberty runtime archive from a specified location (via `runtimeUrl`) or automatically resolve it from the [Liberty repository](https://developer.ibm.com/wasdev/downloads/) based on a version and a runtime type. |
| [server](docs/server.md#server-task) | The `server` task supports the operations: `create`, `start`, `run`, `stop`, `status`, `package`, `dump` and `javadump`. |
| [deploy](docs/deploy.md#deploy-task) | The `deploy` task supports deployment of one or more applications to the Liberty server. |
| [undeploy](docs/undeploy.md#undeploy-task) | The `undeploy` task supports undeployment of a single application from the Liberty server. |
| [install-feature](docs/install-feature.md#install-feature-task) | The `install-feature` task installs a feature packaged as a Subsystem Archive (ESA file) to the Liberty runtime. |
| [uninstall-feature](docs/uninstall-feature.md#uninstall-feature-task) | The `uninstall-feature` task uninstalls a feature from the Liberty runtime. |
| [clean](docs/clean.md#clean-task) | The `clean` task deletes every file in the `${wlp_output_dir}/logs`, `${wlp_output_dir}/workarea`, `${wlp_user_dir}/dropins` or `${wlp_user_dir}/apps`. |
| [compileJSPs](docs/compileJSPs.md#compileJSPs-task) | The `compileJSPs` task compiles JSP files so they do not need to be compiled on demand at runtime.
| [springBootUtil](docs/springBootUtil.md#SpringBootUtilTask) | The `SpringBootUtilTask` task thins a spring boot fat jar, pulling out the runtime dependencies into a local library and producing a much smaller jar containing only the application logic. Use this capability to create efficient Docker layers for your Spring Boot application.

=======
