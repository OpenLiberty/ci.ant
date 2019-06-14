## server task
---

The `server` task supports the following operations:

* `create` - creates a named server instance.
* `start` - starts the named server instance in background. If the server instance does not exist, this option creates one by default.
* `run` - start the named service instance in foreground. If the server instance does not exist, this option creates one by default.
* `stop` - stops the named server.
* `status` - checks the server status.
* `package` - packages the named server and its deployed applications.
* `dump` - dump diagnostic information from the named server into an archive.
* `javadump` - dump diagnostic information from the named server JVM.

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------| 
| operation | Server operations available as options: `create`, `start`, `run`, `stop`, `status`, `package`, `dump`, and `javadump`. | Yes | 
| clean | Clean all cached information on server start up. The default value is `false`. Only used with the `start` operation. | No | 
| timeout | Waiting time before the server starts. The default value is 30 seconds. The unit is milliseconds. Only used with the `start` operation. | No | 
| include | A comma-delimited list of values. The valid values vary depending on the operation. For the `package` operation the valid values are `all`, `usr`, and `minify`. For the `dump` operation the valid values are `heap`, `system`, and `thread`. For the `javadump` operation the valid values are `heap` and `system`. | Yes, only when the `os` option is set |
| os | A comma-delimited list of operating systems that you want the packaged server to support. Only used with the `package` operation. The 'include' option must be set to 'minify'. | No |
| archive | Location of the target archive file. Only used with the `package` or `dump` operations. | No |
| template | Name of the template to use when creating a new server. Only used with the `create` operation. | No |
| resultProperty | Name of a property in which the server status will be stored. By default the server status will be stored under `wlp.<serverName>.status` property. Only used with the `status` operation. | No |
| noPassword | If true, disable generation of the default keystore password by specifying the --no-password option when creating a new server. This option was added in 18.0.0.3. The default value is false. Only used with the `create` operation. | No | 


#### Examples

1. The `operation` attribute can be set on the `server` task.

 ```ant
<wlp:server id="idMyServer" installDir="${wlp_install_dir}" 
            userDir="${wlp_usr}" outputDir="${wlp_output}" serverName="${serverName}" 
            operation="status"/>
 ```

2. The `operation` attribute can also be set on the `server` task that references another `server` task using the `ref` attribute.

 ```ant
<wlp:server id="idMyServer" installDir="${wlp_install_dir}" 
            userDir="${wlp_usr}" outputDir="${wlp_output}" serverName="${serverName}" 
            operation="status"/>

<wlp:server ref="idMyServer" operation="start"/>
 ```
