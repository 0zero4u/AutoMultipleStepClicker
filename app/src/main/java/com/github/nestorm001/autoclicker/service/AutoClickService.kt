package com.github.nestorm001.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.github.nestorm001.autoclicker.MainActivity
import com.github.nestorm001.autoclicker.bean.Event
import com.github.nestorm001.autoclicker.logd
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created on 2018/9/28.
 * By nesto
 */

var autoClickService: AutoClickService? = null

class AutoClickService : AccessibilityService() {

    internal val events = mutableListOf<Event>()

    var source: AccessibilityNodeInfo? = null

    override fun onInterrupt() {
        // NO-OP
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        event.source?.let {
            source = it
            nodeInfo(it)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        "onServiceConnected".logd()
        autoClickService = this

//        val serviceInfo = AccessibilityServiceInfo()
//        serviceInfo.packageNames = arrayOf(applicationContext.packageName as String)
//        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
//        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
//        serviceInfo.notificationTimeout = 100
//        serviceInfo.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
//        setServiceInfo(serviceInfo)

        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun multipleClick(list: List<Triple<Int, Int, String>>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val x = list[0].first
        val y = list[0].second
        val from = list[0].third

        val x2 = list[1].first
        val y2 = list[1].second
        val from2 = list[1].third

        "multiple click $from : $x $y".logd()
        "multiple click $from2 : $x2 $y2".logd()

        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()

        val gestureStrokeDescriptions = GestureDescription.StrokeDescription(path, 10, 10).apply {
            list.forEachIndexed { index, triple ->
                if (index > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        continueStroke(Path().apply { moveTo(triple.first.toFloat(), triple.second.toFloat()) }, 10, 10, true)
                    }
                }
            }
        }

        val gestureDescription = builder
            .addStroke(gestureStrokeDescriptions)
            .build()
        dispatchGesture(gestureDescription, null, null)
    }

    fun click(x: Int, y: Int, from: String) {
        "click $from : $x $y".logd()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 10, 10))
            .build()
        dispatchGesture(gestureDescription, null, null)
    }

    fun run(newEvents: MutableList<Event>) {
        events.clear()
        events.addAll(newEvents)
        events.toString().logd()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val builder = GestureDescription.Builder()
        events.forEach { builder.addStroke(it.onEvent()) }
        dispatchGesture(builder.build(), null, null)
    }

    suspend fun executeGesture(gestureDescription: GestureDescription) {
        suspendCoroutine<Unit?> { continuation ->
            dispatchGesture(
                gestureDescription,
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) = continuation.resume(null)
                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        Log.w("Testing", "Gesture cancelled: $gestureDescription")
                        continuation.resume(null)
                    }
                },
                null
            )
        }
    }

    fun nodeInfo(nodeInfo: AccessibilityNodeInfo) {
        if (nodeInfo.findAccessibilityNodeInfosByText("Lock").isNullOrEmpty().not() ||
            rootInActiveWindow.findAccessibilityNodeInfosByText("Session").isNullOrEmpty().not()
        ) {
            Log.e("Testing", "Found text in conditions.")
        }

    }

    fun checkText() {
        if (source?.findAccessibilityNodeInfosByText("Session").isNullOrEmpty().not() ||
            rootInActiveWindow.findAccessibilityNodeInfosByText("Session").isNullOrEmpty().not()
        ) {
            Log.e("Testing", "Found text in conditions.")
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        "AutoClickService onUnbind".logd()
        autoClickService = null
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        "AutoClickService onDestroy".logd()
        autoClickService = null
        super.onDestroy()
    }
}