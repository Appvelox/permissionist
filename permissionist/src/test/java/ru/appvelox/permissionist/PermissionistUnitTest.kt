package ru.appvelox.permissionist

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import io.mockk.*
import org.junit.Before
import org.junit.Test

class PermissionistUnitTest {
    companion object {
        private val permissionist = spyk(Permissionist, recordPrivateCalls = true)
        private val packageManager = mockk<PackageManager>()
        private val activity = mockk<AppCompatActivity>()
        private val builder = spyk(Permissionist.Builder(activity), recordPrivateCalls = true)
        private val commonListener = spyk<Permissionist.CommonListener>()

        private const val permissionInManifest = Manifest.permission.CAMERA
        private const val permissionNotInManifest = Manifest.permission.READ_CONTACTS
        private const val invalidPermission = "-+*"
    }

    @Before
    fun init() {
        every { activity.packageManager } returns packageManager
        every { activity.packageName } returns "mock"
        clearMocks(permissionist)
    }

    @Test
    fun forActivity_shouldCallReset() {
        permissionist.forActivity(activity)
        verify { permissionist["reset"]() }
    }

    @Test
    fun onResult_shouldCallReset() {
        permissionist.onResult(mapOf())
        verify { permissionist["reset"]() }
    }

    @Test
    fun onResult_withGrantedPermissions_shouldCallOnPermissionsGranted() {
        testOnResult()

        verify { commonListener.onPermissionsGranted(any()) }
    }

    @Test
    fun onResult_withDeniedPermissions_shouldCallOnPermissionsDenied() {
        testOnResult()

        verify { commonListener.onPermissionsDenied(any()) }
    }

    @Test
    fun request_onInvalidPermission_shouldCallOnFailure() {
        every { builder["isPermissionExists"](any<String>()) } returns false

        testRequest(invalidPermission)

        verify { commonListener.onFailure(any()) }
    }

    @Test
    fun request_onPermissionNotInManifest_shouldCallOnFailure() {
        every { builder["isPermissionExists"](any<String>()) } returns true
        every { builder["isManifestContainsPermission"](any<String>()) } returns false

        testRequest(permissionNotInManifest)

        verify { commonListener.onFailure(any()) }
    }

    private fun testRequest(permission: String) {
        every { permissionist.forActivity(activity) } returns builder

        permissionist.forActivity(activity)
                .addPermission(permission)
                .addCommonListener(commonListener)
                .request()
    }

    private fun testOnResult() {
        val resultMap = mapOf(
                permissionInManifest to true,
                permissionNotInManifest to false,
                invalidPermission to false
        )

        permissionist.forActivity(activity).addCommonListener(commonListener)
        permissionist.onResult(resultMap)
    }
}