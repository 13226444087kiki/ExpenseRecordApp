package com.example.expenserecordapp.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.expenserecordapp.MainActivity;

/**
 * 通知工具类
 * 负责创建和发送通知
 * 体现封装思想：将通知创建逻辑封装在独立的类中
 */
public class NotificationHelper {

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 确保通知渠道已创建（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelHelper.createAllChannels(context);
        }
    }

    /**
     * 发送记账提醒通知
     * @param notificationId 通知ID（用于更新或取消通知）
     */
    public void sendExpenseReminderNotification(int notificationId) {
        // 创建点击通知后跳转的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 创建PendingIntent（延迟执行的Intent）
        // 使用FLAG_UPDATE_CURRENT确保更新现有PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        Notification notification = new NotificationCompat.Builder(context,
                NotificationChannelHelper.CHANNEL_ID_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // 使用系统图标
                .setContentTitle("记账提醒") // 直接使用字符串
                .setContentText("今天还没有记录支出，点击立即记账") // 直接使用字符串
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 通知优先级
                .setContentIntent(pendingIntent) // 设置点击后的跳转
                .setAutoCancel(true) // 点击后自动取消通知
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("记账提醒：今天还没有记录任何支出。养成良好的记账习惯，可以帮助你更好地管理财务。点击立即开始记账。")) // 大文本样式
                .build();

        // 发送通知
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
    }

    /**
     * 取消指定ID的通知
     * @param notificationId 通知ID
     */
    public void cancelNotification(int notificationId) {
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }

    /**
     * 取消所有通知
     */
    public void cancelAllNotifications() {
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
}