package com.light.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val stopServiceIntent = Intent(it, AlarmService::class.java)
            it.stopService(stopServiceIntent)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.cancel(1)
        }
    }
}
