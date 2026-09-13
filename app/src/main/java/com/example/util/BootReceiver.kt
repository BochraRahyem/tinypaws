package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Restores care-reminder alarms after the device reboots (Android clears all
 * AlarmManager alarms on shutdown). Registered in AndroidManifest with the
 * RECEIVE_BOOT_COMPLETED permission.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val isBoot = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "android.intent.action.MY_PACKAGE_REPLACED"
        if (!isBoot) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(8_000L) {
                    // Instantiating NotificationHelper also (re)creates channels.
                    NotificationHelper(context.applicationContext)
                    ReminderScheduler.rescheduleAll(context.applicationContext)
                }
                android.util.Log.d("BootReceiver", "Reminders restored after $action")
            } catch (e: Exception) {
                android.util.Log.e("BootReceiver", "Failed to restore reminders after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
