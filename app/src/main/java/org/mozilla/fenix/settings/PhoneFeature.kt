package org.mozilla.fenix.settings

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import mozilla.components.support.ktx.android.content.isPermissionGranted

enum class PhoneFeature(val id: Int, val androidPermissionsList: Array<String>) {
    CAMERA(SitePermissionsManagePhoneFeature.CAMERA_PERMISSION, arrayOf(Manifest.permission.CAMERA)),
    LOCATION(
        SitePermissionsManagePhoneFeature.LOCATION_PERMISSION, arrayOf(
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION
        )
    ),
    MICROPHONE(SitePermissionsManagePhoneFeature.MICROPHONE_PERMISSION, arrayOf(RECORD_AUDIO)),
    NOTIFICATION(SitePermissionsManagePhoneFeature.NOTIFICATION_PERMISSION, emptyArray());

    fun isAndroidPermissionGranted(context: Context): Boolean {
        val permissions = when (this) {
            CAMERA, LOCATION, MICROPHONE -> androidPermissionsList
            NOTIFICATION -> return true
        }
        return context.isPermissionGranted(*permissions)
    }

    companion object {
        fun findFeatureBy(permissions: Array<String>): PhoneFeature? {
            return PhoneFeature.values().find { feature ->
                feature.androidPermissionsList.any { permission ->
                    permission == permissions.first()
                }
            }
        }
    }
}