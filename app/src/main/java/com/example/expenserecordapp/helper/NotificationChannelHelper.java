package com.example.expenserecordapp.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * 通知渠道管理类
 * 负责创建和管理通知渠道（Android 8.0+ 要求）
 * 体现单一职责原则：只负责通知渠道的创建和管理
 */
public class NotificationChannelHelper {

    // 记账提醒通知渠道
    public static final String CHANNEL_ID_REMINDER = "expense_reminder_channel";
    public static final String CHANNEL_NAME_REMINDER = "记账提醒";
    public static final String CHANNEL_DESCRIPTION_REMINDER = "记账提醒通知";

    /**
     * 创建记账提醒通知渠道
     * @param context 上下文
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createReminderChannel(Context context) {
        // 创建通知渠道
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_REMINDER,
                CHANNEL_NAME_REMINDER,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        // 配置渠道属性
        channel.setDescription(CHANNEL_DESCRIPTION_REMINDER);
        channel.enableLights(true); // 启用指示灯
        channel.enableVibration(true); // 启用震动
        channel.setShowBadge(true); // 显示角标

        // 获取通知管理器并创建渠道
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 检查并创建所有需要的通知渠道
     * @param context 上下文
     */
    public static void createAllChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createReminderChannel(context);
        }
    }
}