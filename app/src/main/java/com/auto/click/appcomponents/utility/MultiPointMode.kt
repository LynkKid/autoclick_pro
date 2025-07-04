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
import com.auto.click.model.ConfigMultiAutoClickModel
import com.auto.click.model.LocationPointsModel
import com.auto.click.model.ModelAddViewForMultiClick
import com.auto.click.modules.navigation.ui.MainActivity
import com.auto.click.modules.popup.PopupGameModeTargetEdit
import com.auto.click.modules.popup.PopupSaveConfig
import com.auto.click.modules.popup.PopupSettingClick
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

class MultiPointMode(
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
    private lateinit var btGameMode: ImageView
    private lateinit var btZoom: ImageView
    private var startDragDistance: Int = 0
    private val location = IntArray(2)
    private var isZoom: Boolean = true
    private var isShowHind: Boolean = true
    private val listViewClick = ArrayList<ModelAddViewForMultiClick>()
    private lateinit var configMultiAutoClickModel: ConfigMultiAutoClickModel
    private var isOn = false
    private var isPlayGameModeStop = false
    private var timeIntervalReplayCyclesAll: Long = 0
    private var countNumberOfCyclesAll = 0
    private var timerCyclesAll: Timer? = null
    private var clickJob: Job? = null
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
        configMultiAutoClickModel = if (dataListConfigSave.isNotEmpty()) {
            dataListConfigSave.find { element -> element.configMulti?.id == id }?.configMulti
                ?: ConfigMultiAutoClickModel(
                    nameConfig = "Config ${dataListConfigSave.count() + 1}",
                    listClick = arrayListOf()
                )
        } else {
            ConfigMultiAutoClickModel(
                nameConfig = "Config ${dataListConfigSave.count() + 1}",
                listClick = arrayListOf()
            )
        }
        index = dataListConfigSave.indexOfFirst { element -> element.configOne?.id == id }
        viewController = layoutInflater?.inflate(
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
            btZoom = findViewById(R.id.ivZoom)
            ivMin = findViewById(R.id.ivMin)
            btGameMode = findViewById(R.id.ivGameMode)
            ivMin.visibility = if (PreferenceHelper.getBoolean(
                    Contact.DISPLAY_MINI_MENU,
                    false
                )
            ) View.VISIBLE else View.GONE
            btGameMode.visibility = if (PreferenceHelper.getBoolean(
                    Contact.MODE_GAME,
                    false
                )
            ) View.VISIBLE else View.GONE
            btZoom.visibility = if (PreferenceHelper.getBoolean(
                    Contact.DISPLAY_MENU_HORIZONTAL,
                    false
                )
            ) View.GONE else View.VISIBLE
            btClose.visibility = if (PreferenceHelper.getBoolean(
                    Contact.DISPLAY_MENU_HORIZONTAL,
                    false
                )
            ) View.VISIBLE else View.GONE
        }
        setupButtonListeners(params)
    }

    private fun setupButtonListeners(params: WindowManager.LayoutParams) {
        buttonPlayPause.setOnTouchListener(getPlayPauseTouchListener(params))
        btAdd.setOnTouchListener(getAddTouchListener(params))
        btSwiping.setOnTouchListener(getSwipingTouchListener(params))
        btRemove.setOnTouchListener(getRemoveTouchListener(params))
        btSave.setOnTouchListener(getSaveTouchListener(params))
        btViewApp.setOnTouchListener(getBackAppListener(params))
        btSettings.setOnTouchListener(getSettingsTouchListener(params))
        btShowHind.setOnTouchListener(getShowHindTouchListener(params))
        ivMin.setOnTouchListener(getMiniMenu(params))
        btGameMode.setOnTouchListener(getGameModeTouchListener(params))
        btZoom.setOnTouchListener(getZoomTouchListener(params))
        btClose.setOnTouchListener(getCloseTouchListener(params))
    }

    private fun createOverlayLayoutParams(
        width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
        height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    ): WindowManager.LayoutParams {
        val overlayParam =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        if (configMultiAutoClickModel.listClick.isEmpty()) {
            addViewClick()
            manager.addView(listViewClick[0].viewStart, listViewClick[0].paramLayoutStart)
            listViewClick[0].isAdded = true
        } else {
            for (i in 0..<configMultiAutoClickModel.listClick.count()) {
                if (configMultiAutoClickModel.listClick[i].isClick) {
                    addViewClick(configMultiAutoClickModel.listClick[i])
                } else {
                    addViewSwiping(configMultiAutoClickModel.listClick[i])
                }
            }
            listViewClick.forEach {
                if (it.viewOrder != null) {
                    manager.addView(it.viewOrder, it.paramLayoutOrder)
                }
                manager.addView(it.viewStart, it.paramLayoutStart)
                it.viewEnd?.let { endView ->
                    manager.addView(endView, it.paramLayoutEnd)
                }
                it.isAdded = true
            }
        }
    }

    private fun getPlayPauseTouchListener(params: WindowManager.LayoutParams) =
        TouchAndDragListener(
            params, startDragDistance,
            { viewOnClick() },
            { manager.updateViewLayout(viewController, params) },
            isButtonStart = true
        )

    private fun getAddTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        {
            addViewClick()
            listViewClick.forEach {
                if (!it.isAdded) {
                    manager.addView(it.viewStart, it.paramLayoutStart)
                    it.isAdded = true
                }
            }
        },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getSwipingTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        {
            addViewSwiping()
            listViewClick.forEach {
                if (!it.isAdded) {
                    if (it.viewOrder != null) {
                        manager.addView(it.viewOrder, it.paramLayoutOrder)
                    }
                    manager.addView(it.viewStart, it.paramLayoutStart)
                    it.viewEnd?.let { endView ->
                        manager.addView(endView, it.paramLayoutEnd)
                    }
                    it.isAdded = true
                }
            }
        },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getRemoveTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { removeViewClick() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getSaveTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { save() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getBackAppListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { backToApp() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getSettingsTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { showPopupSettings() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getShowHindTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { showHind() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getMiniMenu(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { miniMenuAction(params) },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getGameModeTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { gameModeAction(params) },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getZoomTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { zoomAction() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun getCloseTouchListener(params: WindowManager.LayoutParams) = TouchAndDragListener(
        params, startDragDistance,
        { removeView() },
        { manager.updateViewLayout(viewController, params) }
    )

    private fun addViewClick(modelAddViewForMultiClick: ModelAddViewForMultiClick? = null) {
        val idView: Int? = modelAddViewForMultiClick?.id
        val xLocation: Int? = modelAddViewForMultiClick?.locationPointsModelStart?.x
        val yLocation: Int? = modelAddViewForMultiClick?.locationPointsModelStart?.y
        val targetSize = PreferenceHelper.getInt(Contact.TARGET_SIZE, 1)
        val targetTransparency = PreferenceHelper.getFloat(Contact.TARGET_TRANSPARENCY, 1f)
        val viewSizeOffset = context.dp2px(sizeSteps.getOrElse(targetSize) { 45f })
        val (centerX, centerY) = context.centerOfScreen(20f)
        val params = createOverlayLayoutParams(viewSizeOffset, viewSizeOffset).apply {
            gravity = Gravity.TOP or Gravity.START
            x = xLocation ?: centerX
            y = yLocation ?: centerY
        }
        val viewClick = this.layoutInflater!!.inflate(R.layout.view_click, null)
        val id = idView ?: (listViewClick.count() + 1)
        listViewClick.add(
            ModelAddViewForMultiClick(
                id, viewClick, paramLayoutStart = params,
                optionDelay = modelAddViewForMultiClick?.optionDelay ?: 0,
                timerDelay = modelAddViewForMultiClick?.timerDelay ?: 0,
                optionDelayClickOfNext = modelAddViewForMultiClick?.optionDelayClickOfNext ?: 0,
                timerDelayClickOfNext = modelAddViewForMultiClick?.timerDelayClickOfNext
                    ?: PreferenceHelper.getInt(Contact.TIME_DELAY_DEFAULT, 100),
                numberOfCycles = modelAddViewForMultiClick?.numberOfCycles ?: 1,
                timeOfSwipe = modelAddViewForMultiClick?.timeOfSwipe ?: PreferenceHelper.getInt(
                    Contact.TIME_SWIPE_DEFAULT,
                    300
                ),
            )
        )
        setPanelTouchListener(id, viewClick, params)
        with(viewClick.findViewById<TextView>(R.id.button)) {
            textSize = textSizeSp.getOrElse(targetSize) { 24f }
            alpha = targetTransparency
            text = "${listViewClick.count()}"
        }
    }

    private fun addViewSwiping(
        modelAddViewForMultiClick: ModelAddViewForMultiClick? = null,
    ) {
        val idView: Int? = modelAddViewForMultiClick?.id
        val xLocationStart: Int? = modelAddViewForMultiClick?.locationPointsModelStart?.x
        val yLocationStart: Int? = modelAddViewForMultiClick?.locationPointsModelStart?.y
        val xLocationEnd: Int? = modelAddViewForMultiClick?.locationPointsModelEnd?.x
        val yLocationEnd: Int? = modelAddViewForMultiClick?.locationPointsModelEnd?.y
        val viewSizeOffset = context.dp2px(40f)
        val (centerX, centerY) = context.centerOfScreen(20f)
        val paramsStart = createOverlayLayoutParams(viewSizeOffset, viewSizeOffset).apply {
            gravity = Gravity.TOP or Gravity.START
            x = xLocationStart ?: centerX
            y = yLocationStart ?: centerY
        }
        val paramsEnd = createOverlayLayoutParams(viewSizeOffset, viewSizeOffset).apply {
            gravity = Gravity.TOP or Gravity.START
            x = xLocationEnd ?: paramsStart.x
            y = yLocationEnd ?: (paramsStart.y - context.dp2px(150f))
        }
        val paramsOrder = createOverlayLayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        val orderView = OrderView(context)
        val panelStart = LayoutInflater.from(context).inflate(R.layout.view_click, null)
        val panelEnd = LayoutInflater.from(context).inflate(R.layout.view_click, null)
        panelEnd.findViewById<TextView>(R.id.button).text = ""
        orderView.setLinePosition(
            startX = paramsStart.x + (viewSizeOffset / 2),
            startY = paramsStart.y + (viewSizeOffset / 2),
            endX = paramsEnd.x + (viewSizeOffset / 2),
            endY = paramsEnd.y + (viewSizeOffset / 2)
        )
        val id = idView ?: (listViewClick.count() + 1)
        listViewClick.add(
            ModelAddViewForMultiClick(
                id,
                panelStart,
                panelEnd,
                orderView,
                paramLayoutStart = paramsStart,
                paramLayoutEnd = paramsEnd,
                paramLayoutOrder = paramsOrder,
                isClick = false,
                optionDelay = modelAddViewForMultiClick?.optionDelay ?: 0,
                timerDelay = modelAddViewForMultiClick?.timerDelay ?: 0,
                optionDelayClickOfNext = modelAddViewForMultiClick?.optionDelayClickOfNext ?: 0,
                timerDelayClickOfNext = modelAddViewForMultiClick?.timerDelayClickOfNext
                    ?: PreferenceHelper.getInt(Contact.TIME_DELAY_DEFAULT, 100),
                numberOfCycles = modelAddViewForMultiClick?.numberOfCycles ?: 1,
                timeOfSwipe = modelAddViewForMultiClick?.timeOfSwipe ?: PreferenceHelper.getInt(
                    Contact.TIME_SWIPE_DEFAULT,
                    300
                ),
            )
        )
        panelStart.findViewById<TextView>(R.id.button).text = "${listViewClick.count()}"
        setPanelTouchListener(id, panelStart, paramsStart, orderView, true, (viewSizeOffset / 2))
        setPanelTouchListener(id, panelEnd, paramsEnd, orderView, false, (viewSizeOffset / 2))
    }

    private fun setPanelTouchListener(
        id: Int,
        panel: View,
        params: WindowManager.LayoutParams,
        orderView: OrderView? = null,
        isStart: Boolean? = null,
        offset: Int? = null,
    ) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        panel.setOnTouchListener(
            TouchAndDragListener(
                params, startDragDistance, isSwiping = orderView != null,
                onTouch = {
                    if (!isOn) {
                        showPopupSettingClick(listViewClick.find { it.id == id }!!)
                    }
                },
                onDrag = {
                    if (orderView != null && isStart != null && offset != null) {
                        val x = (params.x + offset).coerceIn(offset, screenWidth - offset)
                        val y = (params.y + offset).coerceIn(offset, screenHeight - offset)
                        orderView.updateCirclePosition(x, y, isCircle1 = isStart)
                    }
                    manager.updateViewLayout(panel, params)
                }
            )
        )
    }

    private fun removeViewClick() {
        if (listViewClick.isNotEmpty()) {
            manager.removeView(listViewClick[listViewClick.count() - 1].viewStart)
            if (listViewClick[listViewClick.count() - 1].viewEnd != null) {
                manager.removeView(listViewClick[listViewClick.count() - 1].viewEnd)
                manager.removeView(listViewClick[listViewClick.count() - 1].viewOrder)
            }
            listViewClick.removeAt(listViewClick.count() - 1)
        }
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
        listViewClick.forEach {
            manager.removeView(it.viewStart)
            if (it.viewEnd != null && it.viewOrder != null) {
                manager.removeView(it.viewEnd)
                manager.removeView(it.viewOrder)
            }
        }
        listViewClick.clear()
        stopClickingActions().also { isOn = !isOn }
        isCreated = false
        autoClickService?.isViewCreated = false
        val intent = Intent()
        intent.setAction("AUTO_CLICK_CHANGE")
        context.sendBroadcast(intent)
    }

    private fun save() {
        listViewClick.forEach { getLocationViewParams(it) }
        val popupSaveConfig =
            PopupSaveConfig(context, configMultiAutoClickModel = configMultiAutoClickModel)
        popupSaveConfig.onDoneAction = { nameConfig: String ->
            configMultiAutoClickModel.nameConfig = nameConfig
            configMultiAutoClickModel.listClick.clear()
            listViewClick.forEach { element ->
                configMultiAutoClickModel.listClick.add(
                    ModelAddViewForMultiClick(
                        id = element.id,
                        locationPointsModelStart = element.locationPointsModelStart,
                        locationPointsModelEnd = element.locationPointsModelEnd,
                        isAdded = element.isAdded,
                        optionDelay = element.optionDelay,
                        timerDelay = element.timerDelay,
                        optionDelayClickOfNext = element.optionDelayClickOfNext,
                        timerDelayClickOfNext = element.timerDelayClickOfNext,
                        numberOfCycles = element.numberOfCycles,
                        timeOfSwipe = element.timeOfSwipe,
                        isClick = element.isClick
                    )
                )
            }
            val dataListConfigSave = Utils.getItemDataList()
            dataListConfigSave.removeIf { element -> (element.configMulti != null && element.configMulti!!.id == configMultiAutoClickModel.id) }
            if (index == -1) {
                dataListConfigSave.add(ConfigItem(configMulti = configMultiAutoClickModel))
            } else {
                dataListConfigSave.add(index, ConfigItem(configMulti = configMultiAutoClickModel))
            }
            val json = gson.toJson(dataListConfigSave)
            PreferenceHelper.putString(Contact.LIST_CONFIG_SAVE, json)
            val intent = Intent()
            intent.setAction("CHANGE_DATA_CONFIG")
            context.sendBroadcast(intent)
            Toast.makeText(
                context,
                context.getString(R.string.text_config_save_success),
                Toast.LENGTH_SHORT
            ).show()
        }
        popupSaveConfig.showPopup()
    }

    private fun backToApp() {
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(activityIntent)
    }

    private fun showPopupSettings() {
        val popupSettings =
            PopupSettings(context, configMultiAutoClickModel = configMultiAutoClickModel)
        popupSettings.onDoneAction =
            { nameConfig: String, _: Int, _: Int, optionSelected: Int, hours: Int, minutes: Int, seconds: Int, numberOfCycler: Int, antiDetect: Boolean ->
                configMultiAutoClickModel.nameConfig = nameConfig
                configMultiAutoClickModel.optionSelected = optionSelected
                configMultiAutoClickModel.hours = hours
                configMultiAutoClickModel.minutes = minutes
                configMultiAutoClickModel.seconds = seconds
                configMultiAutoClickModel.numberOfCycles = numberOfCycler
                configMultiAutoClickModel.antiDetection = antiDetect
            }
        popupSettings.showPopup()
    }

    private fun showPopupSettingClick(element: ModelAddViewForMultiClick) {
        val popupSettingClick = PopupSettingClick(context, element)
        popupSettingClick.onDoneAction =
            { optionDelay, delayTime, optionDelayClickOfNext, delayTimeClickOfNext, repeatNum, swipeTime, _ ->
                element.optionDelay = optionDelay
                element.timerDelay = delayTime.toInt()
                element.optionDelayClickOfNext = optionDelayClickOfNext
                element.timerDelayClickOfNext = delayTimeClickOfNext.toInt()
                element.numberOfCycles = repeatNum.toInt()
                element.timeOfSwipe = swipeTime.toInt()
            }
        popupSettingClick.showPopup()
    }

    private fun zoomAction() {
        btAdd.visibility = if (isZoom) View.GONE else View.VISIBLE
        btSwiping.visibility = if (isZoom) View.GONE else View.VISIBLE
        btRemove.visibility = if (isZoom) View.GONE else View.VISIBLE
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
            btGameMode.visibility = if (isZoom) View.GONE else View.VISIBLE

        } else {
            btGameMode.visibility = View.GONE
        }

        isZoom = !isZoom
    }

    private fun showHind() {
        btShowHind.setImageResource(if (isShowHind) R.drawable.icon_menu_yincang else R.drawable.icon_menu_xianshi)
        listViewClick.forEach { modelAddView ->
            modelAddView.viewStart!!.visibility = if (isShowHind) View.GONE else View.VISIBLE
            modelAddView.viewEnd?.visibility = if (isShowHind) View.GONE else View.VISIBLE
            modelAddView.viewOrder?.visibility = if (isShowHind) View.GONE else View.VISIBLE
        }
        isShowHind = !isShowHind
    }

    private fun miniMenuAction(params: WindowManager.LayoutParams) {
        viewController!!.visibility = View.GONE
        listViewClick.forEach { modelAddView ->
            modelAddView.viewStart!!.visibility = View.GONE
            modelAddView.viewEnd?.visibility = View.GONE
            modelAddView.viewOrder?.visibility = View.GONE
        }
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
                params, startDragDistance,
                onTouch = {
                    viewController!!.visibility = View.VISIBLE
                    listViewClick.forEach { modelAddView ->
                        modelAddView.viewStart!!.visibility = View.VISIBLE
                        modelAddView.viewEnd?.visibility = View.VISIBLE
                        modelAddView.viewOrder?.visibility = View.VISIBLE
                    }
                    viewMiniMenuController!!.visibility = View.GONE
                },
                onDrag = null
            )
        )
    }

    private var longClickJob: Job? = null
    private fun gameModeAction(params: WindowManager.LayoutParams) {
        viewController!!.visibility = View.GONE
        listViewClick.forEach { modelAddView ->
            modelAddView.viewStart!!.visibility = View.GONE
            modelAddView.viewEnd?.visibility = View.GONE
            modelAddView.viewOrder?.visibility = View.GONE
        }
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
        fun isDragging(event: MotionEvent): Boolean =
            (((event.rawX - initialTouchX).toDouble().pow(2.0)
                    + (event.rawY - initialTouchY).toDouble().pow(2.0))
                    > startDragDistance * startDragDistance)

        viewGameModeController?.setOnTouchListener { v, event ->
            fun longClickHandler() {
                isLongClick = true
                val popupGameModeTargetEdit = PopupGameModeTargetEdit(context)
                popupGameModeTargetEdit.onSwitchModeAction = {
                    viewController!!.visibility = View.VISIBLE
                    listViewClick.forEach { modelAddView ->
                        modelAddView.viewStart!!.visibility = View.VISIBLE
                        modelAddView.viewEnd?.visibility = View.VISIBLE
                        modelAddView.viewOrder?.visibility = View.VISIBLE
                    }
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

    private fun enableButton(isEnable: Boolean) {
        btAdd.isEnabled = isEnable
        btAdd.alpha = if (isEnable) 1.0f else 0.5f
        btSwiping.isEnabled = isEnable
        btSwiping.alpha = if (isEnable) 1.0f else 0.5f
        btRemove.isEnabled = isEnable
        btRemove.alpha = if (isEnable) 1.0f else 0.5f
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
        btGameMode.isEnabled = isEnable
        btGameMode.alpha = if (isEnable) 1.0f else 0.5f
    }

    private fun visibleButton(isVisible: Boolean) {
        if (!isZoom) {
            btAdd.visibility = View.GONE
            btSwiping.visibility = View.GONE
            btRemove.visibility = View.GONE
            btSave.visibility = View.GONE
            btViewApp.visibility = View.GONE
            btShowHind.visibility = if (isVisible) View.VISIBLE else View.GONE
            btSettings.visibility = View.GONE
            ivMin.visibility = View.GONE
            btGameMode.visibility = View.GONE
            if (PreferenceHelper.getBoolean(Contact.DISPLAY_MENU_HORIZONTAL, false)) {
                btZoom.visibility = View.GONE
            } else {
                btZoom.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
            btClose.visibility = if (isVisible) View.VISIBLE else View.GONE
        } else {
            btAdd.visibility = if (isVisible) View.VISIBLE else View.GONE
            btSwiping.visibility = if (isVisible) View.VISIBLE else View.GONE
            btRemove.visibility = if (isVisible) View.VISIBLE else View.GONE
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
                btGameMode.visibility = if (isVisible) View.VISIBLE else View.GONE
            } else {
                btGameMode.visibility = View.GONE
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

    private fun startClickingActions() {
        buttonPlayPause.setImageResource(R.drawable.icon_pause)
        viewGameModeController?.findViewById<ImageView>(R.id.ivPlay)
            ?.setImageResource(R.drawable.icon_pause)
        timeIntervalReplayCyclesAll = 0
        listViewClick.forEach { setupViewParams(it) }
        val delay = 200L
        timerCyclesAll =
            fixedRateTimer(initialDelay = delay, period = timeIntervalReplayCyclesAll) {
                startPerformClick()
            }

        if (configMultiAutoClickModel.optionSelected == 1) {
            scheduleTimedAction()
        }
    }

    private fun setupViewParams(modelAddView: ModelAddViewForMultiClick) {
        if (modelAddView.isAdded) {
            modelAddView.paramLayoutStart!!.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            manager.updateViewLayout(modelAddView.viewStart, modelAddView.paramLayoutStart)
            modelAddView.locationPointsModelStart = getLocationPoints(modelAddView.viewStart!!)
            if (modelAddView.viewEnd != null) {
                modelAddView.locationPointsModelEnd = getLocationPoints(modelAddView.viewEnd!!)
            }
        }
        timeIntervalReplayCyclesAll += (modelAddView.getTotalTimeIntervalClickOfNext() * modelAddView.numberOfCycles) + modelAddView.getTotalTimeInterval()
    }

    private fun getLocationViewParams(modelAddView: ModelAddViewForMultiClick) {
        modelAddView.locationPointsModelStart = LocationPointsModel(
            modelAddView.paramLayoutStart!!.x,
            modelAddView.paramLayoutStart!!.y
        )
        modelAddView.locationPointsModelEnd =
            if (modelAddView.paramLayoutEnd != null) LocationPointsModel(
                modelAddView.paramLayoutEnd!!.x,
                modelAddView.paramLayoutEnd!!.y
            ) else null
    }

    private fun getLocationPoints(view: View): LocationPointsModel {
        view.getLocationOnScreen(location)
        val x = location[0] + context.dp2px(20f)
        val y = location[1] + context.dp2px(20f)
        return LocationPointsModel(x, y)
    }

    private fun ModelAddViewForMultiClick.getTotalTimeIntervalClickOfNext(): Long {
        val timeUnit = when (optionDelayClickOfNext) {
            0 -> timerDelayClickOfNext
            1 -> timerDelayClickOfNext * 1000
            2 -> timerDelayClickOfNext * 1000 * 60
            else -> 0
        }
        return timeUnit.toLong()
    }

    private fun ModelAddViewForMultiClick.getTotalTimeInterval(): Long {
        val timeUnit = when (optionDelay) {
            0 -> timerDelay
            1 -> timerDelay * 1000
            2 -> timerDelay * 1000 * 60
            else -> 0
        }
        return timeUnit.toLong()
    }

    private fun ConfigMultiAutoClickModel.getTimeInterval(): Long {
        return ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000)).toLong()
    }

    private fun scheduleTimedAction() {
        val timeInterval = configMultiAutoClickModel.getTimeInterval()
        timerStopActionCyclesAll = Timer().apply {
            schedule(timeInterval) {
                stopClickingActions().also { isOn = !isOn }
                enableButton(true)
                visibleButton(true)
            }
        }
    }

    fun stopClickingActions() {
        cancelPerformClick()
        timerCyclesAll?.cancel()
        timerCyclesAll = null
        timerStopActionCyclesAll?.cancel()
        timerStopActionCyclesAll = null
        Utils.runOnUiThread {
            buttonPlayPause.setImageResource(R.drawable.icon_menu_play_pause)
            viewGameModeController?.findViewById<ImageView>(R.id.ivPlay)
                ?.setImageResource(R.drawable.icon_menu_play_pause)
            listViewClick.forEach { modelAddView ->
                if (modelAddView.isAdded) {
                    modelAddView.paramLayoutStart!!.flags =
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    manager.updateViewLayout(modelAddView.viewStart, modelAddView.paramLayoutStart)
                }
                modelAddView.timer?.cancel()
                modelAddView.timer = null
            }
        }
        countNumberOfCyclesAll = 0
        timeIntervalReplayCyclesAll = 0
    }

    // Hàm để bắt đầu chuỗi performClick
    private fun startPerformClick() {
        clickJob = CoroutineScope(Dispatchers.Main).launch {
            for (index in listViewClick.indices) {
                if (index == 0) {
                    countNumberOfCyclesAll += 1
                }
                if (configMultiAutoClickModel.optionSelected == 2 && countNumberOfCyclesAll > configMultiAutoClickModel.numberOfCycles) {
                    stopClickingActions().also { isOn = !isOn }
                    enableButton(true)
                    visibleButton(true)
                } else {
                    performClick(index)
                    val modelAddView = listViewClick[index]
                    val timeIntervalReplay = modelAddView.getTotalTimeIntervalClickOfNext()
                    val duration =
                        if (configMultiAutoClickModel.antiDetection) PreferenceHelper.getInt(
                            Contact.SETTINGS_ANTI_DETECT_TIME,
                            0
                        ) else 0
                    delay(((timeIntervalReplay + duration) * modelAddView.numberOfCycles) + modelAddView.getTotalTimeInterval())
                }
            }
        }
    }

    // Hàm hủy chuỗi performClick
    private fun cancelPerformClick() {
        clickJob?.cancel() // Hủy Job để dừng chuỗi thực thi
        listViewClick.forEach { it.timer?.cancel() } // Hủy các Timer hiện có trong listViewClick
    }

    private fun performClick(index: Int) {
        if (index < listViewClick.size) {
            val modelAddView = listViewClick[index]
            val timeIntervalReplay: Long = modelAddView.getTotalTimeIntervalClickOfNext()
            var countNumberOfCycles = 0

            synchronized(modelAddView) {
                // Hủy timer cũ nếu còn
                modelAddView.timer?.cancel()
                // Khởi tạo timer mới
                modelAddView.timer = fixedRateTimer(
                    initialDelay = modelAddView.getTotalTimeInterval(),
                    period = timeIntervalReplay
                ) {
                    synchronized(modelAddView) {
                        if (countNumberOfCycles >= modelAddView.numberOfCycles) {
                            modelAddView.timer?.cancel()
                            modelAddView.timer = null
                            countNumberOfCycles = 0
                        } else {
                            timerCyclesAll?.let {
                                modelAddView.apply {
                                    if (viewEnd == null) autoClickService?.click(
                                        locationPointsModelStart!!,
                                        configMultiAutoClickModel.antiDetection
                                    ) else autoClickService?.swiping(
                                        locationPointsModelStart!!,
                                        locationPointsModelEnd!!,
                                        timeOfSwipe.toLong(),
                                        configMultiAutoClickModel.antiDetection
                                    )
                                    countNumberOfCycles += 1
                                }
                            } ?: run {
                                modelAddView.timer?.cancel().also { countNumberOfCycles = 0 }
                            }
                        }
                    }
                }
            }
        }
    }
}
