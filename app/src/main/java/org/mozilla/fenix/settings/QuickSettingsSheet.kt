/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mozilla.components.support.ktx.android.net.hostWithoutCommonPrefixes
import mozilla.components.support.ktx.kotlin.toUri
import org.mozilla.fenix.R
import org.mozilla.fenix.settings.PhoneFeature.NOTIFICATION
import org.mozilla.fenix.settings.PhoneFeature.LOCATION
import org.mozilla.fenix.settings.PhoneFeature.CAMERA
import org.mozilla.fenix.settings.PhoneFeature.MICROPHONE
import org.mozilla.fenix.utils.Settings
import java.lang.IllegalArgumentException
import android.util.TypedValue
import org.jetbrains.anko.textColorResource

private const val KEY_URL = "KEY_URL"
private const val KEY_IS_SECURED = "KEY_IS_SECURED"
typealias OnNeedToRequestPermissions = (permissions: Array<String>) -> Unit

class QuickSettingsSheet : BottomSheetDialogFragment() {

    private val safeArguments get() = requireNotNull(arguments)
    private val url: String by lazy { safeArguments.getString(KEY_URL) }
    private val isSecured: Boolean by lazy { safeArguments.getBoolean(KEY_IS_SECURED) }
    private lateinit var onNeedToRequestPermissions: OnNeedToRequestPermissions
    private lateinit var cameraActionLabel: AppCompatTextView
    private lateinit var microphoneActionLabel: AppCompatTextView
    private lateinit var locationActionLabel: AppCompatTextView
    private lateinit var notificationActionLabel: AppCompatTextView
    private lateinit var settings: Settings
    private val toolbarTextColorId by lazy {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.toolbarTextColor, typedValue, true)
        typedValue.resourceId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_quick_settings_sheet, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        settings = Settings.getInstance(requireContext())

        bindUrl(rootView)
        bindSecurityInfo(rootView)
        cameraActionLabel = bindPhoneFeatureItem(rootView, CAMERA)
        locationActionLabel = bindPhoneFeatureItem(rootView, LOCATION)
        microphoneActionLabel = bindPhoneFeatureItem(rootView, MICROPHONE)
        notificationActionLabel = bindPhoneFeatureItem(rootView, NOTIFICATION)
    }

    fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        if (!grantResults.all { it == PERMISSION_GRANTED }) {
            return
        }

        val feature = PhoneFeature.findFeatureBy(permissions)
        val actionLabel = when (feature) {
            CAMERA -> cameraActionLabel
            LOCATION -> locationActionLabel
            MICROPHONE -> microphoneActionLabel
            NOTIFICATION -> notificationActionLabel
            else -> throw IllegalArgumentException("$feature can't be updated")
        }
        handleAction(actionLabel, feature)
    }

    private fun bindUrl(view: View) {
        val urlLabel = view.findViewById<AppCompatTextView>(R.id.url)
        urlLabel.text = url.toUri().hostWithoutCommonPrefixes
    }

    private fun bindSecurityInfo(view: View) {
        val securityInfoLabel = view.findViewById<AppCompatTextView>(R.id.security_info)
        val stringId: Int
        val drawableId: Int
        val drawableTint: Int

        if (isSecured) {
            stringId = R.string.quick_settings_sheet_secure_connection
            drawableId = R.drawable.mozac_ic_lock
            drawableTint = R.color.photonGreen50
        } else {
            stringId = R.string.quick_settings_sheet_insecure_connection
            drawableId = R.drawable.mozac_ic_globe
            drawableTint = R.color.photonRed50
        }

        val icon = AppCompatResources.getDrawable(requireContext(), drawableId)
        icon?.setTint(ContextCompat.getColor(requireContext(), drawableTint))
        securityInfoLabel.setText(stringId)
        securityInfoLabel.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
    }

    private fun bindPhoneFeatureItem(view: View, phoneFeature: PhoneFeature): AppCompatTextView {
        val actionLabel = view.findViewById<AppCompatTextView>(phoneFeature.actionId)

        if (!phoneFeature.isAndroidPermissionGranted(requireContext())) {
            handleBlockedByAndroidAction(actionLabel, phoneFeature)
        } else {
            handleAction(actionLabel, phoneFeature)
        }
        return actionLabel
    }

    private fun handleBlockedByAndroidAction(
        actionLabel: AppCompatTextView,
        phoneFeature: PhoneFeature
    ) {
        actionLabel.setText(R.string.phone_feature_blocked_by_android)
        actionLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.photonBlue50))
        actionLabel.tag = phoneFeature
        actionLabel.setOnClickListener {
            val feature = it.tag as PhoneFeature
            onNeedToRequestPermissions.invoke(feature.androidPermissionsList)
        }
    }

    private fun handleAction(actionLabel: AppCompatTextView, phoneFeature: PhoneFeature) {

        //if (settings.shouldRecommendedSettingsBeActivated) {
            actionLabel.text = phoneFeature.actionLabel
            actionLabel.textColorResource = toolbarTextColorId
            actionLabel.isEnabled = false
       // }
    }

    private val PhoneFeature.actionLabel: String
        get() {
            return when (this) {
                CAMERA -> settings
                    .getSitePermissionsPhoneFeatureCameraAction()
                    .toString(requireContext())

                LOCATION -> settings
                    .getSitePermissionsPhoneFeatureLocation()
                    .toString(requireContext())

                MICROPHONE -> settings
                    .getSitePermissionsPhoneFeatureMicrophoneAction()
                    .toString(requireContext())

                NOTIFICATION -> settings
                    .getSitePermissionsPhoneFeatureNotificationAction()
                    .toString(requireContext())
            }
        }

    private val PhoneFeature.actionId: Int
        get() {
            return when (this) {
                CAMERA -> R.id.camera_action_label
                LOCATION -> R.id.location_action_label
                MICROPHONE -> R.id.microphone_action_label
                NOTIFICATION -> R.id.notification_action_label
            }
        }

    companion object {

        fun newInstance(
            url: String,
            isSecured: Boolean,
            onNeedToRequestPermissions: OnNeedToRequestPermissions
        ): QuickSettingsSheet {

            val fragment = QuickSettingsSheet()
            val arguments = fragment.arguments ?: Bundle()

            with(arguments) {
                putString(KEY_URL, url)
                putBoolean(KEY_IS_SECURED, isSecured)
            }
            fragment.arguments = arguments
            fragment.onNeedToRequestPermissions = onNeedToRequestPermissions
            return fragment
        }
    }
}
