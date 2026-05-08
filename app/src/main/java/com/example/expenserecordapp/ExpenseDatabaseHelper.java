package com.example.expenserecordapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 消费记录数据库帮助类
 * 负责创建和管理SQLite数据库
 */
public class ExpenseDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "ExpenseDatabaseHelper";
    
    // 数据库名称
    private static final String DATABASE_NAME = "expense_record.db";
    
    // 数据库版本
    private static final int DATABASE_VERSION = 1;
    
    // 创建表的SQL语句
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + 
            ExpenseContract.ExpenseEntry.TABLE_NAME + " (" +
            ExpenseContract.ExpenseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT + " REAL NOT NULL, " +
            ExpenseContract.ExpenseEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY + " TEXT NOT NULL, " +
            ExpenseContract.ExpenseEntry.COLUMN_NOTE + " TEXT, " +
            ExpenseContract.ExpenseEntry.COLUMN_DATE + " TEXT NOT NULL);";
    
    // 删除表的SQL语句
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + 
            ExpenseContract.ExpenseEntry.TABLE_NAME;
    
    public ExpenseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "ExpenseDatabaseHelper created");
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database table");
        db.execSQL(SQL_CREATE_TABLE);
        
        // 插入一些初始数据
        insertInitialData(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // 简单的升级策略：删除旧表并创建新表
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    /**
     * 插入初始数据
     */
    private void insertInitialData(SQLiteDatabase db) {
        Log.d(TAG, "Inserting initial data");
        
        // 插入一些示例数据
        String[] insertStatements = {
            "INSERT INTO " + ExpenseContract.ExpenseEntry.TABLE_NAME + " (" +
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_TYPE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_NOTE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_DATE + ") VALUES (88.50, '支出', '餐饮', '午餐', '2026-04-24 12:30:00');",
            
            "INSERT INTO " + ExpenseContract.ExpenseEntry.TABLE_NAME + " (" +
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_TYPE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_NOTE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_DATE + ") VALUES (120.00, '支出', '交通', '打车', '2026-04-24 09:15:00');",
            
            "INSERT INTO " + ExpenseContract.ExpenseEntry.TABLE_NAME + " (" +
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_TYPE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_NOTE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_DATE + ") VALUES (256.80, '支出', '购物', '超市', '2026-04-23 18:45:00');",
            
            "INSERT INTO " + ExpenseContract.ExpenseEntry.TABLE_NAME + " (" +
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_TYPE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_NOTE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_DATE + ") VALUES (68.00, '支出', '娱乐', '电影', '2026-04-23 20:30:00');",
            
            "INSERT INTO " + ExpenseContract.ExpenseEntry.TABLE_NAME + " (" +
            ExpenseContract.ExpenseEntry.COLUMN_AMOUNT + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_TYPE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_CATEGORY + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_NOTE + ", " +
            ExpenseContract.ExpenseEntry.COLUMN_DATE + ") VALUES (5000.00, '收入', '工资', '4月份工资', '2026-04-20 09:00:00');"
        };
        
        for (String statement : insertStatements) {
            db.execSQL(statement);
        }
        
        Log.d(TAG, "Initial data inserted");
    }
}
