## product-info task
---

The `product-info` task displays product information such as product name and version of the Liberty runtime and any installed product extensions.

#### Parameters

Only the [common parameters](common-parameters.md#common-parameters) are supported by this task.

#### Examples

Display the product information.
 
```ant
<wlp:product-info installDir="${wlp_install_dir}" />
```