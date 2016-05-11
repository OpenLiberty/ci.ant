## deploy task
---

The `deploy` task supports deployment of one or more applications to the Liberty Profile server.

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------| 
| file | Location of a single application to be deployed. See [file attribute in Apache Ant](http://ant.apache.org/manual/Types/fileset.html). The application type can be war, ear, rar, eba, zip , or jar. | Yes, only when the `fileset` attribute is not specified. |
| fileset | Location of multiple applications to be deployed. See [fileset attribute in Apache Ant](http://ant.apache.org/manual/Types/fileset.html). | Yes, only when the `file` attribute is not specified.|
| timeout | Waiting time before the deployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No |
| deployName | The file name of the deployed application in the `dropins` directory. This option applies only when the `file` attribute is set. | No. By default, the `deployName` is the same as the original file name. | 

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
<wlp:deploy ref="wlp.ant.test" file="${basedir}/resources/myapp-1.0.war" timeout="40000" deployName="myapp.war"/>
```
