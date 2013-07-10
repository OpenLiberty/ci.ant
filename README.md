ci.ant
======

Collection of Ant tasks for managing WebSphere Application Server Liberty Profile and its applications.

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


## Usage
