package ru.appvelox.permissionist

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

class PermissionistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissionist)
        val permissions = intent.getStringArrayExtra(EXTRA_PERMISSION_NAME)!!
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {

            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()

            grantedPermissions.addAll(permissions.filterIndexed { index, _ -> grantResults[index] == PackageManager.PERMISSION_GRANTED })
            deniedPermissions.addAll(permissions.filterIndexed { index, _ -> grantResults[index] == PackageManager.PERMISSION_DENIED })

            val resultMap = permissions.associateBy({ it }, { grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED })

            Permissionist.onResult(resultMap)

            finish()
        }
    }

    companion object {
        const val EXTRA_PERMISSION_NAME = "permission name"
        const val EXTRA_RATIONALE = "rationale"

        const val REQUEST_PERMISSION_CODE = 0
    }
}
