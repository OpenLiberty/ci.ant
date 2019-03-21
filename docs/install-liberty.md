## install-liberty task
---

The `install-liberty` task is used to download and install a Liberty runtime. The task can download the Liberty runtime archive from a specified location (via `runtimeUrl`) or automatically resolve it from the [Wasdev Liberty repository](https://developer.ibm.com/wasdev/downloads/) or [Open Liberty repository](https://openliberty.io/downloads/) based on a version and a runtime type. 

In certain cases when downloading from the Wasdev Liberty repository, the Liberty license code may need to be provided in order to install the runtime. If the license code is required and if you are installing Liberty from the Liberty repository, you can obtain the license code by reading the [current license](https://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/16.0.0.2/lafiles/runtime/en.html) and looking for the `D/N: <license code>` line. Otherwise, download the runtime archive and execute `java -jar wlp*runtime.jar --viewLicenseInfo` command and look for the `D/N: <license code>` line.

### Parameters

#### Common parameters

The following are common parameters for all of the installation methods:

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| baseDir | The base installation directory. The actual installation directory of Liberty will be `${baseDir}/wlp`. The default value is `.` (current working directory). | No | 
| cacheDir | The directory used for caching downloaded files such as the license or `.jar` files. The default value is `${java.io.tmpdir}/wlp-cache`. | No | 
| maxDownloadTime | Maximum time in seconds the download can take. The default value is `0` (no maximum time). | No | 
| offline | Enable offline mode. Install without access to a network. The Liberty profile files must be present in the `cacheDir` directory. The default value is `false`. | No |

#### Parameters when installing using `runtimeUrl`

The following additional parameters are available when downloading a runtime via a `runtimeUrl`:

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| runtimeUrl | URL to the Liberty profile's `.jar` or a `.zip` file. If not set, the Liberty repository will be used to find the Liberty runtime archive. | No |
| licenseCode | Liberty profile license code. See [above](#install-liberty-task). | No |
| username | Username needed for basic authentication. | No | 
| password | Password needed for basic authentication. | No | 
| useWlpCache | Enables caching of a Liberty `zip`. Defaults to `true`. Only disable caching if the runtimeUrl points to a local Liberty `zip`. | No |

#### Parameters when installing from Wasdev repository (WebSphere Liberty runtimes)

The following additional parameters are available when downloading a WebSphere Liberty runtime from the Wasdev repository:

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| useOpenLiberty | A boolean indicator of whether to use the Open Liberty repository. **This must be set to "false" to use the Wasdev repository.** | No |
| licenseCode | Libert license code. See [above](#install-liberty-task). | No |
| version | Exact or wildcard version of the WebSphere Liberty runtime to install. Available versions are listed in the [index.yml](http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/index.yml) file. By default, the latest stable release is used. | No |
| type | Runtime type to download from the Wasdev repository. Currently, the following types are supported: `kernel`, `webProfile7`, and `javaee7`. Defaults to `webProfile7`. | No |

#### Parameters when installing from Open Liberty repository (Open Liberty runtimes)

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| useOpenLiberty | A boolean indicator of whether to use the Open Liberty repository. **This must be set to "true" to use the Open Liberty repository.** | No |
| version | Exact version of the Open Liberty runtime to install. Available versions are listed in the [info.json](https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/release/info.json) file. By default, the latest stable release is used. | No |
| type | Runtime type to download from the Open Liberty repository. Value must be a substring of the available `package_locations`. For example, [here](https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/release/2018-06-19_0502/info.json) are the package locations for Open Liberty 18.0.0.2. If no `type` is specified, the default runtime provided by `driver_location` will be used. | No |

### Examples

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
