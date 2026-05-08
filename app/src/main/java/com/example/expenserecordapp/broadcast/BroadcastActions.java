package com.example.expenserecordapp.broadcast;

/**
 * 广播Action常量类
 * 统一管理所有广播Action字符串，避免硬编码
 * 体现单一职责原则：只负责定义广播相关的常量
 */
public class BroadcastActions {

    // 账目更新广播Action
    public static final String ACTION_EXPENSE_UPDATED = "com.example.expenserecordapp.action.EXPENSE_UPDATED";

    // 广播Extra键名
    public static final String EXTRA_EXPENSE_AMOUNT = "extra_expense_amount";
    public static final String EXTRA_EXPENSE_CATEGORY = "extra_expense_category";
    public static final String EXTRA_EXPENSE_DATE = "extra_expense_date";

    // 私有构造函数，防止实例化
    private BroadcastActions() {
        throw new IllegalStateException("Constants class");
    }
}