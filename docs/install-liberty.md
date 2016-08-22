## install-liberty task
---

The `install-liberty` task is used to download and install Liberty profile server. The task can download the Liberty runtime archive from a specified location (via `runtimeUrl`) or automatically resolve it from the [Liberty repository](https://developer.ibm.com/wasdev/downloads/) based on a version and a runtime type. 

In certain cases, the Liberty license code may need to be provided in order to install the runtime. If the license code is required and if you are installing Liberty from the Liberty repository, you can obtain the license code by reading the [current license](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/8.5.5.7/lafiles/runtime/en.html) and looking for the `D/N: <license code>` line. Otherwise, download the runtime archive and execute `java -jar wlp*runtime.jar --viewLicenseInfo` command and look for the `D/N: <license code>` line.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| licenseCode | Liberty profile license code. See [above](#install-liberty-task). | Yes, if `type` is `webProfile6` or `runtimeUrl` specifies a `.jar` file. |
| version | Exact or wildcard version of the Liberty profile server to install. Available versions are listed in the [index.yml](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/index.yml) file. Only used if `runtimeUrl` is not set. By default, the latest stable release is used. | No |
| type | Liberty runtime type to download from the Liberty repository. Currently, the following types are supported: `kernel`, `webProfile6`, `webProfile7`, and `javaee7`. Only used if `runtimeUrl` is not set. Defaults to `webProfile6` if `licenseCode` is set and `webProfile7` otherwise. | No |
| runtimeUrl | URL to the Liberty profile's `.jar` or a `.zip` file. If not set, the Liberty repository will be used to find the Liberty runtime archive. | No |
| baseDir | The base installation directory. The actual installation directory of Liberty profile will be `${baseDir}/wlp`. The default value is `.` (current working directory). | No | 
| cacheDir | The directory used for caching downloaded files such as the license or `.jar` files. The default value is `${java.io.tmpdir}/wlp-cache`. | No | 
| username | Username needed for basic authentication. | No | 
| password | Password needed for basic authentication. | No | 
| maxDownloadTime | Maximum time in seconds the download can take. The default value is `0` (no maximum time). | No | 
| offline | Enable offline mode. Install without access to a network. The Liberty profile files must be present in the `cacheDir` directory. The default value is `false`. | No |

#### Examples

1. Install Liberty runtime with Java EE 7 Web Profile features from the Liberty repository.

 ```ant
<wlp:install-liberty/>
 ```

2. Install Liberty runtime with Java EE 6 Web Profile features from the Liberty repository (must provide `licenseCode`).

 ```ant
<wlp:install-liberty type="webProfile6" licenseCode="<license code>"/>
 ```

3. Install from a specific location using a zip file.

 ```ant
<wlp:install-liberty runtimeUrl="<url to wlp*.zip>"/>
 ```

4. Install from a specific location using a jar file.

 ```ant
<wlp:install-liberty licenseCode="<license code>" runtimeUrl="<url to runtime.jar>"/>
 ```
