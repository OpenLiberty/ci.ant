## compileJSPs task
---

The 'compileJSPs' task compiles JSP files. It has two modes of operation. The first mode compiles the JSP files in a specified war file and merges the compiled JSP files back in the war file. The second mode compiles the JSP files from a source directory. To compile the JSP files, the task requires a running Liberty server. It creates and starts a server in order to compile the JSP files.

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| war | The file path to a war file containing the JSP files to compile. | Required unless `srcdir` is set.
| srcdir | The path to a directory containing the JSP files to compile. | Required unless `war` is set.
| destdir | The path where the compiled JSP classes are saved. | Required if `srcdir` is set.
| source | The version of Java the source JSP files use. Valid values are 1.5, 1.6, 6, 1.7, 7, 1.8, and 8. If not set, it defaults to the Ant Java version | No
| jspVersion | The JSP version of the JSP files. The installed Liberty server needs to support the required JSP version. Valid values are 2.2 and 2.3. The default is 2.3. | No
| features | A comma separated list of Liberty features that are required to compile the JSP files. | No
| classpath | The classpath to use to compile the JSP files. | No
| classpathref | The classpath to use to compile the JSP files, given as a reference to a path. | No
| timeout | Maximum time to wait (in seconds) for all the JSP files to compile. The server is stopped and the task ends after this specified time. The default value is 30 seconds. | No
| tmpdir | The path where the temporary Liberty server directory is created. The default is the system temp directory. | No
| cleanup | Indicates whether the temporary Liberty server directory should be deleted when the task is complete. The default is `true`. | No

**Note:** If the `tmpdir` parameter is not specified, this task will create a random directory for the temporary Liberty server in the system temp dir.
For example, a directory like `/tmp/compileJsp7857999246295419245` is used. If `tmpdir` is given, the directory created is set to  `${tmpdir}/jspCompile` without random digits. If you set the `tmpdir` parameter, it must be project-specific so that multiple, parallel builds do not collide with each other.


#### Examples

Compile the JSP files in a source folder:

    <compileJSPs srcdir="src/main/webapp" destdir="target/classes" installDir="${wlp.install.dir}"/> 

Compile the JSP files from a war file:

    <compileJSPs war="target/my-web.war" installDir="${wlp.install.dir}"/>
