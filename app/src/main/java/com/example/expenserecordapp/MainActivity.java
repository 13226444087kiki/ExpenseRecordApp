package com.example.expenserecordapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expenserecordapp.ui.RecordListActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;
import java.util.Calendar;

import com.example.expenserecordapp.adapter.ExpenseAdapter;
import com.example.expenserecordapp.broadcast.BroadcastActions;
import com.example.expenserecordapp.broadcast.ExpenseUpdateReceiver;
import com.example.expenserecordapp.helper.NotificationHelper;
import com.example.expenserecordapp.service.ExpenseMonitorService;
import com.example.expenserecordapp.service.ServiceStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ExpenseViewModel viewModel;
    private NotificationHelper notificationHelper;
    private static final int NOTIFICATION_ID_REMINDER = 1001;

    // UI 组件
    private EditText etAmount;
    private EditText etCategory;
    private EditText etDate;
    private EditText etNote;
    private Button btnAddExpense;
    private Button btnViewRecords;
    private Button btnSendNotification;
    private Button btnSendBroadcast;
    private Button btnStartService;
    private Button btnStopService;
    private Button btnTestProvider;
    private Button btnExpense;
    private Button btnIncome;
    private TextView tvTotalAmount;
    private TextView tvMonthAmount;
    private TextView tvErrorMessage;
    private TextView tvSuccessMessage;
    private TextView tvBroadcastStatus;
    private TextView tvServiceStatus;
    private TextView tvProviderStatus;
    private RecyclerView rvExpenseList;
    private LinearLayout llEmptyState;

    // 类型选择
    private boolean isExpense = true;

    private ExpenseAdapter expenseAdapter;

    // 广播相关字段
    private ExpenseUpdateReceiver expenseUpdateReceiver;
    private int broadcastCount = 0;

    // Service相关字段
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        viewModel.setContext(this);
        notificationHelper = new NotificationHelper(this);

        initViews();
        setupRecyclerView();
        setupButtonListeners();
        setupInputMethod();
        observeViewModel();

        // 加载数据
        viewModel.loadTransactions();

        // 初始化广播接收器
        registerExpenseUpdateReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回首页时重新加载数据，确保与记账记录页面同步
        viewModel.loadTransactions();

        // 初始化Service状态
        updateServiceStatus(false);
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        etCategory = findViewById(R.id.et_category);
        etDate = findViewById(R.id.et_date);
        etNote = findViewById(R.id.et_note);
        btnAddExpense = findViewById(R.id.btn_add_expense);
        btnViewRecords = findViewById(R.id.btn_view_records);
        btnSendNotification = findViewById(R.id.btn_send_notification);
        btnSendBroadcast = findViewById(R.id.btn_send_broadcast);
        btnStartService = findViewById(R.id.btn_start_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        btnTestProvider = findViewById(R.id.btn_test_provider);
        btnExpense = findViewById(R.id.btn_expense);
        btnIncome = findViewById(R.id.btn_income);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvMonthAmount = findViewById(R.id.tv_month_amount);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        tvSuccessMessage = findViewById(R.id.tv_success_message);
        tvBroadcastStatus = findViewById(R.id.tv_broadcast_status);
        tvServiceStatus = findViewById(R.id.tv_service_status);
        tvProviderStatus = findViewById(R.id.tv_provider_status);
        rvExpenseList = findViewById(R.id.rv_expense_list);
        llEmptyState = findViewById(R.id.ll_empty_state);
        
        // 设置默认日期为当前时间
        etDate.setText(getCurrentDate());
        
        // 为日期输入框添加点击事件，弹出日历选择器
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
        
        // 设置输入法类型，优先使用中文简体拼音键盘
        etCategory.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etNote.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        // 禁用日期输入框的键盘输入
        etDate.setInputType(InputType.TYPE_NULL);
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter();
        rvExpenseList.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseList.setAdapter(expenseAdapter);

        // 设置左滑删除功能
        setupItemTouchHelper();
    }

    /**
     * 设置ItemTouchHelper，实现左滑删除功能
     */
    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                viewModel.deleteTransaction(position);
                expenseAdapter.removeItem(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvExpenseList);
    }

    private void setupButtonListeners() {
        btnAddExpense.setOnClickListener(v -> {
            addExpense();
        });

        btnViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
            startActivity(intent);
        });

        btnExpense.setOnClickListener(v -> {
            // 选择支出类型
            isExpense = true;
            btnExpense.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
            btnExpense.setTextColor(getResources().getColor(R.color.white));
            btnIncome.setBackgroundTintList(getResources().getColorStateList(R.color.gray_300));
            btnIncome.setTextColor(getResources().getColor(R.color.gray_700));
        });

        btnIncome.setOnClickListener(v -> {
            // 选择收入类型
            isExpense = false;
            btnIncome.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
            btnIncome.setTextColor(getResources().getColor(R.color.white));
            btnExpense.setBackgroundTintList(getResources().getColorStateList(R.color.gray_300));
            btnExpense.setTextColor(getResources().getColor(R.color.gray_700));
        });

        btnSendNotification.setOnClickListener(v -> {
            // 发送记账提醒通知
            notificationHelper.sendExpenseReminderNotification(NOTIFICATION_ID_REMINDER);
            Toast.makeText(this, "记账提醒通知已发送", Toast.LENGTH_SHORT).show();
        });

        btnSendBroadcast.setOnClickListener(v -> {
            // 发送账目更新广播
            sendExpenseUpdateBroadcast();
        });

        btnStartService.setOnClickListener(v -> {
            // 启动后台服务
            startExpenseMonitorService();
        });

        btnStopService.setOnClickListener(v -> {
            // 停止后台服务
            stopExpenseMonitorService();
        });

        btnTestProvider.setOnClickListener(v -> {
            // 测试ContentProvider
            testContentProvider();
        });
    }

    private void setupInputMethod() {
        etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etCategory.requestFocus();
                return true;
            }
            return false;
        });

        etCategory.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etDate.requestFocus();
                return true;
            }
            return false;
        });

        etDate.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etNote.requestFocus();
                return true;
            }
            return false;
        });

        etNote.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addExpense();
                return true;
            }
            return false;
        });
    }

    private void addExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        clearMessages();

        if (!validateInput(amountStr, category, date)) {
            return;
        }

        try {
            double amount = parseAmount(amountStr);

            if (amount <= 0) {
                showErrorMessage("金额必须大于0");
                return;
            }

            // 根据用户选择设置类型
            String type = isExpense ? "支出" : "收入";
            
            // 调用ViewModel的方法添加记录
            boolean success = viewModel.addTransaction(amount, type, category, note, date);

            if (success) {
                showSuccessMessage(isExpense ? "消费记录添加成功！" : "收入记录添加成功！");
                clearInputFields();
                Toast.makeText(this, isExpense ? "消费记录已添加" : "收入记录已添加", Toast.LENGTH_SHORT).show();

                // 添加成功后发送通知
                notificationHelper.sendExpenseReminderNotification(NOTIFICATION_ID_REMINDER);
                
                // 添加成功后发送广播
                sendExpenseUpdateBroadcastWithData(amount, category, date);
            } else {
                showErrorMessage("添加失败，请重试");
            }

        } catch (NumberFormatException e) {
            showErrorMessage("金额格式不正确，请输入有效的数字");
        } catch (IllegalArgumentException e) {
            showErrorMessage(e.getMessage());
        }
    }

    private double parseAmount(String amountStr) throws NumberFormatException, IllegalArgumentException {
        if (TextUtils.isEmpty(amountStr)) {
            throw new IllegalArgumentException("请输入消费金额");
        }

        String cleanedAmount = amountStr
                .replace(",", "")
                .replace(" ", "")
                .replace("，", "")
                .trim();

        if (!cleanedAmount.matches("^-?\\d+(\\.\\d+)?$")) {
            throw new NumberFormatException("金额包含非法字符");
        }

        try {
            double amount = Double.parseDouble(cleanedAmount);

            if (amount > 1000000) {
                throw new IllegalArgumentException("金额过大，请输入小于100万的金额");
            }

            if (cleanedAmount.contains(".")) {
                int decimalPlaces = cleanedAmount.length() - cleanedAmount.indexOf(".") - 1;
                if (decimalPlaces > 2) {
                    throw new IllegalArgumentException("金额最多支持两位小数");
                }
            }

            return amount;

        } catch (NumberFormatException e) {
            throw new NumberFormatException("金额格式不正确，请输入有效的数字");
        }
    }

    private boolean validateInput(String amountStr, String category, String date) {
        if (TextUtils.isEmpty(amountStr)) {
            showErrorMessage("请输入金额");
            return false;
        }

        if (TextUtils.isEmpty(category)) {
            showErrorMessage("请输入类别");
            return false;
        }

        if (TextUtils.isEmpty(date)) {
            showErrorMessage("请输入日期");
            return false;
        }

        return true;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void clearInputFields() {
        etAmount.setText("");
        etCategory.setText("");
        etDate.setText(getCurrentDate());
        etNote.setText("");
        etAmount.requestFocus();
    }

    /**
     * 显示日期时间选择器
     */
    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 先显示日期选择器
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // 然后显示时间选择器
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // 格式化日期时间
                        String dateTime = String.format("%04d-%02d-%02d %02d:%02d:00",
                                year, month + 1, dayOfMonth, hourOfDay, minute);
                        etDate.setText(dateTime);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void clearMessages() {
        tvErrorMessage.setVisibility(View.GONE);
        tvSuccessMessage.setVisibility(View.GONE);
    }

    private void showErrorMessage(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvSuccessMessage.setVisibility(View.GONE);
    }

    private void showSuccessMessage(String message) {
        tvSuccessMessage.setText(message);
        tvSuccessMessage.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        // 观察总金额变化
        viewModel.getTotalAmount().observe(this, totalAmount -> {
            tvTotalAmount.setText(String.format("¥ %.2f", totalAmount));
        });

        // 观察当月金额变化
        viewModel.getMonthAmount().observe(this, monthAmount -> {
            tvMonthAmount.setText(String.format("¥ %.2f", monthAmount));
        });

        // 观察交易列表变化
        viewModel.getTransactionList().observe(this, transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                rvExpenseList.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvExpenseList.setVisibility(View.VISIBLE);
                llEmptyState.setVisibility(View.GONE);
                expenseAdapter.setTransactions(transactions);
            }
        });

        // 观察错误消息
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showErrorMessage(errorMessage);
            }
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnAddExpense.setEnabled(false);
                btnAddExpense.setText("处理中...");
            } else {
                btnAddExpense.setEnabled(true);
                btnAddExpense.setText("添加消费");
            }
        });
    }

    /**
     * 注册账目更新广播接收器
     */
    private void registerExpenseUpdateReceiver() {
        // 创建IntentFilter，指定要监听的广播Action
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.ACTION_EXPENSE_UPDATED);

        // 创建广播接收器实例
        expenseUpdateReceiver = new ExpenseUpdateReceiver();

        // 注册广播接收器
        registerReceiver(expenseUpdateReceiver, filter);

        // 更新状态显示
        updateBroadcastStatus("广播接收器已注册");
    }



    /**
     * 发送账目更新广播（手动触发）
     */
    private void sendExpenseUpdateBroadcast() {
        // 获取当前输入的数据
        String amountStr = etAmount.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        // 如果金额为空，使用默认值演示
        double amount = 0.0;
        try {
            if (!amountStr.isEmpty()) {
                amount = Double.parseDouble(amountStr);
            } else {
                amount = 50.0 + (broadcastCount * 10); // 演示用随机金额
            }
        } catch (NumberFormatException e) {
            amount = 100.0; // 默认金额
        }

        // 获取当前时间
        String currentDate = getCurrentDate();

        // 发送带数据的广播
        sendExpenseUpdateBroadcastWithData(amount, category, currentDate);
    }

    /**
     * 发送带数据的账目更新广播
     */
    private void sendExpenseUpdateBroadcastWithData(double amount, String category, String date) {
        // 创建Intent并设置Action
        Intent broadcastIntent = new Intent(BroadcastActions.ACTION_EXPENSE_UPDATED);

        // 添加额外数据
        broadcastIntent.putExtra(BroadcastActions.EXTRA_EXPENSE_AMOUNT, amount);
        broadcastIntent.putExtra(BroadcastActions.EXTRA_EXPENSE_CATEGORY,
                category.isEmpty() ? "演示类别" : category);
        broadcastIntent.putExtra(BroadcastActions.EXTRA_EXPENSE_DATE, date);

        // 发送广播
        sendBroadcast(broadcastIntent);

        // 更新计数器
        broadcastCount++;

        // 更新状态显示
        String status = String.format("已发送 %d 条广播\n金额: ¥%.2f\n类别: %s\n时间: %s",
                broadcastCount, amount,
                category.isEmpty() ? "演示类别" : category,
                date);
        updateBroadcastStatus(status);

        // 显示发送成功的Toast
        Toast.makeText(this, "账目更新广播已发送", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新广播状态显示
     */
    private void updateBroadcastStatus(String status) {
        if (tvBroadcastStatus != null) {
            tvBroadcastStatus.setText(status);
        }
    }

    /**
     * 启动账目监控服务
     */
    private void startExpenseMonitorService() {
        Intent serviceIntent = new Intent(this, ExpenseMonitorService.class);
        serviceIntent.setAction(ServiceStatus.ACTION_START_SERVICE);

        // 启动服务，使用普通服务方式，避免ForegroundServiceStartNotAllowedException
        startService(serviceIntent);

        isServiceRunning = true;
        updateServiceStatus(true);

        Toast.makeText(this, "后台任务已启动", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "账目监控服务已启动");
    }

    /**
     * 停止账目监控服务
     */
    private void stopExpenseMonitorService() {
        Intent serviceIntent = new Intent(this, ExpenseMonitorService.class);
        serviceIntent.setAction(ServiceStatus.ACTION_STOP_SERVICE);

        stopService(serviceIntent);

        isServiceRunning = false;
        updateServiceStatus(false);

        Toast.makeText(this, "后台任务已停止", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "账目监控服务已停止");
    }



    /**
     * 更新服务状态显示
     */
    private void updateServiceStatus(boolean isRunning) {
        if (tvServiceStatus != null) {
            if (isRunning) {
                tvServiceStatus.setText("状态：后台任务运行中");
                tvServiceStatus.setTextColor(getResources().getColor(R.color.success));

                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
            } else {
                tvServiceStatus.setText("状态：后台任务已停止");
                tvServiceStatus.setTextColor(getResources().getColor(R.color.error));

                btnStartService.setEnabled(true);
                btnStopService.setEnabled(false);
            }
        }
    }

    /**
     * 测试ContentProvider功能
     */
    private void testContentProvider() {
        StringBuilder status = new StringBuilder("ContentProvider测试结果：\n");
        
        try {
            // 1. 查询所有消费记录
            status.append("\n1. 查询所有消费记录：\n");
            Cursor cursor = getContentResolver().query(
                    ExpenseContract.ExpenseEntry.CONTENT_URI,
                    null, null, null, null
            );
            
            if (cursor != null) {
                int count = 0;
                
                // 获取列索引
                int idIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry._ID);
                int amountIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT);
                int categoryIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY);
                int noteIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_NOTE);
                int dateIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_DATE);
                
                while (cursor.moveToNext()) {
                    int id = (idIndex >= 0) ? cursor.getInt(idIndex) : 0;
                    double amount = (amountIndex >= 0) ? cursor.getDouble(amountIndex) : 0.0;
                    String category = (categoryIndex >= 0) ? cursor.getString(categoryIndex) : "";
                    String note = (noteIndex >= 0) ? cursor.getString(noteIndex) : "";
                    String billDate = (dateIndex >= 0) ? cursor.getString(dateIndex) : "";
                    
                    status.append(String.format("ID: %d, 金额: %.2f, 类别: %s, 备注: %s, 日期: %s\n",
                            id, amount, category, note, billDate));
                    count++;
                }
                cursor.close();
                status.append(String.format("共查询到 %d 条记录\n", count));
            } else {
                status.append("查询失败\n");
            }
            
            // 2. 插入一条新的消费记录
            status.append("\n2. 插入新记录：\n");
            ContentValues values = new ContentValues();
            values.put(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT, 88.88);
            values.put(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY, "测试");
            values.put(ExpenseContract.ExpenseEntry.COLUMN_NOTE, "ContentProvider测试");
            values.put(ExpenseContract.ExpenseEntry.COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            
            Uri newUri = getContentResolver().insert(ExpenseContract.ExpenseEntry.CONTENT_URI, values);
            if (newUri != null) {
                status.append("插入成功！新记录URI: " + newUri.toString() + "\n");
            } else {
                status.append("插入失败\n");
            }
            
            // 3. 再次查询，确认插入成功
            status.append("\n3. 插入后再次查询：\n");
            cursor = getContentResolver().query(
                    ExpenseContract.ExpenseEntry.CONTENT_URI,
                    null, null, null, null
            );
            
            if (cursor != null) {
                int count = cursor.getCount();
                cursor.close();
                status.append(String.format("现在共有 %d 条记录\n", count));
            }
            
        } catch (Exception e) {
            status.append("测试过程中发生错误：" + e.getMessage() + "\n");
            e.printStackTrace();
        }
        
        // 更新状态显示
        if (tvProviderStatus != null) {
            tvProviderStatus.setText(status.toString());
        }
        
        // 显示测试结果
        Toast.makeText(this, "ContentProvider测试完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 注销广播接收器（重要！避免内存泄漏）
        if (expenseUpdateReceiver != null) {
            unregisterReceiver(expenseUpdateReceiver);
            expenseUpdateReceiver = null;
        }

        // 停止服务（可选）
        if (isServiceRunning) {
            stopExpenseMonitorService();
        }

        // 清理通知（可选）
        // notificationHelper.cancelNotification(NOTIFICATION_ID_REMINDER);
    }
}