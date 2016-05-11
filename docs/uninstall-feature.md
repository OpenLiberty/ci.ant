## uninstall-feature task
---

The `uninstall-feature` task uninstalls a feature from the Liberty runtime.

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------|
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
