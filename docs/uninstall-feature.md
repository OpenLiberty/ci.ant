## uninstall-feature task
---

The `uninstall-feature` task uninstalls a feature from the Liberty runtime.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes, only if the `ref` attribute is not set. |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. Configuration such as `installDir`, `userDir`, `outputDir`, and `serverName` are reused from the referenced server task. | No |
| name | Specifies the feature name to be uninstalled. The name can a short name or a symbolic name of a Subsystem Archive (ESA file). | Yes, only if there are no [nested features](#nested-feature-elements) | 

#### Examples

1. Uninstall a single feature using the `name` parameter.
 ```ant
 <wlp:uninstall-feature installDir="${wlp_install_dir}" name="mongodb-2.0"/>
 ```
 
2. Uninstall one or more features using nested `feature` elements.
 ```ant
 <wlp:uninstall-feature installDir="${wlp_install_dir}">
         <feature>mongodb-2.0</feature>
         <feature>oauth-2.0</feature>
 </wlp:uninstall-feature>
 ```
 
#### Nested Feature Elements

| Element | Description | Required |
| --------- | ------------ | ----------|
| feature | Specifies the feature name to be uninstalled. The name can a short name or a symbolic name of a Subsystem Archive (ESA file). | No |
