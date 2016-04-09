## deploy task
---

The `deploy` task supports deployment of one or more applications to the Liberty Profile server.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes, only if the `ref` attribute is not set. |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No |
| ref | Reference to an existing server task definition to reuse its server configuration. Configuration such as `installDir`, `userDir`, `outputDir`, and `serverName` are reused from the referenced server task. | No | 
| file | Location of a single application to be deployed. See [file attribute in Apache Ant](http://ant.apache.org/manual/Types/fileset.html). The application type can be war, ear, rar, eba, zip , or jar. | Yes, only when the `fileset` attribute is not specified. |
| fileset | Location of multiple applications to be deployed. See [fileset attribute in Apache Ant](http://ant.apache.org/manual/Types/fileset.html). | Yes, only when the `file` attribute is not specified.|
| timeout| Waiting time before the deployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No |
| finalName| Final name of the application to be deployed, this will be shown in the dropins folder. It is available just for `file` option .| No. If it is not set, the final name will be the same as the file name. | 

#### Examples

1. Using `fileset`.

 ```ant
 <wlp:deploy ref="wlp.ant.test">
     <fileset dir="${basedir}/resources/">
         <include name="**/*.war"/>
     </fileset>
 </wlp:deploy>
 ```

2. Using `file`.

 ```ant
<wlp:deploy ref="wlp.ant.test" file="${basedir}/resources/SimpleOSGiApp.eba" timeout="40000" finalName="finalSimpleOSGiApp"/>
```
