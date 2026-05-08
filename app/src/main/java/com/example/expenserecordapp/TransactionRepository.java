package com.example.expenserecordapp;


import java.util.ArrayList;
import java.util.List;

/**
 * 消费记录数据仓库
 * 负责统一管理消费记录数据，提供数据访问接口
 * 当前版本使用内存存储，后续可扩展为数据库存储
 */
public class TransactionRepository {

    // 内存中的消费记录列表
    private List<Transaction> transactionList;

    // 单例模式，确保全局只有一个Repository实例
    private static TransactionRepository instance;

    /**
     * 私有构造方法，防止外部创建实例
     */
    private TransactionRepository() {
        transactionList = new ArrayList<>();
        initializeSampleData();
    }

    /**
     * 获取Repository单例实例
     *
     * @return TransactionRepository实例
     */
    public static synchronized TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    /**
     * 添加消费记录
     *
     * @param transaction 要添加的消费记录
     */
    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            // 添加到列表开头，最新记录显示在最前面
            transactionList.add(0, transaction);
        }
    }

    /**
     * 添加消费记录（通过参数）
     *
     * @param amount 消费金额
     * @param category 消费类别
     * @param note 备注
     * @param date 日期
     */
    public void addTransaction(double amount, String category, String note, String date) {
        Transaction transaction = new Transaction(amount, category, note, date);
        addTransaction(transaction);
    }

    /**
     * 删除消费记录
     *
     * @param transaction 要删除的消费记录
     * @return 是否删除成功
     */
    public boolean deleteTransaction(Transaction transaction) {
        return transactionList.remove(transaction);
    }

    /**
     * 删除指定位置的消费记录
     *
     * @param position 要删除的记录位置
     * @return 被删除的消费记录，如果位置无效则返回null
     */
    public Transaction deleteTransaction(int position) {
        if (position >= 0 && position < transactionList.size()) {
            return transactionList.remove(position);
        }
        return null;
    }

    /**
     * 获取所有消费记录
     *
     * @return 消费记录列表的副本（防止外部修改内部数据）
     */
    public List<Transaction> getAllTransactions() {
        // 返回副本，防止外部修改内部数据
        return new ArrayList<>(transactionList);
    }

    /**
     * 获取总消费金额
     *
     * @return 总消费金额
     */
    public double getTotalExpense() {
        double total = 0.0;

        for (Transaction transaction : transactionList) {
            total += transaction.getAmount();
        }

        return total;
    }

    /**
     * 获取指定位置的消费记录
     *
     * @param position 位置
     * @return 消费记录，如果位置无效则返回null
     */
    public Transaction getTransactionAt(int position) {
        if (position >= 0 && position < transactionList.size()) {
            return transactionList.get(position);
        }
        return null;
    }

    /**
     * 获取消费记录数量
     *
     * @return 记录数量
     */
    public int getTransactionCount() {
        return transactionList.size();
    }

    /**
     * 清空所有消费记录
     */
    public void clearAllTransactions() {
        transactionList.clear();
    }

    /**
     * 更新消费记录
     *
     * @param position 位置
     * @param transaction 更新后的消费记录
     * @return 是否更新成功
     */
    public boolean updateTransaction(int position, Transaction transaction) {
        if (position >= 0 && position < transactionList.size() && transaction != null) {
            transactionList.set(position, transaction);
            return true;
        }
        return false;
    }

    /**
     * 根据类别筛选消费记录
     *
     * @param category 类别
     * @return 该类别的消费记录列表
     */
    public List<Transaction> getTransactionsByCategory(String category) {
        List<Transaction> result = new ArrayList<>();

        for (Transaction transaction : transactionList) {
            if (transaction.getCategory().equals(category)) {
                result.add(transaction);
            }
        }

        return result;
    }

    /**
     * 获取指定日期的消费记录
     *
     * @param date 日期
     * @return 该日期的消费记录列表
     */
    public List<Transaction> getTransactionsByDate(String date) {
        List<Transaction> result = new ArrayList<>();

        for (Transaction transaction : transactionList) {
            if (transaction.getDate().equals(date)) {
                result.add(transaction);
            }
        }

        return result;
    }

    /**
     * 获取指定类别的总消费金额
     *
     * @param category 类别
     * @return 该类别的总消费金额
     */
    public double getTotalExpenseByCategory(String category) {
        double total = 0.0;

        for (Transaction transaction : transactionList) {
            if (transaction.getCategory().equals(category)) {
                total += transaction.getAmount();
            }
        }

        return total;
    }

    /**
     * 初始化示例数据（开发阶段使用）
     */
    private void initializeSampleData() {
        addTransaction(28.50, "餐饮", "早餐", "2024-01-15");
        addTransaction(15.00, "交通", "地铁", "2024-01-15");
        addTransaction(128.00, "购物", "买书", "2024-01-14");
        addTransaction(45.80, "餐饮", "午餐", "2024-01-14");
        addTransaction(9.90, "娱乐", "电影票", "2024-01-13");
    }
}

