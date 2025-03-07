package com.light.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TimePicker
import android.widget.CheckBox
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Locale
import androidx.core.content.edit
import androidx.core.net.toUri


class MainActivity : AppCompatActivity() {
    private lateinit var timePicker: TimePicker
    private lateinit var checkBoxRepeat: CheckBox
    private lateinit var btnSetAlarm: Button
    private lateinit var etSelectedTime: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestExactAlarmPermission(this)
        createNotificationChannel()
        val permPref: SharedPreferences = getSharedPreferences("permission", Context.MODE_PRIVATE)
        if (!permPref.getBoolean("granted", false)) {
            requestAutoStartPermission(this)
        }

        timePicker = findViewById(R.id.timePicker)
        checkBoxRepeat = findViewById(R.id.checkBoxRepeat)
        btnSetAlarm = findViewById(R.id.btnSetAlarm)
        etSelectedTime = findViewById(R.id.etSelectedTime)
        timePicker.setIs24HourView(true)


        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val formattedTime = String.format(Locale.getDefault(),"%02d:%02d", hourOfDay, minute)
            etSelectedTime.setText(formattedTime)
        }

        btnSetAlarm.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            setAlarm(hour, minute, checkBoxRepeat.isChecked)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "alarm_channel",
            "Alarm Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }


    private fun setAlarm(hour: Int, minute: Int, repeat: Boolean) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        if (repeat) {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        Toast.makeText(this, "Báo thức đã đặt lúc ${String.format(Locale.getDefault(),"%02d:%02d", hour, minute)}", Toast.LENGTH_SHORT).show()
    }
    private fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    "package:${context.packageName}".toUri())
                context.startActivity(intent)
            }
        }
    }
    private fun requestAutoStartPermission(context: Context) {
        val manufacturer = Build.MANUFACTURER
        if (manufacturer.equals("Xiaomi", ignoreCase = true)) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                context.startActivity(intent)
                val permPref: SharedPreferences = context.getSharedPreferences("permission", Context.MODE_PRIVATE)
                permPref.edit() { putBoolean("granted", true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
