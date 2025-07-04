package com.auto.click.appcomponents.utility

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.auto.click.R
import com.auto.click.appcomponents.utility.Utils.centerOfScreen
import com.auto.click.appcomponents.utility.Utils.dp2px
import com.auto.click.autoClickService
import com.auto.click.model.ConfigOneAutoClickModel
import com.auto.click.model.LocationPointsModel
import com.auto.click.model.ModelAddView
import com.auto.click.modules.navigation.ui.MainActivity
import com.auto.click.modules.popup.PopupGameModeTargetEdit
import com.auto.click.modules.popup.PopupSaveConfig
import com.auto.click.modules.popup.PopupSettings
import com.auto.click.modules.popup.adapter.ConfigItem
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.schedule
import kotlin.math.pow
import kotlin.math.sqrt

class OnePointMode(
    private val layoutInflater: LayoutInflater?,
    private val manager: WindowManager,
    private val context: Context,
) {
    private val gson = Gson()
    private var index = 0
    private var viewController: View? = null
    private var viewGameModeController: View? = null
    private var viewMiniMenuController: View? = null
    private lateinit var buttonPlayPause: ImageView
    private lateinit var btAdd: ImageView
    private lateinit var btSwiping: ImageView
    private lateinit var btRemove: ImageView
    private lateinit var btSave: ImageView
    private lateinit var btViewApp: ImageView
    private lateinit var btShowHind: ImageView
    private lateinit var btSettings: ImageView
    private lateinit var btClose: ImageView
    private lateinit var ivMin: ImageView
    private lateinit var ivGameMode: ImageView
    private lateinit var btZoom: ImageView
    private var startDragDistance: Int = 0
    private val location = IntArray(2)
    private var isZoom: Boolean = true
    private var isShowHind: Boolean = true
    private var isOn = false
    private var isPlayGameModeStop = false
    private var countNumberOfCycles = 0
    private var timeIntervalReplay: Long = 0
    private lateinit var configOneAutoClickModel: ConfigOneAutoClickModel
    private lateinit var modelAddView: ModelAddView
    private var timerStopActionCyclesAll: Timer? = null
    var isCreated: Boolean = false
    private var isLongClick: Boolean = false
    private val sizeSteps = listOf(40f, 45f, 50f)
    private val textSizeSp = listOf(22f, 24f, 26f)

    fun createView(id: Long) {
        isOn = false
        isShowHind = true
        isZoom = true
        val dataListConfigSave = Utils.getItemDataList()
        configOneAutoClickModel = if (dataListConfigSave.isNotEmpty()) {
            dataListConfigSave.find { element -> element.configOne?.id == id }?.configOne
                ?: ConfigOneAutoClickModel(
                    nameConfig = "Config ${dataListConfigSave.count() + 1}",
                    modelAddView = ModelAddView()
                )
        } else {
            ConfigOneAutoClickModel(
                nameConfig = "Config ${dataListConfigSave.count() + 1}",
                modelAddView = ModelAddView()
            )
        }
        index = dataListConfigSave.indexOfFirst { element -> element.configOne?.id == id }
        viewController = this.layoutInflater!!.inflate(
            if (PreferenceHelper.getBoolean(Contact.DISPLAY_MENU_HORIZONTAL, false)) {
                when (PreferenceHelper.getInt(Contact.MENU_SIZE, 1)) {
                    0 -> R.layout.widget_horizontal_small
                    1 -> R.layout.widget_horizontal_medium
                    else -> R.layout.widget_horizontal_large
                }
            } else {
                when (PreferenceHelper.getInt(Contact.MENU_SIZE, 1)) {
                    0 -> R.layout.widget_vertical_small
                    1 -> R.layout.widget_vertical_medium
                    else -> R.layout.widget_vertical_large
                }
            }, null
        )
        startDragDistance = context.dp2px(10f)
        val params = createOverlayLayoutParams().apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            x = context.dp2px(20f)
            windowAnimations = 0
        }
        setupViewController(params)  // Khởi tạo layout của view controller
        setupButtons(params)         // Khởi tạo các nút chức năng
        setupInitialClick()    // Khởi tạo click mặc định ban đầu
        isCreated = true
    }

    private fun setupViewController(params: WindowManager.LayoutParams) {
        manager.addView(viewController, params)

        viewController?.setOnTouchListener(
            TouchAndDragListener(
                params,
                startDragDistance,
                { },
                { manager.updateViewLayout(viewController, params) })
        )
    }

    private fun setupButtons(params: WindowManager.LayoutParams) {
        with(viewController!!) {
            buttonPlayPause = findViewById(R.id.ivPlay)
            btAdd = findViewById(R.id.ivAdd)
            btSwiping = findViewById(R.id.ivSwiping)
            btRemove = findViewById(R.id.ivRemove)
            btSave = findViewById(R.id.ivSave)
            btViewApp = findViewById(R.id.ivBackApp)
            btShowHind = findViewById(R.id.ivShowHind)
            btSettings = findViewById(R.id.ivSetting)
            btClose = findViewById(R.id.ivClose)
            ivMin = findViewById(R.id.ivMin)
            ivGameMode = findViewById(R.id.ivGameMode)
            btZoom = findViewById(R.id.ivZoom)
            btAdd.visibility = View.GONE
            btSwiping.visibility = View.GONE
            btRemove.visibility = View.GONE
            ivMin.visibility = if (PreferenceHelper.getBoolean(
                    Contact.DISPLAY_MINI_MENU, false
                )
            ) View.VISIBLE else View.GONE
            ivGameMode.visibility = if (PreferenceHelper.getBoolean(
                    Contact.MODE_GAME, false
                )
            ) View.VISIBLE else View.GONE
            btZoom.visibility = if (PreferenceHelper.getBoolean(
                    Contact.DISPLAY_MENU_HORIZONTAL, false
                )
            ) View.GONE else View.VISIBLE
            btClose.visibility = if (PreferenceHelper.getBoolean(
                    Contact.DISPLAY_MENU_HORIZONTAL, false
                )
            ) View.VISIBLE else View.GONE
        }
        setupButtonListeners(params)
    }

    private fun setupButtonListeners(params: WindowManager.LayoutParams) {
        buttonPlayPause.setOnTouchListener(getPlayPauseTouchListener(params))
        btSave.setOnTouchListener(getSaveTouchListener(params))
        btViewApp.setOnTouchListener(getBackAppListener(params))
        btSettings.setOnTouchListener(getSettingsTouchListener(params))
        btShowHind.setOnTouchListener(getShowHindTouchListener(params))
        ivMin.setOnTouchListener(getMiniMenu(params))
        ivGameMode.setOnTouchListener(getGameModTouchListener(params))
        btZoom.setOnTouchListener(getZoomTouchListener(params))
        btClose.setOnTouchListener(getCloseTouchListener(params))
    }

    private fun createOverlayLayoutParams(
        width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    ): WindowManager.LayoutParams {
        val overlayParam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            width,
            height,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun setupInitialClick() {
        addView()
        manager.addView(modelAddView.view, modelAddView.paramLayout)
        modelAddView.isAdded = true
    }

    private fun getPlayPauseTouchListener(params: WindowManager.LayoutParams) =
        TouchAndDragListener(
            params,
            startDragDistance,
            { viewOnClick() },
            { manager.updateViewLayout(viewController, params) },
            isButtonStart = true
        )

    private fun getSaveTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { save() },
        { manager.updateViewLayout(viewController, params) })

    private fun getBackAppListener(params: WindowManager.LayoutParams) =
        TouchAndDragListener(
            params,
            startDragDistance,
            { backToApp() },
            { manager.updateViewLayout(viewController, params) })

    private fun getSettingsTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { showPopupSettings() },
        { manager.updateViewLayout(viewController, params) })

    private fun getShowHindTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { showHind() },
        { manager.updateViewLayout(viewController, params) })

    private fun getZoomTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { zoomAction() },
        { manager.updateViewLayout(viewController, params) })

    private fun getMiniMenu(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { miniMenuAction(params) },
        { manager.updateViewLayout(viewController, params) })

    private fun getGameModTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { gameModeAction(params) },
        { manager.updateViewLayout(viewController, params) })

    private fun getCloseTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params,
        startDragDistance,
        { removeView() },
        { manager.updateViewLayout(viewController, params) })

    private fun addView() {
        val targetSize = PreferenceHelper.getInt(Contact.TARGET_SIZE, 1)
        val targetTransparency = PreferenceHelper.getFloat(Contact.TARGET_TRANSPARENCY, 1f)
        val viewSizeOffset = context.dp2px(sizeSteps.getOrElse(targetSize) { 45f })
        val (centerX, centerY) = context.centerOfScreen(20f)
        val params = createOverlayLayoutParams(viewSizeOffset, viewSizeOffset).apply {
            gravity = Gravity.TOP or Gravity.START
            x = configOneAutoClickModel.modelAddView.locationPoints?.x ?: centerX
            y = configOneAutoClickModel.modelAddView.locationPoints?.y ?: centerY
        }
        val viewClick = this.layoutInflater!!.inflate(R.layout.view_click, null)
        modelAddView = ModelAddView(viewClick, params)
        setPanelTouchListener(viewClick, params)
        with(viewClick.findViewById<TextView>(R.id.button)) {
            textSize = textSizeSp.getOrElse(targetSize) { 24f }
            alpha = targetTransparency
            text = "1"
        }
    }

    private fun setPanelTouchListener(panel: View, params: WindowManager.LayoutParams) {
        panel.setOnTouchListener(
            TouchAndDragListener(params, startDragDistance, onTouch = {}, onDrag = {
                manager.updateViewLayout(panel, params)
            })
        )
    }

    fun removeView() {
        if (viewController != null) {
            manager.removeView(viewController)
            viewController = null
        }
        if (viewGameModeController != null) {
            manager.removeView(viewGameModeController)
            viewGameModeController = null
        }
        if (viewMiniMenuController != null) {
            manager.removeView(viewMiniMenuController)
            viewMiniMenuController = null
        }
        stopClickingActions().also { isOn = !isOn }
        manager.removeView(modelAddView.view)
        isCreated = false
        autoClickService?.isViewCreated = false
        val intent = Intent()
        intent.setAction("AUTO_CLICK_CHANGE")
        context.sendBroadcast(intent)
    }

    private fun zoomAction() {
        btSave.visibility = if (isZoom) View.GONE else View.VISIBLE
        btViewApp.visibility = if (isZoom) View.GONE else View.VISIBLE
        btSettings.visibility = if (isZoom) View.GONE else View.VISIBLE
        btClose.visibility = if (isZoom) View.VISIBLE else View.GONE

        if (PreferenceHelper.getBoolean(Contact.DISPLAY_MINI_MENU, false)) {
            ivMin.visibility = if (isZoom) View.GONE else View.VISIBLE

        } else {
            ivMin.visibility = View.GONE
        }

        if (PreferenceHelper.getBoolean(Contact.MODE_GAME, false)) {
            ivGameMode.visibility = if (isZoom) View.GONE else View.VISIBLE

        } else {
            ivGameMode.visibility = View.GONE
        }
        isZoom = !isZoom
    }

    private fun miniMenuAction(params: WindowManager.LayoutParams) {
        viewController!!.visibility = View.GONE
        modelAddView.view!!.visibility = View.GONE
        if (viewMiniMenuController == null) {
            viewMiniMenuController =
                this.layoutInflater!!.inflate(R.layout.floating_min_layout, null)
            startDragDistance = context.dp2px(10f)
            val paramsMiniMenu =
                createOverlayLayoutParams(context.dp2px(20f), context.dp2px(50f)).apply {
                    gravity = Gravity.CENTER or Gravity.START
                    windowAnimations = 0
                }
            manager.addView(viewMiniMenuController, paramsMiniMenu)
        } else {
            viewMiniMenuController!!.visibility = View.VISIBLE
        }
        viewMiniMenuController!!.setOnTouchListener(
            TouchAndDragListener(
                params, startDragDistance, onTouch = {
                    viewController!!.visibility = View.VISIBLE
                    modelAddView.view!!.visibility = View.VISIBLE
                    viewMiniMenuController!!.visibility = View.GONE
                }, onDrag = null
            )
        )
    }

    private var longClickJob: Job? = null
    private fun gameModeAction(params: WindowManager.LayoutParams) {
        viewController!!.visibility = View.GONE
        modelAddView.view!!.visibility = View.GONE
        val paramsGameMode: WindowManager.LayoutParams
        if (viewGameModeController == null) {
            viewGameModeController =
                this.layoutInflater!!.inflate(R.layout.floating_game_mode_layout, null)
            startDragDistance = context.dp2px(10f)
            paramsGameMode =
                createOverlayLayoutParams(context.dp2px(65f), context.dp2px(65f)).apply {
                    gravity = Gravity.CENTER
                    windowAnimations = 0
                }
            manager.addView(viewGameModeController, paramsGameMode)
        } else {
            paramsGameMode = (viewGameModeController!!.layoutParams as WindowManager.LayoutParams)
            viewGameModeController!!.visibility = View.VISIBLE
        }
        var initialX = 0
        var initialY = 0
        var initialTouchX: Float = 0.toFloat()
        var initialTouchY: Float = 0.toFloat()
        var isDrag = false
        fun isDragging(event: MotionEvent): Boolean = (((event.rawX - initialTouchX).toDouble()
            .pow(2.0) + (event.rawY - initialTouchY).toDouble()
            .pow(2.0)) > startDragDistance * startDragDistance)

        viewGameModeController?.setOnTouchListener { v, event ->
            fun longClickHandler() {
                isLongClick = true
                val popupGameModeTargetEdit = PopupGameModeTargetEdit(context)
                popupGameModeTargetEdit.onSwitchModeAction = {
                    viewController!!.visibility = View.VISIBLE
                    modelAddView.view!!.visibility = View.VISIBLE
                    viewGameModeController!!.visibility = View.GONE
                }
                popupGameModeTargetEdit.onCloseAction = {
                    removeView()
                }
                popupGameModeTargetEdit.showPopup()
                v.performLongClick()
            }
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isDrag = false
                    initialX = paramsGameMode.x
                    initialY = paramsGameMode.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    if (isOn) {
                        isPlayGameModeStop = true
                        viewOnClick()
                    }
                    longClickJob?.cancel()
                    longClickJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(ViewConfiguration.getLongPressTimeout().toLong())
                        longClickHandler()
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val d = sqrt(
                        (event.rawX - initialTouchX).toDouble()
                            .pow(2.0) + (event.rawY - initialTouchY).toDouble().pow(2.0)
                    )
                    Log.d("PHT", "d=$d")
                    Log.d("PHT", "4dp=${context.dp2px(4f)}")
                    if (d >= context.dp2px(10f)) {
                        longClickJob?.cancel()
                        if (!isDrag && isDragging(event)) {
                            isDrag = true
                        }
                        if (!isDrag) true
                        paramsGameMode.x = initialX + (event.rawX - initialTouchX).toInt()
                        paramsGameMode.y = initialY + (event.rawY - initialTouchY).toInt()
                        manager.updateViewLayout(viewGameModeController, paramsGameMode)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    longClickJob?.cancel()
                    if (!isDrag && !isOn && !isPlayGameModeStop && !isLongClick) {
                        viewOnClick()
                        v.performClick()
                    }
                    isPlayGameModeStop = false
                    isLongClick = false
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    longClickJob?.cancel()
                }
            }
            false
        }
    }

    private fun save() {
        modelAddView.locationPoints =
            LocationPointsModel(modelAddView.paramLayout!!.x, modelAddView.paramLayout!!.y)
        val popupSaveConfig =
            PopupSaveConfig(context, configOneAutoClickModel = configOneAutoClickModel)
        popupSaveConfig.onDoneAction = { nameConfig: String ->
            configOneAutoClickModel.nameConfig = nameConfig
            configOneAutoClickModel.modelAddView = ModelAddView(
                locationPoints = modelAddView.locationPoints, isAdded = modelAddView.isAdded
            )
            val dataListConfigSave = Utils.getItemDataList()
            dataListConfigSave.removeIf { element -> (element.configOne != null && element.configOne!!.id == configOneAutoClickModel.id) }
            if (index == -1) {
                dataListConfigSave.add(ConfigItem(configOneAutoClickModel))
            } else {
                dataListConfigSave.add(index, ConfigItem(configOneAutoClickModel))
            }
            val json = gson.toJson(dataListConfigSave)
            PreferenceHelper.putString(Contact.LIST_CONFIG_SAVE, json)
            val intent = Intent()
            intent.setAction("CHANGE_DATA_CONFIG")
            context.sendBroadcast(intent)
            Toast.makeText(
                context, context.getString(R.string.text_config_save_success), Toast.LENGTH_SHORT
            ).show()
        }
        popupSaveConfig.showPopup()
    }

    private fun backToApp() {
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(activityIntent)
    }

    private fun showHind() {
        btShowHind.setImageResource(if (isShowHind) R.drawable.icon_menu_yincang else R.drawable.icon_menu_xianshi)
        modelAddView.view!!.visibility = if (isShowHind) View.GONE else View.VISIBLE
        manager.updateViewLayout(modelAddView.view, modelAddView.paramLayout)
        isShowHind = !isShowHind
    }

    private fun showPopupSettings() {
        val popupSettings =
            PopupSettings(context, configOneAutoClickModel = configOneAutoClickModel)
        popupSettings.onDoneAction =
            { nameConfig: String, timeDelay: Int, optionDelay: Int, optionSelected: Int, hours: Int, minutes: Int, seconds: Int, numberOfCycler: Int, antiDetect: Boolean ->
                configOneAutoClickModel.nameConfig = nameConfig
                configOneAutoClickModel.optionDelay = optionDelay
                configOneAutoClickModel.timerDelay = timeDelay
                configOneAutoClickModel.optionSelected = optionSelected
                configOneAutoClickModel.hours = hours
                configOneAutoClickModel.minutes = minutes
                configOneAutoClickModel.seconds = seconds
                configOneAutoClickModel.numberOfCycles = numberOfCycler
                configOneAutoClickModel.antiDetection = antiDetect
            }
        popupSettings.showPopup()
    }

    private fun enableButton(isEnable: Boolean) {
        btSave.isEnabled = isEnable
        btSave.alpha = if (isEnable) 1.0f else 0.5f
        btViewApp.isEnabled = isEnable
        btViewApp.alpha = if (isEnable) 1.0f else 0.5f
        btShowHind.isEnabled = isEnable
        btShowHind.alpha = if (isEnable) 1.0f else 0.5f
        btSettings.isEnabled = isEnable
        btSettings.alpha = if (isEnable) 1.0f else 0.5f
        btZoom.isEnabled = isEnable
        btZoom.alpha = if (isEnable) 1.0f else 0.5f
        btClose.isEnabled = isEnable
        btClose.alpha = if (isEnable) 1.0f else 0.5f
        ivMin.isEnabled = isEnable
        ivMin.alpha = if (isEnable) 1.0f else 0.5f
        ivGameMode.isEnabled = isEnable
        ivGameMode.alpha = if (isEnable) 1.0f else 0.5f
    }

    private fun visibleButton(isVisible: Boolean) {
        if (!isZoom) {
            btSave.visibility = View.GONE
            btViewApp.visibility = View.GONE
            btShowHind.visibility = if (isVisible) View.VISIBLE else View.GONE
            btSettings.visibility = View.GONE
            ivMin.visibility = View.GONE
            ivGameMode.visibility = View.GONE
            if (PreferenceHelper.getBoolean(Contact.DISPLAY_MENU_HORIZONTAL, false)) {
                btZoom.visibility = View.GONE
            } else {
                btZoom.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
            btClose.visibility = if (isVisible) View.VISIBLE else View.GONE
        } else {
            btSave.visibility = if (isVisible) View.VISIBLE else View.GONE
            btViewApp.visibility = if (isVisible) View.VISIBLE else View.GONE
            btShowHind.visibility = if (isVisible) View.VISIBLE else View.GONE
            btSettings.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (PreferenceHelper.getBoolean(Contact.DISPLAY_MENU_HORIZONTAL, false)) {
                btZoom.visibility = View.GONE
                btClose.visibility = if (isVisible) View.VISIBLE else View.GONE
            } else {
                btZoom.visibility = if (isVisible) View.VISIBLE else View.GONE
                btClose.visibility = View.GONE
            }
            if (PreferenceHelper.getBoolean(Contact.DISPLAY_MINI_MENU, false)) {
                ivMin.visibility = if (isVisible) View.VISIBLE else View.GONE
            } else {
                ivMin.visibility = View.GONE
            }
            if (PreferenceHelper.getBoolean(Contact.MODE_GAME, false)) {
                ivGameMode.visibility = if (isVisible) View.VISIBLE else View.GONE
            } else {
                ivGameMode.visibility = View.GONE
            }
        }
    }

    private fun viewOnClick() {
        if (isOn) {
            stopClickingActions().also { isOn = !isOn }
            enableButton(true)
            visibleButton(true)
        } else {
            startClickingActions().also { isOn = !isOn }
            enableButton(false)
            if (PreferenceHelper.getBoolean(Contact.MENU_FOLDABLE, false)) {
                visibleButton(false)
            }
        }
    }

    fun stopClickingActions() {
        timerStopActionCyclesAll?.cancel()
        timerStopActionCyclesAll = null
        Utils.runOnUiThread {
            buttonPlayPause.setImageResource(R.drawable.icon_menu_play_pause)
            viewGameModeController?.findViewById<ImageView>(R.id.ivPlay)
                ?.setImageResource(R.drawable.icon_menu_play_pause)
            if (modelAddView.isAdded!!) {
                modelAddView.paramLayout!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                manager.updateViewLayout(modelAddView.view, modelAddView.paramLayout)
            }
            modelAddView.timer?.cancel()
        }
        countNumberOfCycles = 0
        timeIntervalReplay = 0
    }

    private fun startClickingActions() {
        buttonPlayPause.setImageResource(R.drawable.icon_pause)
        viewGameModeController?.findViewById<ImageView>(R.id.ivPlay)
            ?.setImageResource(R.drawable.icon_pause)
        timeIntervalReplay = 0
        if (modelAddView.isAdded!!) {
            modelAddView.paramLayout!!.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            manager.updateViewLayout(modelAddView.view, modelAddView.paramLayout)
            modelAddView.locationPoints = getLocationPoints(modelAddView.view!!)
            timeIntervalReplay = configOneAutoClickModel.getTotalTimeIntervalClickOfNext()

        }
        val duration = if (configOneAutoClickModel.antiDetection) PreferenceHelper.getInt(
            Contact.SETTINGS_ANTI_DETECT_TIME, 0
        ) else 0
        val delay = 200L
        modelAddView.timer =
            fixedRateTimer(initialDelay = delay, period = timeIntervalReplay + duration) {
                startPerformClick()
            }

        if (configOneAutoClickModel.optionSelected == 1) {
            scheduleTimedAction()
        }
    }

    // Hàm để bắt đầu chuỗi performClick
    private fun startPerformClick() {
        countNumberOfCycles += 1
        if (configOneAutoClickModel.optionSelected == 2 && countNumberOfCycles > configOneAutoClickModel.numberOfCycles) {
            stopClickingActions().also { isOn = !isOn }
            enableButton(true)
            visibleButton(true)
        } else {
            autoClickService?.click(
                modelAddView.locationPoints!!, configOneAutoClickModel.antiDetection
            )
        }
    }

    private fun scheduleTimedAction() {
        val timeInterval = configOneAutoClickModel.getTimeInterval()
        timerStopActionCyclesAll = Timer().apply {
            schedule(timeInterval) {
                stopClickingActions().also { isOn = !isOn }
                enableButton(true)
                visibleButton(true)
            }
        }
    }

    private fun ConfigOneAutoClickModel.getTotalTimeIntervalClickOfNext(): Long {
        val timeUnit = when (optionDelay) {
            0 -> timerDelay
            1 -> timerDelay * 1000
            2 -> timerDelay * 1000 * 60
            else -> 0
        }
        return timeUnit.toLong()
    }

    private fun ConfigOneAutoClickModel.getTimeInterval(): Long {
        return ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000)).toLong()
    }

    private fun getLocationPoints(view: View): LocationPointsModel {
        view.getLocationOnScreen(location)
        val x = location[0] + context.dp2px(20f)
        val y = location[1] + context.dp2px(20f)
        return LocationPointsModel(x, y)
    }
}