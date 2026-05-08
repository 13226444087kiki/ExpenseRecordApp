package com.example.expenserecordapp.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.expenserecordapp.R;

/**
 * 账目更新广播接收器
 * 负责接收账目更新的广播通知
 * 体现观察者模式：监听特定事件并做出响应
 */
public class ExpenseUpdateReceiver extends BroadcastReceiver {

    /**
     * 接收到广播时的回调方法
     * @param context 上下文
     * @param intent 包含广播数据的Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 检查是否是账目更新广播
        if (BroadcastActions.ACTION_EXPENSE_UPDATED.equals(intent.getAction())) {
            // 从Intent中获取额外数据
            double amount = intent.getDoubleExtra(BroadcastActions.EXTRA_EXPENSE_AMOUNT, 0.0);
            String category = intent.getStringExtra(BroadcastActions.EXTRA_EXPENSE_CATEGORY);
            String date = intent.getStringExtra(BroadcastActions.EXTRA_EXPENSE_DATE);

            // 构建提示消息
            String message = buildNotificationMessage(amount, category, date);

            // 显示Toast提示（最简单的响应方式）
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            // 这里可以添加其他响应逻辑，例如：
            // 1. 更新UI组件
            // 2. 刷新数据
            // 3. 记录日志
            // 4. 发送其他广播
        }
    }

    /**
     * 构建通知消息
     */
    private String buildNotificationMessage(double amount, String category, String date) {
        StringBuilder message = new StringBuilder();
        message.append("收到账目更新通知\n");

        if (amount > 0) {
            message.append("金额：¥").append(String.format("%.2f", amount)).append("\n");
        }

        if (category != null && !category.isEmpty()) {
            message.append("类别：").append(category).append("\n");
        }

        if (date != null && !date.isEmpty()) {
            message.append("时间：").append(date);
        }

        return message.toString();
    }
}