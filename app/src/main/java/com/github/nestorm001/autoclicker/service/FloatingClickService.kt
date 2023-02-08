package com.github.nestorm001.autoclicker.service

import android.accessibilityservice.GestureDescription
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.github.nestorm001.autoclicker.R
import com.github.nestorm001.autoclicker.TouchAndDragListener
import com.github.nestorm001.autoclicker.dp2px
import com.github.nestorm001.autoclicker.logd
import com.github.nestorm001.autoclicker.model.Action
import kotlinx.coroutines.*
import java.io.File
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.fixedRateTimer


/**
 * Created on 2018/9/28.
 * By nesto
 */
class FloatingClickService : Service() {
    private lateinit var manager: WindowManager
    private lateinit var view: View
    private lateinit var view1: View
    private lateinit var view2: View
    private lateinit var view3: View

    private lateinit var params: WindowManager.LayoutParams
    private lateinit var params1: WindowManager.LayoutParams
    private lateinit var params2: WindowManager.LayoutParams
    private lateinit var params3: WindowManager.LayoutParams

    private var xForRecord = 0
    private var yForRecord = 0
    private val location = IntArray(2)
    private val location2 = IntArray(2)
    private val location3 = IntArray(2)
    private var startDragDistance: Int = 0
    private var timer: Timer? = null

    /** Coroutine scope for the image processing. */
    private var processingScope: CoroutineScope? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        processingScope = CoroutineScope(Dispatchers.IO)
        startDragDistance = dp2px(10f)
        view = LayoutInflater.from(this).inflate(R.layout.widget, null)
        view1 = LayoutInflater.from(this).inflate(R.layout.widget, null)
        view2 = LayoutInflater.from(this).inflate(R.layout.widget, null)
        view3 = LayoutInflater.from(this).inflate(R.layout.widget, null)

        (view1 as TextView).text = "1"
        (view2 as TextView).text = "2"
        (view3 as TextView).text = "3"

        //setting the layout parameters
        val overlayParam =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params1 = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params2 = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params3 = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )


        //getting windows services and adding the floating view to it
        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.addView(view3, params3)
        manager.addView(view2, params2)
        manager.addView(view1, params1)
        manager.addView(view, params)

        //adding an touchlistener to make drag movement of the floating widget
        view.setOnTouchListener(
            TouchAndDragListener(params, startDragDistance,
                { viewOnClick() },
                { manager.updateViewLayout(view, params) })
        )

        //adding an touchlistener to make drag movement of the floating widget
        view1.setOnTouchListener(
            TouchAndDragListener(params1, startDragDistance,
                { },
                { manager.updateViewLayout(view1, params1) })
        )

        //adding an touchlistener to make drag movement of the floating widget
        view2.setOnTouchListener(
            TouchAndDragListener(params2, startDragDistance,
                { },
                { manager.updateViewLayout(view2, params2) })
        )

        //adding an touchlistener to make drag movement of the floating widget
        view3.setOnTouchListener(
            TouchAndDragListener(params3, startDragDistance,
                { },
                { manager.updateViewLayout(view3, params3) })
        )
    }

    private var isOn = false
    private fun viewOnClick() {
        if (isOn) {
            timer?.cancel()
        } else {
            timer = fixedRateTimer(
                initialDelay = 0,
                period = 200
            ) {
                view1.getLocationOnScreen(location)
                view2.getLocationOnScreen(location2)
                view3.getLocationOnScreen(location3)

                val actions = listOf(
                    mapToAction(location, view1, 1),
                    mapToAction(location2, view2, 2),
                    mapToAction(location3, view3, 3)
                )

                processingScope?.launch {
                    actions.forEach {
                        executeClick(it)
                    }
                }

                autoClickService?.checkText()


//                autoClickService?.multipleClick(listItemClick)

//                autoClickService?.click(
//                    location[0] + view1.right + 10,
//                    location[1] + view1.bottom + 10, "view 1"
//                )
//                autoClickService?.click(location2[0] + view2.right + 10,
//                    location2[1] + view2.bottom + 10, "view 2")
            }
        }
        isOn = !isOn

        (view as TextView).text = if (isOn) "ON" else "OFF"
    }

    /**
     * Execute the provided click.
     * @param click the click to be executed.
     */
    private suspend fun executeClick(click: Action.Click) {
        val clickPath = Path()
        val clickBuilder = GestureDescription.Builder()

//        if (click.clickOnCondition) {
//            conditionPosition?.let { conditionCenter ->
//                clickPath.moveTo(conditionCenter.x, conditionCenter.y)
//            } ?: run {
//                Log.w("Testing", "Can't click on position, there is no condition position")
//                return
//            }
//        } else {
        clickPath.moveTo(click.x!!, click.y!!)
//        getPixelColor(click.x!!, click.y!!)
        getPixelColor2(click.view)
//        }
        clickBuilder.addStroke(
            GestureDescription.StrokeDescription(
                clickPath,
                10,
                10
            )
        )

        withContext(Dispatchers.Main) {
            autoClickService?.executeGesture(clickBuilder.build())
        }
    }

    private fun getPixelColor(xcoord: Int, ycoord: Int) {
        try {
            val sh = Runtime.getRuntime().exec("su", null, null)
            val os: OutputStream = sh.outputStream
            os.write(
                ("/system/bin/screencap -p " + "/sdcard/colorPickerTemp.png").toByteArray(
                    charset("ASCII")
                )
            )
            os.flush()
            os.close()
            sh.waitFor()
//            val screen =
//                BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + File.separator.toString() + "colorPickerTemp.png")
//            val pixel = screen.getPixel(xcoord, ycoord + 10)
//            Log.d(
//                "pixel color",
//                "Pixel Color: + " + Integer.toHexString(pixel) + " at x:" + xcoord + " y:" + ycoord
//            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Path.moveTo(x: Int, y: Int) {
        moveTo(x.toFloat(), y.toFloat())
    }

    private fun getPixelColor2(view: View) {
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val colorCode = bitmap.getPixel(1, 1)
        val pixelColor = Integer.toHexString(colorCode)

        Log.e("Testing", "Color Code: $pixelColor")
    }

    override fun onDestroy() {
        super.onDestroy()
        "FloatingClickService onDestroy".logd()
        processingScope?.launch {
            timer?.cancel()
            manager.removeView(view)

            processingScope?.cancel()
            processingScope = null
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        "FloatingClickService onConfigurationChanged".logd()
        val x = params.x
        val y = params.y
        params.x = xForRecord
        params.y = yForRecord
        xForRecord = x
        yForRecord = y
        manager.updateViewLayout(view, params)
    }

    private fun mapToAction(location: IntArray, view: View, index: Long): Action.Click {
        return Action.Click(
            id = index,
            eventId = index,
            name = "View name $index",
            pressDuration = 10,
            x = location[0].plus(view.right.plus(10)),
            y = location[1].plus(view.bottom.plus(10)),
            clickOnCondition = false,
            view = view
        )
    }
}