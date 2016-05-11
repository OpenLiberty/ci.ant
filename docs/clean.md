## clean task
---

The `clean` task deletes every file in the `${wlp_output_dir}/logs`, `${wlp_output_dir}/workarea`, `${wlp_user_dir}/dropins` or `${wlp_user_dir}/apps`.

#### Parameters

Parameters supported by this task in addition to the [common parameters](common-parameters.md#common-parameters).

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| logs | Delete all the files in the `${wlp_output_dir}/logs` directory. The default value is `true`. | No |
| workarea | Delete all the files in the `${wlp_output_dir}/workarea` directory. The default value is `true`. | No |
| dropins | Delete all the files in the `${wlp_user_dir}/dropins` directory. The default value is `false`. | No |
| apps | Delete all the files in the `${wlp_user_dir}/apps` directory. The default value is `false`. | No |

#### Examples

Remove every app deployed to the `${userDir}/dropins` and every file in the `${wlp_output_dir}/workarea` and `${wlp_output_dir}/logs` directories.
 
```ant
<wlp:clean ref="wlp.ant.test" dropins="true" apps="true" />
```