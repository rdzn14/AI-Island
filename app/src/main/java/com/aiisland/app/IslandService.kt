package com.aiisland.app

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.math.sqrt

private class Owner : LifecycleOwner, SavedStateRegistryOwner {
    private val reg = LifecycleRegistry(this)
    private val ctl = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = reg
    override val savedStateRegistry: SavedStateRegistry get() = ctl.savedStateRegistry

    fun start() {
        ctl.performAttach()
        ctl.performRestore(null)
        reg.currentState = Lifecycle.State.RESUMED
    }

    fun stop() {
        reg.currentState = Lifecycle.State.DESTROYED
    }
}

class IslandService : AccessibilityService(), SensorEventListener {
    companion object {
        @Volatile
        var running = false
    }

    private val phone = mutableStateOf(Phone())
    private val owner = Owner()
    private val handler = Handler(Looper.getMainLooper())
    private var view: ComposeView? = null
    private var shakeAt = 0L

    private val rx = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refresh()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (view != null) return
        running = true
        owner.start()
        refresh()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_TIME_TICK)
        }
        ContextCompat.registerReceiver(this, rx, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        val sm = getSystemService(SensorManager::class.java)
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        showIsland()
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        try {
            unregisterReceiver(rx)
        } catch (e: Exception) {
        }
        getSystemService(SensorManager::class.java).unregisterListener(this)
        view?.let {
            try {
                getSystemService(WindowManager::class.java).removeView(it)
            } catch (e: Exception) {
            }
        }
        view = null
        owner.stop()
        super.onDestroy()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        val v = event?.values ?: return
        val x = v[0]
        val y = v[1]
        val z = v[2]
        val now = SystemClock.elapsedRealtime()
        if (sqrt(x * x + y * y + z * z) / 9.81f > 2.4f && now - shakeAt > 3000) {
            shakeAt = now
            phone.value = phone.value.copy(dizzy = true)
            handler.postDelayed({ phone.value = phone.value.copy(dizzy = false) }, 2200)
        }
        val p = phone.value
        val tilt = ((-x / 4.5f).coerceIn(-1f, 1f) * 20).roundToInt() / 20f
        val down = if (p.faceDown) z < -6f else z < -8f
        if (tilt != p.tilt || down != p.faceDown) {
            phone.value = p.copy(tilt = tilt, faceDown = down)
        }
    }

    private fun refresh() {
        val b = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = b?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = b?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = b?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        phone.value = phone.value.copy(
            battery = if (level >= 0 && scale > 0) level * 100 / scale else 80,
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL,
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        )
    }

    private fun showIsland() {
        val wm = getSystemService(WindowManager::class.java)
        val density = resources.displayMetrics.density
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        lp.y = (8 * density).toInt()
        if (Build.VERSION.SDK_INT >= 30) {
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val v = ComposeView(this)
        v.setViewTreeLifecycleOwner(owner)
        v.setViewTreeSavedStateRegistryOwner(owner)
        v.setContent { Island(phone.value) }
        wm.addView(v, lp)
        view = v
    }
}
