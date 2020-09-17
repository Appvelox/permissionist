package ru.appvelox.permissionist

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonRequestPermission.setOnClickListener {
            Permissionist.forActivity(this)
                    .addPermission(Manifest.permission.CAMERA)
                    .addPermission(Manifest.permission.WRITE_CONTACTS)
                    .addPermission(Manifest.permission.SEND_SMS)
                    .addPermission(Manifest.permission.READ_SMS)
                    .addPermission(Manifest.permission.GET_ACCOUNTS)
                    .addPermission("invalid name")
                    .addPermission("")
                    .addCommonListener(object : Permissionist.CommonListener {
                        override fun onPermissionsGranted(permissions: List<String>) {
                            Log.d("tag", "onPermissionsGranted: ${Gson().toJson(permissions)}")
                        }

                        override fun onPermissionsDenied(permissions: List<String>) {
                            Log.d("tag", "onPermissionsDenied: ${Gson().toJson(permissions)}")
                        }

                        override fun onFailure(errors: List<Permissionist.Error>) {
                            for (error in errors) {
                                when (error) {
                                    is Permissionist.Error.PermissionsAlreadyGranted -> {
                                        val alreadyGrantedPermissions = error.permissions
                                        Log.d("tag", "onFailure@PermissionsAlreadyGranted : ${Gson().toJson(error)}")
                                    }
                                    is Permissionist.Error.PermissionsDoesNotExist -> {
                                        val invalidPermissions = error.permissions
                                        Log.d("tag", "onFailure@PermissionsDoesNotExist : ${Gson().toJson(error)}")
                                    }
                                    is Permissionist.Error.NoPermissionInManifest -> {
                                        val permissions = error.permissions
                                        Log.d("tag", "onFailure@NoPermissionInManifest : ${Gson().toJson(error)}")
                                    }
                                    is Permissionist.Error.NoPermissionsProvided -> {
                                        Log.d("tag", "onFailure@NoPermissionsProvided : ${Gson().toJson(error)}")
                                    }
                                }
                            }
                        }
                    })
                    .request()
        }

        buttonRequestPermissionWithSystemRationale.setOnClickListener {
            Permissionist.forActivity(this)
                    .addPermission(Manifest.permission.CAMERA, object : Permissionist.SingleListener {
                        override fun onPermissionGranted(permission: String) {
//                            Do something if permission granted
                            Log.d("tag", "onPermissionGranted: ${Gson().toJson(permission)}")
                        }

                        override fun onPermissionDenied(permission: String) {
//                            Do something if permission denied
                            Log.d("tag", "onPermissionDenied: ${Gson().toJson(permission)}")
                        }
                    })
                    .withRationale(editTextSystemRationaleTitle.text.toString(), editTextSystemRationaleMessage.text.toString(), "Ok", "Cancel")
                    .request()
        }

        buttonRequestPermissionWithCustomRationale.setOnClickListener {
            Permissionist.forActivity(this)
                    .addPermission(Manifest.permission.CAMERA)
                    .withRationale(CustomRationaleDialog())
                    .request()
        }
    }
}
