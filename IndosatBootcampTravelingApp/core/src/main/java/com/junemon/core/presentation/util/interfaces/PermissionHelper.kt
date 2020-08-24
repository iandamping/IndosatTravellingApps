package com.junemon.core.presentation.util.interfaces

import androidx.fragment.app.Fragment

/**
 * Created by Ian Damping on 04,December,2019
 * Github https://github.com/iandamping
 * Indonesia.
 */
interface PermissionHelper {

    fun requestCameraPermissionsGranted(permissions:Array<String>):Boolean

    fun requestReadPermissionsGranted(permissions:Array<String>):Boolean

    fun Fragment.onRequestPermissionsResult(
        permissionCode:Int,
        requestCode: Int,
        grantResults: IntArray,
        permissionGranted:() ->Unit = {},
        permissionDenied:()->Unit = {}
    )

    fun Fragment.requestingPermission(
        permissions:Array<String>,
        requestCode: Int
    )
}