
# Permissionist
[ ![Download](https://api.bintray.com/packages/appvelox/Permissionist/ru.appvelox.permissionist/images/download.svg?version=0.0.3) ](https://bintray.com/appvelox/Permissionist/ru.appvelox.permissionist/0.0.3/link)

Handy Android library for permission requesting

## Download
Add dependency in your module `build.gradle`
```groovy
dependencies{
    implementation 'ru.appvelox.permissionist:permissionist:0.0.3'
}
```

## Usage
### Request permissions
In order to request permission, you just need  to pass activity in Permissionist's method `forActivity()` and provide valid permission name

```kotlin
Permissionist.forActivity(this)
             .addPermission(Manifest.permission.CAMERA)
             .request()
```
Request multiple permissions simultaneously

```kotlin
Permissionist.forActivity(this)
                .addPermission(Manifest.permission.CAMERA)
                .addPermission(Manifest.permission.WRITE_CONTACTS)
                .request()
```
### Listen to permission requesting result
Add listener for single permission by passing it as optional argument 
```kotlin
Permissionist.forActivity(this)
                .addPermission(Manifest.permission.CAMERA, object: Permissionist.SingleListener{
                        override fun onPermissionGranted(permission: String) {
                            // Do something if permission was granted
                        }

                        override fun onPermissionDenied(permission: String) {
                            // Do something if permission was denied
                        }
                    })
                .request()
```

If you want to listen to errors, you can attach `CommonListener`

```kotlin
Permissionist.forActivity(this)
                .addPermission(Manifest.permission.CAMERA)
                .addPermission(Manifest.permission.WRITE_CONTACTS)
                .addCommonListener(object : Permissionist.CommonListener {
                        override fun onPermissionsGranted(permissions: List<String>) {
                            // These permissions has been granted
                        }

                        override fun onPermissionsDenied(permissions: List<String>) {
                            // These permissions has been denied                            
                        }

                        override fun onFailure(errors: List<Permissionist.Error>) {
                            for (error in errors) {
                                when (error) {
                                    is Permissionist.Error.PermissionsAlreadyGranted -> {
                                        // Permissions have been granted recently
                                        val alreadyGrantedPermissions = error.permissions
                                    }
                                    is Permissionist.Error.PermissionsDoesNotExist -> {
                                        // There are no permissions with provided names
                                        val invalidPermissions = error.permissions
                                    }
                                    is Permissionist.Error.NoPermissionInManifest -> {
                                        // Manifest doesn't contain permissions
                                        val permissions = error.permissions
                                    }
                                    is Permissionist.Error.NoPermissionsProvided -> {
                                        // There are no permissions provided
                                    }
                                }
                            }
                        }
                    })
                .request()
```
### Show rationale
Explain to the user, what your app need some permissions for. Use method `withRationale()` to show system alert dialog with explanation

```kotlin
Permissionist.forActivity(this)
                    .addPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withRationale("Permission required", "We need access to your's device storage", "OK", "CANCEL")
                    .request()
```

Or provide your custom rationale that implements `CustomRationale` interface 

```kotlin 
val customRationale = CustomRationale()
Permissionist.forActivity(this)
                    .addPermission(Manifest.permission.CAMERA)
                    .withRationale(customRationale)
                    .request()
```


```kotlin
class CustomRationale: DialogFragment(), Permissionist.CustomRationale {
    private var onProceedListener: Permissionist.CustomRationale.OnProceedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.custom_rationale, null)

        builder.setView(view)

        view.buttonOk.setOnClickListener {
            onProceedListener?.onProceed()
            dismiss()
        }

        // ... some code

        return builder.create()
    }

    override fun setOnProceedListener(onProceedListener: Permissionist.CustomRationale.OnProceedListener) {
        this.onProceedListener = onProceedListener
    }

    override fun show(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, "custom rationale dialog")
    }
}
```



