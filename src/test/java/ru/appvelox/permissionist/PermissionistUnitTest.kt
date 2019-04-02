package ru.appvelox.permissionist

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock


class PermissionistUnitTest {

    var permissionist: Permissionist = spyk(Permissionist, recordPrivateCalls = true)
    var packageManager = mockk<PackageManager>()
    var activity = mockk<AppCompatActivity>()

    val permissionInManifest = Manifest.permission.CAMERA
    val permissionNotInManifest = Manifest.permission.READ_CONTACTS
    val invalidPermission = "-+*"

    @Before
    fun prepare() {
        permissionist = spyk(Permissionist, recordPrivateCalls = true)

        val permissionInManifest = Manifest.permission.CAMERA
        val permissionNotInManifest = Manifest.permission.READ_CONTACTS
        val invalidPermission = "-+*"

        packageManager = mockk<PackageManager>()
        activity = mockk<AppCompatActivity>()
        every { activity.packageManager } returns packageManager
    }

    @Test
    fun forActivity_PermissionistObjectReseted() {
        val activity = mock(AppCompatActivity::class.java)
        permissionist.forActivity(activity)
        verify { permissionist["reset"]() }
    }

    @Test
    fun request_OnInvalidPermission_ShouldThrowException(){

        permissionist.forActivity(activity).addPermission(permissionInManifest).request()

    }
}