## install-feature task
---

The `install-feature` task installs a feature packaged as a Subsystem Archive (ESA file) to the Liberty runtime. The task will do nothing and output a warning if `bin/installUtility` does not exist in the runtime. 

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| acceptLicense | Accept feature license terms and conditions. The default value is `false`. | No | 
| to | Specifies feature installation location. Set to `usr` to install as a user feature. Otherwise, set it to any configured product extension location. The default value is `usr`. | No |
| from | Specifies a single directory-based repository as the source of the assets. | No |
| name | Specifies the name of the Subsystem Archive (ESA file) to be installed. The name can be a feature name, a file name or a URL. | No | 

To install the features from the `server.xml` file, don't specify any features in the name or in [nested features](uninstall-feature.md#nested-feature-elements).

#### Examples
1. Install a single feature using the `name` parameter.
 ```ant
 <wlp:install-feature installDir="${wlp_install_dir}" name="mongodb-2.0" whenFileExists="ignore" acceptLicense="true"/>
 ```
 
2. Install one or more features using nested `feature` elements.
 ```ant
 <wlp:install-feature installDir="${wlp_install_dir}" whenFileExists="ignore" acceptLicense="true">
         <feature>mongodb-2.0</feature>
         <feature>oauth-2.0</feature>
 </wlp:install-feature>
 ```

3. Install all the not-installed features from the server.
 ```ant
 <wlp:install-feature installDir="${wlp_install_dir}" whenFileExists="ignore" acceptLicense="true"
      serverName="${serverName}" />
 ```
