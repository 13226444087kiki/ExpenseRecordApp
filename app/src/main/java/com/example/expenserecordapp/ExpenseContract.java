package com.example.expenserecordapp;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 消费记录ContentProvider的契约类
 * 定义了ContentProvider使用的URI、表结构和常量
 * 用于教学演示ContentProvider的基本概念
 */
public final class ExpenseContract {
    
    // 防止实例化
    private ExpenseContract() {}
    
    // 授权字符串 - 用于标识ContentProvider
    public static final String AUTHORITY = "com.example.expenserecordapp.provider";
    
    // 基础URI - content://com.example.expenserecordapp.provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    // 路径部分 - 用于标识不同的数据集合
    public static final String PATH_EXPENSES = "expenses";
    
    /**
     * 消费记录表的结构定义
     */
    public static final class ExpenseEntry implements BaseColumns {
        
        // 完整URI - content://com.example.expenserecordapp.provider/expenses
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXPENSES).build();
        
        // 表名
        public static final String TABLE_NAME = "expenses";
        
        // 列名
        public static final String COLUMN_AMOUNT = "amount";        // 金额
        public static final String COLUMN_TYPE = "type";            // 类型（收入/支出）
        public static final String COLUMN_CATEGORY = "category";    // 类别
        public static final String COLUMN_NOTE = "note";            // 备注
        public static final String COLUMN_DATE = "billDate";        // 账单日期
        
        // MIME类型
        public static final String CONTENT_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.example.expenserecordapp.expense";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.example.expenserecordapp.expense";
    }
}
