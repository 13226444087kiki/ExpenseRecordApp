package com.example.expenserecordapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.expenserecordapp.R;
import com.example.expenserecordapp.ExpenseViewModel;
import com.example.expenserecordapp.Transaction;
import com.example.expenserecordapp.adapter.RecordAdapter;
import com.example.expenserecordapp.model.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * 记账记录列表页面
 * 用于展示所有消费记录
 * 与首页共享同一数据源
 */
public class RecordListActivity extends AppCompatActivity {

    private RecyclerView rvRecords;
    private TextView tvEmptyState;
    private RecordAdapter recordAdapter;
    private ExpenseViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        // 初始化视图
        initViews();

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        viewModel.setContext(this);

        // 设置 RecyclerView
        setupRecyclerView();

        // 观察数据变化
        observeViewModel();

        // 加载数据
        viewModel.loadTransactions();
    }

    private void initViews() {
        rvRecords = findViewById(R.id.rv_records);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        // 设置返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // 创建适配器
        recordAdapter = new RecordAdapter();

        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRecords.setLayoutManager(layoutManager);

        // 设置适配器
        rvRecords.setAdapter(recordAdapter);

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
                recordAdapter.removeItem(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvRecords);
    }

    /**
     * 观察ViewModel数据变化
     */
    private void observeViewModel() {
        // 观察交易列表变化
        viewModel.getTransactionList().observe(this, transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                // 显示空状态
                rvRecords.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                // 显示数据
                rvRecords.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);

                // 将Transaction转换为Record
                List<Record> records = convertTransactionsToRecords(transactions);
                recordAdapter.setRecords(records);
            }
        });
    }

    /**
     * 将Transaction列表转换为Record列表
     */
    private List<Record> convertTransactionsToRecords(List<Transaction> transactions) {
        List<Record> records = new ArrayList<>();
        int id = 1;

        for (Transaction transaction : transactions) {
            // 转换为Record对象
            Record record = new Record(
                id++, // 生成ID
                transaction.getCategory(), // 使用类别作为标题
                transaction.getAmount(), // 金额
                transaction.getDate(), // 日期
                Record.RecordType.EXPENSE, // 默认为支出
                getCategoryFromString(transaction.getCategory()), // 转换类别
                transaction.getNote() // 使用备注作为描述
            );
            records.add(record);
        }

        return records;
    }

    /**
     * 将字符串类别转换为Record.Category枚举
     */
    private Record.Category getCategoryFromString(String categoryStr) {
        switch (categoryStr.toLowerCase()) {
            case "餐饮":
            case "food":
                return Record.Category.FOOD;
            case "交通":
            case "transport":
                return Record.Category.TRANSPORT;
            case "购物":
            case "shopping":
                return Record.Category.SHOPPING;
            case "娱乐":
            case "entertainment":
                return Record.Category.ENTERTAINMENT;
            case "工资":
            case "salary":
                return Record.Category.SALARY;
            default:
                return Record.Category.OTHER;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到该页面时重新加载数据，确保与首页同步
        viewModel.loadTransactions();
    }
}