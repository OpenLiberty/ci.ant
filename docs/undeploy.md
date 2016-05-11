## undeploy task
---

The `undeploy` task supports undeployment of a single application from the Liberty Profile server.

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------| 
| file | Name of the application to be undeployed. The application type can be war, ear, rar, eba, zip , or jar. | No |
| patternset | Includes and excludes patterns of applications to be undeployed. See [patternset attribute in Apache Ant](http://ant.apache.org/manual/Types/patternset.html). | No |
| timeout | Waiting time before the undeployment completes successfully. The default value is 30 seconds. The unit is milliseconds. | No | 

When `file` has been set the `patternset` parameter will be ignored, also when the `file` and `patternset` parameters are not set the task will undeploy all the deployed applications.
#### Examples

1. Undeploy the `myapp.war` application.

 ```ant
<wlp:undeploy ref="wlp.ant.test" file="myapp.war" timeout="60000"/>
 ```

2. Undeploy all the applications with the `.war` extension except the `example.war` file.

 ```ant
 <patternset id="mypattern">
     <include name="**/*.war"/>
     <exclude name="example.war"/>
 </patternset>
 <wlp:undeploy ref="wlp.ant.test"  timeout="20000">
     <patternset refid="mypattern"/>
 </wlp:undeploy>
 ```

3. Undeploy all the applications previously deployed on the server.

 ```ant
<wlp:undeploy ref="wlp.ant.test" timeout="60000"/>
 ```
