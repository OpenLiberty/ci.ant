## Common Parameters
---

Parameters shared by all the tasks except install-liberty.

#### Parameters

| Attribute | Description | Required |
| --------- | ------------ | ----------|
| installDir | Location of the Liberty profile server installation. | Yes, only if the `ref` attribute is not set. |
| serverName | Name of the Liberty profile server instance. The default value is `defaultServer`. | No |
| userDir | Value of the `${wlp_user_dir}` variable. The default value is `${installDir}/usr/`. | No | 
| outputDir | Value of the `${wlp_output_dir}` variable. The default value is `${installDir}/usr/servers/${serverName}`. | No |
| ref | Reference to an existing server task definition to reuse its server configuration. Configuration such as `installDir`, `userDir`, `outputDir`, and `serverName` are reused from the referenced server task. | No |