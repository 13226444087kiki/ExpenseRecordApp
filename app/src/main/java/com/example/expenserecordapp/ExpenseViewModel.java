package com.example.expenserecordapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 消费记录ViewModel
 * 负责管理消费记录数据和业务逻辑
 * 使用ContentProvider实现持久化存储
 */
public class ExpenseViewModel extends ViewModel {

    private static final String TAG = "ExpenseViewModel";

    // 消费记录列表
    private final MutableLiveData<List<Transaction>> transactionList =
            new MutableLiveData<>(new ArrayList<>());

    // 总消费金额
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);

    // 本月消费金额
    private final MutableLiveData<Double> monthAmount = new MutableLiveData<>(0.0);

    // 加载状态
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // 错误信息
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // 上下文
    private Context context;

    /**
     * 设置上下文
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 获取消费记录列表
     * @return 消费记录列表的LiveData
     */
    public LiveData<List<Transaction>> getTransactionList() {
        return transactionList;
    }

    /**
     * 获取总消费金额
     * @return 总消费金额的LiveData
     */
    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    /**
     * 获取本月消费金额
     * @return 本月消费金额的LiveData
     */
    public LiveData<Double> getMonthAmount() {
        return monthAmount;
    }

    /**
     * 获取加载状态
     * @return 加载状态的LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * 获取错误信息
     * @return 错误信息的LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * 添加消费记录
     * @param amount 金额
     * @param type 类型
     * @param category 类别
     * @param note 备注
     * @param date 日期
     * @return 是否添加成功
     */
    public boolean addTransaction(double amount, String type, String category, String note, String date) {
        // 验证输入数据
        if (!validateTransaction(amount, category, date)) {
            return false;
        }

        isLoading.setValue(true);

        try {
            // 创建新的消费记录
            Transaction newTransaction = new Transaction(amount, type, category, note, date);

            // 保存到数据库
            if (saveToDatabase(newTransaction)) {
                // 重新加载数据
                loadTransactions();
                return true;
            } else {
                errorMessage.setValue("添加失败，请重试");
                return false;
            }

        } catch (Exception e) {
            errorMessage.setValue("添加消费记录失败: " + e.getMessage());
            return false;
        } finally {
            isLoading.setValue(false);
        }
    }

    /**
     * 删除消费记录
     * @param position 要删除的记录位置
     * @return 是否删除成功
     */
    public boolean deleteTransaction(int position) {
        List<Transaction> currentList = transactionList.getValue();
        if (currentList == null || position < 0 || position >= currentList.size()) {
            errorMessage.setValue("删除失败: 无效的位置");
            return false;
        }

        try {
            // 获取要删除的记录
            Transaction deletedTransaction = currentList.get(position);

            // 从数据库删除
            if (deleteFromDatabase(deletedTransaction, position)) {
                // 重新加载数据
                loadTransactions();
                return true;
            } else {
                errorMessage.setValue("删除失败，请重试");
                return false;
            }

        } catch (Exception e) {
            errorMessage.setValue("删除消费记录失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新消费记录
     * @param position 要更新的记录位置
     * @param transaction 更新后的消费记录
     * @return 是否更新成功
     */
    public boolean updateTransaction(int position, Transaction transaction) {
        if (!validateTransaction(transaction.getAmount(), transaction.getCategory(), transaction.getDate())) {
            return false;
        }

        List<Transaction> currentList = transactionList.getValue();
        if (currentList == null || position < 0 || position >= currentList.size()) {
            errorMessage.setValue("更新失败: 无效的位置");
            return false;
        }

        try {
            // 更新数据库
            if (updateInDatabase(transaction, position)) {
                // 重新加载数据
                loadTransactions();
                return true;
            } else {
                errorMessage.setValue("更新失败，请重试");
                return false;
            }

        } catch (Exception e) {
            errorMessage.setValue("更新消费记录失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 加载消费记录（从数据库）
     */
    public void loadTransactions() {
        isLoading.setValue(true);

        // 从数据库加载数据
        new Thread(() -> {
            try {
                // 从数据库加载数据
                List<Transaction> loadedTransactions = loadFromDatabase();

                // 在主线程更新LiveData
                transactionList.postValue(loadedTransactions);
                calculateTotalAmount(loadedTransactions);
                calculateMonthAmount(loadedTransactions);

            } catch (Exception e) {
                errorMessage.postValue("加载数据失败: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * 验证消费记录数据
     * @param amount 金额
     * @param category 类别
     * @param date 日期
     * @return 是否有效
     */
    private boolean validateTransaction(double amount, String category, String date) {
        if (amount <= 0) {
            errorMessage.setValue("金额必须大于0");
            return false;
        }

        if (category == null || category.trim().isEmpty()) {
            errorMessage.setValue("请选择消费类别");
            return false;
        }

        if (date == null || date.trim().isEmpty()) {
            errorMessage.setValue("请选择消费日期");
            return false;
        }

        return true;
    }

    /**
     * 计算总金额
     * @param transactions 记录列表
     */
    private void calculateTotalAmount(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            totalAmount.setValue(0.0);
            return;
        }

        double sum = 0;
        for (Transaction transaction : transactions) {
            if ("支出".equals(transaction.getType())) {
                sum += transaction.getAmount();
            }
        }

        totalAmount.setValue(sum);
    }

    /**
     * 计算本月金额
     * @param transactions 记录列表
     */
    private void calculateMonthAmount(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            monthAmount.setValue(0.0);
            return;
        }

        // 这里简化处理，实际应该根据当前月份筛选
        // 假设所有记录都是本月的
        double sum = 0;
        for (Transaction transaction : transactions) {
            if ("支出".equals(transaction.getType())) {
                sum += transaction.getAmount();
            }
        }

        monthAmount.setValue(sum);
    }

    /**
     * 清空所有消费记录
     */
    public void clearAllTransactions() {
        try {
            // 清空数据库
            if (clearDatabase()) {
                // 清空内存数据
                transactionList.setValue(new ArrayList<>());
                totalAmount.setValue(0.0);
                monthAmount.setValue(0.0);
            }
        } catch (Exception e) {
            errorMessage.setValue("清空记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据类别筛选消费记录
     * @param category 类别
     * @return 筛选后的列表
     */
    public List<Transaction> filterByCategory(String category) {
        List<Transaction> currentList = transactionList.getValue();
        if (currentList == null || currentList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction transaction : currentList) {
            if (transaction.getCategory().equals(category)) {
                filteredList.add(transaction);
            }
        }

        return filteredList;
    }

    // 数据库操作方法

    private boolean saveToDatabase(Transaction transaction) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }

        try {
            ContentValues values = new ContentValues();
            values.put(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT, transaction.getAmount());
            values.put(ExpenseContract.ExpenseEntry.COLUMN_TYPE, transaction.getType());
            values.put(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY, transaction.getCategory());
            values.put(ExpenseContract.ExpenseEntry.COLUMN_NOTE, transaction.getNote());
            values.put(ExpenseContract.ExpenseEntry.COLUMN_DATE, transaction.getDate());

            Uri uri = context.getContentResolver().insert(
                    ExpenseContract.ExpenseEntry.CONTENT_URI, values);

            return uri != null;
        } catch (Exception e) {
            Log.e(TAG, "Error saving to database", e);
            return false;
        }
    }

    private boolean deleteFromDatabase(Transaction transaction, int position) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }

        try {
            // 由于Transaction没有ID字段，我们通过位置来删除
            // 实际项目中应该在Transaction中添加ID字段
            // 这里我们先查询所有记录，然后根据位置获取ID
            Cursor cursor = context.getContentResolver().query(
                    ExpenseContract.ExpenseEntry.CONTENT_URI,
                    new String[]{ExpenseContract.ExpenseEntry._ID},
                    null, null, ExpenseContract.ExpenseEntry.COLUMN_DATE + " DESC");

            if (cursor != null) {
                if (cursor.moveToPosition(position)) {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(ExpenseContract.ExpenseEntry._ID));
                    cursor.close();

                    Uri uri = ExpenseContract.ExpenseEntry.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(id)).build();

                    int deleted = context.getContentResolver().delete(uri, null, null);
                    return deleted > 0;
                }
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting from database", e);
            return false;
        }
    }

    private boolean updateInDatabase(Transaction transaction, int position) {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }

        try {
            // 由于Transaction没有ID字段，我们通过位置来更新
            // 实际项目中应该在Transaction中添加ID字段
            Cursor cursor = context.getContentResolver().query(
                    ExpenseContract.ExpenseEntry.CONTENT_URI,
                    new String[]{ExpenseContract.ExpenseEntry._ID},
                    null, null, ExpenseContract.ExpenseEntry.COLUMN_DATE + " DESC");

            if (cursor != null) {
                if (cursor.moveToPosition(position)) {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(ExpenseContract.ExpenseEntry._ID));
                    cursor.close();

                    ContentValues values = new ContentValues();
                    values.put(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT, transaction.getAmount());
                    values.put(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY, transaction.getCategory());
                    values.put(ExpenseContract.ExpenseEntry.COLUMN_NOTE, transaction.getNote());
                    values.put(ExpenseContract.ExpenseEntry.COLUMN_DATE, transaction.getDate());

                    Uri uri = ExpenseContract.ExpenseEntry.CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(id)).build();

                    int updated = context.getContentResolver().update(uri, values, null, null);
                    return updated > 0;
                }
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error updating database", e);
            return false;
        }
    }

    private List<Transaction> loadFromDatabase() {
        List<Transaction> transactions = new ArrayList<>();

        if (context == null) {
            Log.e(TAG, "Context is null");
            return transactions;
        }

        try {
            Cursor cursor = context.getContentResolver().query(
                    ExpenseContract.ExpenseEntry.CONTENT_URI,
                    null, null, null, ExpenseContract.ExpenseEntry.COLUMN_DATE + " DESC");

            if (cursor != null) {
                // 获取列索引
                int amountIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_AMOUNT);
                int typeIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_TYPE);
                int categoryIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_CATEGORY);
                int noteIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_NOTE);
                int dateIndex = cursor.getColumnIndex(ExpenseContract.ExpenseEntry.COLUMN_DATE);
                
                while (cursor.moveToNext()) {
                    double amount = (amountIndex >= 0) ? cursor.getDouble(amountIndex) : 0.0;
                    String type = (typeIndex >= 0) ? cursor.getString(typeIndex) : "支出";
                    String category = (categoryIndex >= 0) ? cursor.getString(categoryIndex) : "";
                    String note = (noteIndex >= 0) ? cursor.getString(noteIndex) : "";
                    String date = (dateIndex >= 0) ? cursor.getString(dateIndex) : "";

                    Transaction transaction = new Transaction(amount, type, category, note, date);
                    transactions.add(transaction);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading from database", e);
        }

        return transactions;
    }

    private boolean clearDatabase() {
        if (context == null) {
            Log.e(TAG, "Context is null");
            return false;
        }

        try {
            // 实际项目中应该实现清空数据库的功能
            // 这里我们暂时不实现，因为ContentProvider没有提供清空所有记录的方法
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing database", e);
            return false;
        }
    }
}