package com.shohan.khatago.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shohan.khatago.AppContainer
import com.shohan.khatago.R
import com.shohan.khatago.core.Money
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val CHANNEL_ID = "payment_reminders"
    private const val WORK_NAME = "payment_reminder_worker"

    fun ensureScheduled(context: Context) {
        createChannel(context)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Payment reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Upcoming, due today and overdue payment reminders"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            manager.createNotificationChannel(channel)
        }
    }

    internal fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    internal fun notify(context: Context, id: Int, title: String, body: String) {
        if (!hasNotificationPermission(context)) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = AppContainer(applicationContext)
        val snapshot = container.repository.observeDashboard().first()
        when {
            snapshot.overdueCount > 0 -> ReminderScheduler.notify(
                applicationContext,
                1001,
                "Overdue payments need attention",
                "You have ${snapshot.overdueCount} overdue payments totalling ${Money.format(snapshot.overdueMinor)}."
            )
            snapshot.dueTodayItems.isNotEmpty() -> ReminderScheduler.notify(
                applicationContext,
                1002,
                "Payments due today",
                "You have ${snapshot.dueTodayItems.size} payment items due today."
            )
            snapshot.upcomingItems.isNotEmpty() -> ReminderScheduler.notify(
                applicationContext,
                1003,
                "Upcoming payments",
                "You have ${snapshot.upcomingItems.size} payments due soon."
            )
            else -> NotificationManagerCompat.from(applicationContext).cancelAll()
        }
        return Result.success()
    }
}
