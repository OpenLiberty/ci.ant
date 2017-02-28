## compileJSPs task
---

The 'compileJSPs' task will compile jsps. It has two modes of operation, the first allows it to compile the JSPs in specified war file, and merge the compiled JSPs in. The second will compile JSPs from a source directory. To compile the JSPs it requires an install of Liberty, it creates and starts a server in order to trigger the JSP compilation.

#### Paramters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| war | The file path to a war file containing JSPs that should be compiled | Required unless srcdir is set
| srcdir | The path to a directory containing JSPs to be compiled | Required unless war is set
| destdir | The path where the compiled JSPs should end up | Required if srcdir is set
| source | The version of Java the source files are in. Can be 1.5, 1.6, 6, 1.7, 7, 1.8, 8. If not set defaults to ant Java version | No
| jspVersion | The version of JSP for the JSPs. The installed Liberty server needs to support the required version of JSPs. Can be 2.2, 2.3. Defaults to 2.3 | No
| features | A comma separated list of Liberty features that are required to compile the JSPs | No
| classpath | The classpath required to compile the JSPs | No
| classpathref | A ref to a path for compiling the JSPs | No
| timeout | Stop the server if jsp compile isn't finish within the specified time (given in seconds). Default: 30 sec | No
| tmpdir | The path where a temporary Liberty server directory is created. Default is system tmp directory. | No
| cleanup | Whether the temporary Liberty server directory should be deleted afterwards. Default: `true` | No

**Note:** If the attribute `tmpdir` is not given, this task will create a a random directory for the temporary Liberty server in the system tmp dir.
Something line `/tmp/compileJsp7857999246295419245`. If `tmpdir` is given, the name is always `${tmpdir}/jspCompile` (without random digits).
Short: If you set the attribute `tmpdir` it must be project-specific, so that multiple parallel builds don't influence each other.


#### Examples

Compile the JSPs in a source folder

    <compileJSPs srcdir="src/main/webapp" destdir="target/classes" installDir="${wlp.install.dir}"/> 

Compile the JSPs in a war

    <compileJSPs war="target/my-web.war" installDir="${wlp.install.dir}"/>
