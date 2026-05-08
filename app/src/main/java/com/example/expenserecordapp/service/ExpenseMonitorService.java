package com.example.expenserecordapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.expenserecordapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 账目监控服务
 * 负责在后台执行账目整理和提醒任务
 * 体现Service的作用：在后台执行长时间运行的任务
 */
public class ExpenseMonitorService extends Service {

    private static final String TAG = "ExpenseMonitorService";

    // 后台任务相关
    private Handler serviceHandler;
    private Runnable monitoringTask;
    private boolean isRunning = false;
    private int taskCounter = 0;

    // 模拟数据
    private final Random random = new Random();
    private final String[] expenseCategories = {"餐饮", "交通", "购物", "娱乐", "生活缴费", "其他"};
    private final String[] reminderMessages = {
        "今天还没有记录支出，记得记账哦！",
        "本月消费已超预算，请注意控制开支",
        "发现重复消费记录，建议检查",
        "本周餐饮消费较高，建议调整",
        "交通费用比上周下降20%，继续保持"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

        // 初始化Handler（在主线程）
        serviceHandler = new Handler(Looper.getMainLooper());

        // 初始化监控任务
        monitoringTask = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    executeMonitoringTask();
                    // 每隔5秒执行一次
                    serviceHandler.postDelayed(this, 5000);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            if (ServiceStatus.ACTION_STOP_SERVICE.equals(action)) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        // 启动后台任务
        startMonitoring();

        // 显示服务启动提示
        showToast("账目监控服务已启动");

        // 如果服务被杀死，不自动重启
        return START_NOT_STICKY;
    }

    /**
     * 启动监控任务
     */
    private void startMonitoring() {
        if (!isRunning) {
            isRunning = true;
            taskCounter = 0;
            serviceHandler.post(monitoringTask);
            Log.d(TAG, "监控任务已启动");
        }
    }

    /**
     * 执行监控任务
     */
    private void executeMonitoringTask() {
        taskCounter++;

        // 模拟不同的后台任务
        switch (taskCounter % 4) {
            case 0:
                simulateDataCleaning();
                break;
            case 1:
                simulateExpenseAnalysis();
                break;
            case 2:
                simulateReminderCheck();
                break;
            case 3:
                simulateDailySummary();
                break;
        }

        Log.d(TAG, "执行第 " + taskCounter + " 次监控任务");
    }

    /**
     * 模拟数据整理
     */
    private void simulateDataCleaning() {
        String currentTime = getCurrentTime();
        String category = expenseCategories[random.nextInt(expenseCategories.length)];
        int count = random.nextInt(10) + 1;

        String logMessage = String.format("[%s] 数据整理：清理了 %d 条无效的 '%s' 类别记录",
                currentTime, count, category);
        Log.i(TAG, logMessage);

        // 可以在这里添加实际的数据整理逻辑
        // 例如：清理重复记录、验证数据完整性等
    }

    /**
     * 模拟消费分析
     */
    private void simulateExpenseAnalysis() {
        String currentTime = getCurrentTime();
        String category = expenseCategories[random.nextInt(expenseCategories.length)];
        double amount = random.nextDouble() * 1000;

        String logMessage = String.format("[%s] 消费分析：'%s' 类别本月消费 ¥%.2f，占比 %.1f%%",
                currentTime, category, amount, random.nextDouble() * 30);
        Log.i(TAG, logMessage);

        // 可以在这里添加实际的消费分析逻辑
        // 例如：计算各类别消费占比、趋势分析等
    }

    /**
     * 模拟提醒检查
     */
    private void simulateReminderCheck() {
        String currentTime = getCurrentTime();
        String reminder = reminderMessages[random.nextInt(reminderMessages.length)];

        String logMessage = String.format("[%s] 提醒检查：%s", currentTime, reminder);
        Log.i(TAG, logMessage);

        // 每3次任务显示一次Toast提醒（避免太频繁）
        if (taskCounter % 3 == 0) {
            showToast(reminder);
        }

        // 可以在这里添加实际的提醒逻辑
        // 例如：检查今天是否记账、预算是否超支等
    }

    /**
     * 模拟每日汇总
     */
    private void simulateDailySummary() {
        String currentTime = getCurrentTime();
        int totalExpenses = random.nextInt(20) + 5;
        double totalAmount = random.nextDouble() * 5000;

        String logMessage = String.format("[%s] 每日汇总：今日共 %d 笔消费，总计 ¥%.2f",
                currentTime, totalExpenses, totalAmount);
        Log.i(TAG, logMessage);

        // 可以在这里添加实际的汇总逻辑
        // 例如：生成每日消费报告、统计关键指标等
    }

    /**
     * 显示Toast消息
     */
    private void showToast(String message) {
        serviceHandler.post(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 获取当前时间
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");

        // 停止后台任务
        stopMonitoring();

        // 移除显示Toast的代码，避免在服务销毁时崩溃
        // showToast("账目监控服务已停止");

        super.onDestroy();
    }

    /**
     * 停止监控任务
     */
    private void stopMonitoring() {
        if (isRunning) {
            isRunning = false;
            serviceHandler.removeCallbacks(monitoringTask);
            Log.d(TAG, "监控任务已停止");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 这是一个Started Service，不需要绑定
        return null;
    }
}