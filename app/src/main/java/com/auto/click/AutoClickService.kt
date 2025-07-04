package com.auto.click

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.auto.click.appcomponents.utility.Contact
import com.auto.click.appcomponents.utility.MultiClickSimultaneousMode
import com.auto.click.appcomponents.utility.MultiPointMode
import com.auto.click.appcomponents.utility.OnePointMode
import com.auto.click.appcomponents.utility.OptionConfig
import com.auto.click.appcomponents.utility.PreferenceHelper
import com.auto.click.appcomponents.utility.PressAndHoldMode
import com.auto.click.appcomponents.utility.Utils
import com.auto.click.appcomponents.utility.Utils.dp2px
import com.auto.click.model.AppInfo
import com.auto.click.model.LocationPointsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

var autoClickService: AutoClickService? = null

class AutoClickService : AccessibilityService() {
    private var layoutInflater: LayoutInflater? = null
    private var onePointMode: OnePointMode? = null
    private var multiPointMode: MultiPointMode? = null
    private var pressAndHoldMode: PressAndHoldMode? = null
    private var multiClickSimultaneousMode: MultiClickSimultaneousMode? = null
    var isViewCreated: Boolean = false
    val listApplication = arrayListOf<AppInfo>()
    private var isOpenWithApplication = false

    private var manager: WindowManager? = null

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                Log.d("PHT", "Screen turned off, stopping AutoClickService")
                stopAutoClick()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Đăng ký BroadcastReceiver để lắng nghe sự kiện màn hình khóa
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (this.layoutInflater == null) {
            this.layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (manager == null) {
            manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        if (onePointMode == null) {
            onePointMode = OnePointMode(this.layoutInflater, manager!!, this@AutoClickService)
        }
        if (multiPointMode == null) {
            multiPointMode = MultiPointMode(this.layoutInflater, manager!!, this@AutoClickService)
        }
        if (pressAndHoldMode == null) {
            pressAndHoldMode =
                PressAndHoldMode(this.layoutInflater, manager!!, this@AutoClickService)
        }
        if (multiClickSimultaneousMode == null) {
            multiClickSimultaneousMode =
                MultiClickSimultaneousMode(this.layoutInflater, manager!!, this@AutoClickService)
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d("PHT", packageName)
            if (packageName.contains(Utils.getPackageName(this@AutoClickService))) return
            if (packageName.contains("com.android.systemui") ||
                packageName.contains("inputmethod") || checkForEditText(event)
            ) {
                return
            }
            val appInfo =
                listApplication.find { element -> element.applicationId.contains(packageName) }
            if (appInfo != null) {
                if (!isViewCreated) {
                    when (OptionConfig.fromValue(appInfo.optionConfig)) {
                        OptionConfig.SINGLE_TARGET_MODE -> {
                            createViewOnePointMode(0)
                            isOpenWithApplication = true
                        }

                        OptionConfig.MULTI_TARGET_MODE -> {
                            createViewMultiPointMode(0)
                            isOpenWithApplication = true
                        }

                        OptionConfig.LOAD_CONFIG -> {
                            val dataListConfigSave = Utils.getItemDataList()
                            if (dataListConfigSave[appInfo.indexConfig].configOne != null) {
                                createViewOnePointMode(dataListConfigSave[appInfo.indexConfig].configOne!!.id)
                            } else if (dataListConfigSave[appInfo.indexConfig].configMulti != null) {
                                createViewMultiPointMode(dataListConfigSave[appInfo.indexConfig].configMulti!!.id)
                            }
                            isOpenWithApplication = true
                        }

                        OptionConfig.NONE -> TODO()
                    }
                }
            } else {
                if (isViewCreated && isOpenWithApplication) {
                    removeView()
                    isOpenWithApplication = false
                }
            }
        }
    }

    private fun checkForEditText(accessibilityEvent: AccessibilityEvent?): Boolean {
        if (accessibilityEvent == null) {
            return false
        }
        return if (accessibilityEvent.className != null) {
            "android.widget.EditText".contentEquals(accessibilityEvent.className.toString()) ||
                    "android.inputmethodservice.SoftInputWindow".contentEquals(accessibilityEvent.className.toString())
        } else {
            false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("PHT", "onServiceConnected")
        if (listApplication.isEmpty()) {
            val listApplicationWithConfigString =
                PreferenceHelper.getString(Contact.LIST_APPLICATION_WITH_CONFIG, "")
            var dataListApplicationWithConfig: ArrayList<AppInfo> = ArrayList()
            if (listApplicationWithConfigString != "") {
                val type = object : TypeToken<ArrayList<AppInfo>>() {}.type
                dataListApplicationWithConfig =
                    Gson().fromJson(listApplicationWithConfigString, type)
            }
            dataListApplicationWithConfig.forEach { element ->
                if (element.isChecked) {
                    listApplication.add(element)
                }
            }
        }
        autoClickService = this
    }

    fun createViewOnePointMode(id: Long) {
        onePointMode!!.createView(id)
        isViewCreated = true
    }

    fun createViewMultiPointMode(id: Long) {
        multiPointMode!!.createView(id)
        isViewCreated = true
    }

    fun createViewPressAndHoldMode(id: Long) {
        pressAndHoldMode!!.createView(id)
        isViewCreated = true
    }

    fun createViewMultiClickSimultaneousMode(id: Long) {
        multiClickSimultaneousMode!!.createView(id)
        isViewCreated = true
    }

    fun removeView() {
        if (onePointMode!!.isCreated) {
            onePointMode!!.removeView()
        }
        if (multiPointMode!!.isCreated) {
            multiPointMode!!.removeView()
        }
        if (pressAndHoldMode!!.isCreated) {
            pressAndHoldMode!!.removeView()
        }
        if (multiClickSimultaneousMode!!.isCreated) {
            multiClickSimultaneousMode!!.removeView()
        }
        isViewCreated = false
        val intent = Intent()
        intent.setAction("AUTO_CLICK_CHANGE")
        this.sendBroadcast(intent)
    }

    fun stopAutoClick() {
        if (onePointMode!!.isCreated) {
            onePointMode!!.stopClickingActions()
        }

        if (multiPointMode!!.isCreated) {
            multiPointMode!!.stopClickingActions()
        }

        if (pressAndHoldMode!!.isCreated) {
            pressAndHoldMode!!.stopClickingActions()
        }

        if (multiClickSimultaneousMode!!.isCreated) {
            multiClickSimultaneousMode!!.stopClickingActions()
        }
    }

    fun click(
        locationPoint: LocationPointsModel,
        antiDetection: Boolean
    ) {
        val builder = GestureDescription.Builder()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val path = Path()
        val x = locationPoint.x.toFloat()
        val y = locationPoint.y.toFloat()
        // Xử lý antiDetection
        val antiDetectPosition =
            (PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0) / 100) * 20
        val adjustedX = if (antiDetection) {
            if (antiDetectPosition > 0) x + dp2px(
                Random.nextInt(
                    (-1 * (antiDetectPosition)),
                    antiDetectPosition
                ).toFloat()
            ) else x
        } else x
        val adjustedY = if (antiDetection) {
            if (antiDetectPosition > 0) y + dp2px(
                Random.nextInt(
                    (-1 * (antiDetectPosition)),
                    antiDetectPosition
                ).toFloat()
            ) else y
        } else y
        path.moveTo(adjustedX, adjustedY)
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, 1))
        val gestureDescription = builder.build()
        dispatchGesture(gestureDescription, null, null)
    }

    fun longPressAndHold(
        locationPoint: LocationPointsModel,
        antiDetection: Boolean,
        longClickDuration: Long
    ) {
        val builder = GestureDescription.Builder()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val x = locationPoint.x.toFloat()
        val y = locationPoint.y.toFloat()
        // Xử lý antiDetection
        val antiDetectPosition =
            (PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0) / 100) * 20
        val adjustedX = if (antiDetection) {
            if (antiDetectPosition > 0) x + dp2px(
                Random.nextInt(
                    (-1 * (antiDetectPosition)),
                    antiDetectPosition
                ).toFloat()
            ) else x
        } else x
        val adjustedY = if (antiDetection) {
            if (antiDetectPosition > 0) y + dp2px(
                Random.nextInt(
                    (-1 * (antiDetectPosition)),
                    antiDetectPosition
                ).toFloat()
            ) else y
        } else y
        val path = Path().apply {
            moveTo(adjustedX, adjustedY)
            lineTo(adjustedX, adjustedY)
        }
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, longClickDuration, false))
        val gestureDescription = builder.build()
        dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d("LongPress", "✅ Gesture completed at ($adjustedX, $adjustedY)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e("LongPress", "❌ Gesture cancelled at ($adjustedX, $adjustedY)")
            }
        }, null)
    }

    fun swiping(
        locationPointStartModel: LocationPointsModel,
        locationPointEnd: LocationPointsModel,
        duration: Long = 100,
        antiDetection: Boolean = false,
    ) {
        val builder = GestureDescription.Builder()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val path = Path()
        val startX = if (antiDetection) locationPointStartModel.x + Random.nextInt(
            -20,
            20
        ) else locationPointStartModel.x
        val startY = if (antiDetection) locationPointStartModel.y + Random.nextInt(
            -20,
            20
        ) else locationPointStartModel.y
        val endX =
            if (antiDetection) locationPointEnd.x + Random.nextInt(-20, 20) else locationPointEnd.x
        val endY =
            if (antiDetection) locationPointEnd.y + Random.nextInt(-20, 20) else locationPointEnd.y
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        val gestureDescription = builder.build()
        dispatchGesture(gestureDescription, null, null)

    }

    fun multiClickSimultaneous(
        points: List<LocationPointsModel>,
        antiDetection: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val builder = GestureDescription.Builder()

        for (point in points) {
            val x = point.x.toFloat()
            val y = point.y.toFloat()

            val antiDetectPosition =
                (PreferenceHelper.getInt(Contact.SETTINGS_ANTI_DETECT_POSITIONS, 0) / 100) * 20

            val adjustedX = if (antiDetection && antiDetectPosition > 0) {
                x + dp2px(Random.nextInt(-antiDetectPosition, antiDetectPosition).toFloat())
            } else x

            val adjustedY = if (antiDetection && antiDetectPosition > 0) {
                y + dp2px(Random.nextInt(-antiDetectPosition, antiDetectPosition).toFloat())
            } else y

            val path = Path().apply {
                moveTo(adjustedX, adjustedY)
            }

            // Thêm stroke cho từng điểm vào builder (tất cả delay 0ms, duration 1ms)
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, 1))
        }

        val gesture = builder.build()
        dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d("MultiClick", "✅ All gestures completed.")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e("MultiClick", "❌ Gesture cancelled.")
            }
        }, null)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("PHT", "FloatingClickService onConfigurationChanged")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("PHT", "AutoClickService onUnbind")
        autoClickService = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        unregisterReceiver(screenOffReceiver)
        Log.d("PHT", "AutoClickService onDestroy")
        autoClickService = null
        super.onDestroy()
    }
}