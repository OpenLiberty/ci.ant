## clean task
---

The `clean` task deletes every file in the `${wlp_output_dir}/logs`, `${wlp_output_dir}/workarea`, `${wlp_user_dir}/dropins` or `${wlp_user_dir}/apps`.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes, only if the `ref` attribute is not set. |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No | 
| ref | Reference to an existing server task definition to reuse its server configuration. Configuration such as `installDir`, `userDir`, `outputDir`, and `serverName` are reused from the referenced server task. | No |
| logs | Delete all the files in the `${wlp_output_dir}/logs` directory. The default value is `true`. | No |
| workarea | Delete all the files in the `${wlp_output_dir}/workarea` directory. The default value is `true`. | No |
| dropins | Delete all the files in the `${wlp_user_dir}/dropins` directory. The default value is `false`. | No |
| apps | Delete all the files in the `${wlp_user_dir}/apps` directory. The default value is `false`. | No |

#### Examples

Remove every app deployed to the `${userDir}/dropins` and every file in the `${wlp_output_dir}/workarea` and `${wlp_output_dir}/logs` directories.
 
```ant
<wlp:clean ref="wlp.ant.test" dropins="true" apps="true" />
```