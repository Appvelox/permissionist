package ru.appvelox.permissionist

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable

object Permissionist {
    private var permissions = mutableMapOf<String, SingleListener?>()
    private var alreadyGrantedPermissions = mutableListOf<String>()
    private var notExistingPermissions = mutableListOf<String>()
    private var noPermissionInManifest = mutableListOf<String>()
    private var commonListener: CommonListener? = null

    fun forActivity(activity: AppCompatActivity): Builder {
        reset()
        return Builder(activity)
    }

    private fun permissionGranted(permission: String) {
        permissions[permission]?.onPermissionGranted(permission)
    }

    private fun permissionDenied(permission: String) {
        permissions[permission]?.onPermissionDenied(permission)
    }

    fun onResult(resultMap: Map<String, Boolean>) {
        notifySinglePermissionListeners(resultMap)
        notifyCommonListener(resultMap)
        reset()
    }

    private fun reset() {
        permissions.clear()
        alreadyGrantedPermissions.clear()
        notExistingPermissions.clear()
        commonListener = null
    }

    private fun notifyCommonListener(resultMap: Map<String, Boolean> = mapOf()) {
        notifyAboutGrantedPermissions(resultMap)
        notifyAboutDeniedPermissions(resultMap)
        notifyAboutErrors(resultMap)
    }

    private fun notifyAboutGrantedPermissions(resultMap: Map<String, Boolean>) {
        val grantedPermissions = resultMap.filter { it.value }.keys.toList()
        if (grantedPermissions.isNotEmpty())
            commonListener?.onPermissionsGranted(grantedPermissions)
    }

    private fun notifyAboutDeniedPermissions(resultMap: Map<String, Boolean>) {
        val deniedPermissions = resultMap.filter { !it.value }.keys.toList()
        if (deniedPermissions.isNotEmpty())
            commonListener?.onPermissionsDenied(deniedPermissions)
    }

    private fun notifyAboutErrors(resultMap: Map<String, Boolean>) {
        val errors = mutableListOf<Error>()

        if (alreadyGrantedPermissions.isNotEmpty())
            errors.add(Error.PermissionsAlreadyGranted(alreadyGrantedPermissions))

        if (notExistingPermissions.isNotEmpty())
            errors.add(Error.PermissionsDoesNotExist(notExistingPermissions))

        if(noPermissionInManifest.isNotEmpty())
            errors.add(Error.NoPermissionInManifest(noPermissionInManifest))

        if (resultMap.isEmpty())
            errors.add(Error.NoPermissionsProvided)

        if (errors.isNotEmpty())
            commonListener?.onFailure(errors)
    }

    private fun notifySinglePermissionListeners(resultMap: Map<String, Boolean>) {
        resultMap.forEach { entry ->
            if (entry.value)
                permissionGranted(entry.key)
            else
                permissionDenied(entry.key)
        }
    }

    fun isPermissionGranted(permission: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    class Builder(private val activity: AppCompatActivity) {

        private var rationale: Rationale? = null
        private var customRationale: CustomRationale? = null

        fun addPermission(permission: String, listener: SingleListener? = null): Builder {
            if (!isPermissionExists(permission)) {
                notExistingPermissions.add(permission)
                return this
            }

            if(!isManifestContainsPermission(permission)){
                noPermissionInManifest.add(permission)
                return this
            }

            if (isPermissionGranted(permission, activity)) {
                alreadyGrantedPermissions.add(permission)
                return this
            }

            permissions[permission] = listener
            return this
        }

        fun withRationale(title: String? = null, message: String? = null, positiveButtonText: String, negativeButtonText: String? = null): Builder {
            customRationale = null
            rationale = Rationale(title, message, positiveButtonText, negativeButtonText)
            return this
        }

        fun withRationale(customRationale: CustomRationale): Builder {
            rationale = null
            this.customRationale = customRationale
            return this
        }

        fun addCommonListener(listener: CommonListener): Builder {
            commonListener = listener
            return this
        }

        fun request() {
            customRationale?.let {
                showCustomRationale(it)
                return
            }

            rationale?.let {
                showSystemRationale(it)
                return
            }

            createPermissionistActivity()
        }

        private fun showSystemRationale(rationale: Rationale) {
            val builder = AlertDialog.Builder(activity)

            rationale.title?.let {
                builder.setTitle(it)
            }

            rationale.message?.let {
                builder.setMessage(it)
            }

            builder.setPositiveButton(rationale.positiveButtonText) { _, _ ->
                createPermissionistActivity()
            }

            rationale.negativeButtonText?.let {
                builder.setNegativeButton(it) { _, _ ->
                }
            }

            builder.show()
        }

        private fun showCustomRationale(customRationale: CustomRationale) {
            customRationale.setOnProceedListener(object : CustomRationale.OnProceedListener {
                override fun onProceed() {
                    createPermissionistActivity()
                }
            })

            customRationale.show(activity)
        }

        private fun createPermissionistActivity() {
            val permissions = permissions.keys.toTypedArray()

            if (permissions.isEmpty()) {
                notifyCommonListener(mapOf())
                return
            }

            val intent = Intent(activity, PermissionistActivity::class.java)
            intent.putExtra(PermissionistActivity.EXTRA_PERMISSION_NAME, permissions)
            intent.putExtra(PermissionistActivity.EXTRA_RATIONALE, rationale)
            activity.startActivity(intent)
        }

        private fun isPermissionExists(permission: String): Boolean {
            return try {
                activity.packageManager.getPermissionInfo(permission, 0)
                true
            } catch (exception: PackageManager.NameNotFoundException) {
                false
            }
        }

        private fun isManifestContainsPermission(permission: String): Boolean {
            val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = packageInfo.requestedPermissions ?: return false

            return requestedPermissions.contains(permission)
        }
    }


    interface SingleListener {
        fun onPermissionGranted(permission: String)
        fun onPermissionDenied(permission: String)
    }

    open class SimpleSingleListener : SingleListener {
        override fun onPermissionGranted(permission: String) {}
        override fun onPermissionDenied(permission: String) {}
    }

    interface CommonListener {
        fun onPermissionsGranted(permissions: List<String>)
        fun onPermissionsDenied(permissions: List<String>)
        fun onFailure(errors: List<Error>)
    }

    sealed class Error {
        class PermissionsAlreadyGranted(val permissions: List<String>) : Error()
        class PermissionsDoesNotExist(val permissions: List<String>) : Error()
        class NoPermissionInManifest(val permissions: List<String>) : Error()
        object NoPermissionsProvided : Error()
    }

    class SimpleCommonListener : CommonListener {
        override fun onPermissionsGranted(permissions: List<String>) {
        }

        override fun onPermissionsDenied(permissions: List<String>) {
        }

        override fun onFailure(errors: List<Error>) {
        }
    }

    data class Rationale(val title: String? = null, val message: String? = null, val positiveButtonText: String, val negativeButtonText: String? = null) : Serializable

    interface CustomRationale {
        fun setOnProceedListener(onProceedListener: OnProceedListener)
        fun show(activity: AppCompatActivity)

        interface OnProceedListener {
            fun onProceed()
        }
    }

    class NoPermissionProvidedException private constructor() : Throwable("Provide at least one permission name")
}
