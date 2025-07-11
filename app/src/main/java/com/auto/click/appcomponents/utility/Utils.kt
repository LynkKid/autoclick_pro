package com.auto.click.appcomponents.utility

import android.app.ActivityManager
import android.app.Dialog
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.auto.click.R
import com.auto.click.model.AppInfo
import com.auto.click.modules.popup.adapter.ConfigItem
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Currency

enum class OptionDelay(val value: Int) {
    MILLISECONDS(0),    // Milli giây
    SECONDS(1),         // Giây
    MINUTES(2);         // Phút

    companion object {
        fun fromValue(value: Int): OptionDelay {
            return values().find { it.value == value } ?: MILLISECONDS
        }
    }
}

enum class OptionSelected(val value: Int) {
    INFINITE(0),        // Vô tận
    BY_TIME(1),         // Theo thời gian
    BY_COUNT(2);        // Theo số lần

    companion object {
        fun fromValue(value: Int): OptionSelected {
            return values().find { it.value == value } ?: INFINITE
        }
    }
}

enum class OptionConfig(val value: Int) {
    NONE(0),                    // Không làm gì
    SINGLE_TARGET_MODE(1),      // Mở chế độ một mục tiêu
    MULTI_TARGET_MODE(2),       // Mở chế độ đa mục tiêu
    LOAD_CONFIG(3);             // Tải cấu hình

    companion object {
        fun fromValue(value: Int): OptionConfig {
            return values().find { it.value == value } ?: NONE
        }
    }
}

object Utils {
    fun isMyServiceRunning(context: Context, serviceClass: Class<out Service>) = try {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    } catch (e: Exception) {
        false
    }

    fun checkAndRequestPermissionOverlays(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val mSettingsIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName)
                )
                try {
                    context.startActivity(mSettingsIntent)
                    return true
                } catch (ex: Exception) {
                    Toast.makeText(
                        context,
                        "Unable to launch app draw overlay settings $mSettingsIntent",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
            return false
        } else {
            Toast.makeText(context, "Device does not support app overlay", Toast.LENGTH_SHORT)
                .show()
            return false
        }
    }

    fun isAccessibilityEnabled(context: Context, accessibilityService: Class<*>?): Boolean {
        val expectedComponentName = ComponentName(context, accessibilityService!!)
        val enabledServicesSetting =
            Settings.Secure.getString(context.contentResolver, "enabled_accessibility_services")
                ?: return false
        val colonSplitter = SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val enabledService = ComponentName.unflattenFromString(colonSplitter.next())
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = context.packageName
            return powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            return true // Trên các phiên bản Android cũ hơn, không có tối ưu hóa pin
        }
    }

    fun runOnUiThread(action: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.post(action)
    }

    fun runDelayed(delayMillis: Long, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            action()
        }, delayMillis)
    }

    fun Context.dp2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun View.setEnabledWithAlpha(enabled: Boolean) {
        isEnabled = enabled
        isClickable = enabled
        alpha = if (enabled) 1.0f else 0.4f
    }

    fun Context.centerOfScreen(offsetDp: Float): Pair<Int, Int> {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val offset = dp2px(offsetDp)
        return (screenWidth / 2) - offset to (screenHeight / 2) - offset
    }

    fun showDialogRequestAccessibility(context: Context, action: () -> Unit) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_agerrment_center_popup)
        dialog.window?.apply {
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        val yesBtn: TextView = dialog.findViewById(R.id.tvYes)
        yesBtn.setOnClickListener {
            action()
            dialog.dismiss()
        }

        val noBtn: TextView = dialog.findViewById(R.id.tvNo)
        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showDialogTryRequestAccessibility(context: Context, action: () -> Unit) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_accessibility_invalid_center_popup)
        dialog.window?.apply {
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        val yesBtn: TextView = dialog.findViewById(R.id.tvOk)
        yesBtn.setOnClickListener {
            action()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showDialogSettingAccessibility(context: Context, action1: () -> Unit, action2: () -> Unit) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_accessibility_center_popup)
        dialog.window?.apply {
            val params: WindowManager.LayoutParams = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.90).toInt()
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
        }

        val settingsBt: TextView = dialog.findViewById(R.id.tvOk)
        settingsBt.setOnClickListener {
            action1()
            dialog.dismiss()
        }

        val tutorialBtn: LinearLayout = dialog.findViewById(R.id.lineTutorial)
        tutorialBtn.setOnClickListener {
            action2()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager: PackageManager = context.packageManager
        val installedApps = mutableListOf<AppInfo>()

        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            if ((packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                val appName = packageManager.getApplicationLabel(packageInfo).toString()
                val appIcon = bitmapToBase64(
                    packageManager.getApplicationIcon(packageInfo.packageName).toBitmap()
                )
                val applicationId = packageInfo.packageName

                installedApps.add(AppInfo(appName, appIcon, applicationId = applicationId))
            }
        }

        return installedApps
    }

    fun getItemDataList(): ArrayList<ConfigItem> {
        var arrayList: ArrayList<ConfigItem> = ArrayList()
        val listConfigSaveString = PreferenceHelper.getString(Contact.LIST_CONFIG_SAVE, "")
        if (listConfigSaveString != "") {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<ConfigItem>>() {}.type
            arrayList = gson.fromJson(listConfigSaveString, type)
        }
        return arrayList
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun base64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun getPackageName(context: Context): String {
        return context.packageName
    }

    fun getAppVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown Version"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown Version"
        }
    }

    fun SwitchMaterial.setProSwitchListener(
        isProVersion: () -> Boolean,
        onActionPro: ((b: Boolean) -> Unit),
        onAction: (() -> Unit),
    ) {
        var isListenerEnabled = true

        this.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isListenerEnabled) return@setOnCheckedChangeListener

            if (isChecked) {
                if (isProVersion()) {
                    // Người dùng có phiên bản Pro, cho phép bật và lưu preference
                    onActionPro(true)
                } else {
                    // Người dùng không có phiên bản Pro, revert switch và mở Activity Premium
                    isListenerEnabled = false
                    this.isChecked = false
                    isListenerEnabled = true
                    onAction()
                }
            } else {
                // Khi tắt tính năng, lưu preference không cần kiểm tra Pro version
                onActionPro(false)
            }
        }
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(currencyCode)
        return format.format(amount)
    }
}

fun View.setSafeOnClickListener(debounceTime: Long = 300L, onSafeClick: (View) -> Unit) {
    var lastClickTime = 0L
    this.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            onSafeClick(it)
        }
    }
}

typealias Action = () -> Unit
