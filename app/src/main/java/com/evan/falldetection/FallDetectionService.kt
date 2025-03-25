package com.evan.falldetection

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
import android.hardware.*
import android.os.IBinder
import android.util.Log
import com.evan.falldetection.presentation.FallAlertActivity
import com.evan.falldetection.presentation.MainActivity
import kotlin.math.pow
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification(), FOREGROUND_SERVICE_TYPE_HEALTH)
        //get the sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        //register the listener
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val ax = it.values[0]
                    val ay = it.values[1]
                    val az = it.values[2]
                    //compute acceleration using the root of the power of x + the power of y + the power of z axes
                    val acceleration = sqrt(ax.pow(2) + ay.pow(2) + az.pow(2))
                    Log.d("FallDetection", "Accelerometer: X=$ax, Y=$ay, Z=$az, Acc=$acceleration")

                    if (acceleration > 25) { // Fall threshold set as 25m/s2
                        Log.w("FallDetection", "⚠️ Possible Fall Detected! ⚠️")
                        triggerFallAlert()
                    }
                }

                Sensor.TYPE_GYROSCOPE -> {
                    val gx = it.values[0]
                    val gy = it.values[1]
                    val gz = it.values[2]
                    Log.d("FallDetection", "Gyroscope: X=$gx, Y=$gy, Z=$gz")
                }
            }
        }
    }

    private fun triggerFallAlert() {
        val intent = Intent(this, FallAlertActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "fall_detection_channel"
        val channel = NotificationChannel(
            channelId,
            "Fall Detection",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, channelId)
            .setContentTitle("Fall Detection Running")
            .setContentText("Monitoring for falls...")
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
    }
}
