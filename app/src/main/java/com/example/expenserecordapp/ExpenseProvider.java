package com.example.expenserecordapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * 消费记录ContentProvider
 * 使用SQLite数据库实现持久化存储
 */
public class ExpenseProvider extends ContentProvider {
    
    private static final String TAG = "ExpenseProvider";
    
    // URI匹配器
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    // 匹配码
    private static final int EXPENSES = 100;      // 所有消费记录
    private static final int EXPENSE_ID = 101;    // 单个消费记录
    
    // 静态初始化URI匹配器
    static {
        // 匹配 content://com.example.expenserecordapp.provider/expenses
        sUriMatcher.addURI(ExpenseContract.AUTHORITY, ExpenseContract.PATH_EXPENSES, EXPENSES);
        // 匹配 content://com.example.expenserecordapp.provider/expenses/#
        sUriMatcher.addURI(ExpenseContract.AUTHORITY, ExpenseContract.PATH_EXPENSES + "/#", EXPENSE_ID);
    }
    
    // 数据库帮助类
    private ExpenseDatabaseHelper dbHelper;
    
    @Override
    public boolean onCreate() {
        // 初始化数据库帮助类
        dbHelper = new ExpenseDatabaseHelper(getContext());
        Log.d(TAG, "ExpenseProvider onCreate: 初始化完成");
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "ExpenseProvider query: " + uri.toString());
        
        // 获取可读数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        // 匹配URI
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSES:
                // 查询所有消费记录，按日期降序排序
                if (sortOrder == null) {
                    sortOrder = ExpenseContract.ExpenseEntry.COLUMN_DATE + " DESC";
                }
                cursor = db.query(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                );
                break;
                
            case EXPENSE_ID:
                // 查询单个消费记录
                String idString = uri.getLastPathSegment();
                String whereClause = ExpenseContract.ExpenseEntry._ID + " = ?";
                String[] whereArgs = {idString};
                cursor = db.query(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,
                    projection,
                    whereClause,
                    whereArgs,
                    null,
                    null,
                    null
                );
                break;
                
            default:
                Log.e(TAG, "Unknown URI: " + uri);
        }
        
        // 设置通知URI，以便数据变化时通知监听器
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }
    
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSES:
                return ExpenseContract.ExpenseEntry.CONTENT_LIST_TYPE;
            case EXPENSE_ID:
                return ExpenseContract.ExpenseEntry.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "ExpenseProvider insert: " + uri.toString());
        
        int match = sUriMatcher.match(uri);
        if (match == EXPENSES) {
            // 获取可写数据库
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            // 插入数据
            long newId = db.insert(ExpenseContract.ExpenseEntry.TABLE_NAME, null, values);
            
            if (newId > 0) {
                // 生成新的URI
                Uri newUri = Uri.withAppendedPath(ExpenseContract.ExpenseEntry.CONTENT_URI, String.valueOf(newId));
                
                // 通知数据变化
                getContext().getContentResolver().notifyChange(uri, null);
                
                Log.d(TAG, "Inserted new expense with ID: " + newId);
                return newUri;
            }
        }
        
        Log.e(TAG, "Insert not supported for URI: " + uri);
        return null;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "ExpenseProvider delete: " + uri.toString());
        
        // 获取可写数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedCount = 0;
        
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSE_ID:
                // 删除单个记录
                String idString = uri.getLastPathSegment();
                String whereClause = ExpenseContract.ExpenseEntry._ID + " = ?";
                String[] whereArgs = {idString};
                deletedCount = db.delete(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,
                    whereClause,
                    whereArgs
                );
                // 通知数据变化
                if (deletedCount > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
                
            default:
                Log.e(TAG, "Delete not supported for URI: " + uri);
        }
        
        Log.d(TAG, "Deleted " + deletedCount + " records");
        return deletedCount;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "ExpenseProvider update: " + uri.toString());
        
        // 获取可写数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedCount = 0;
        
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSE_ID:
                // 更新单个记录
                String idString = uri.getLastPathSegment();
                String whereClause = ExpenseContract.ExpenseEntry._ID + " = ?";
                String[] whereArgs = {idString};
                updatedCount = db.update(
                    ExpenseContract.ExpenseEntry.TABLE_NAME,
                    values,
                    whereClause,
                    whereArgs
                );
                // 通知数据变化
                if (updatedCount > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
                
            default:
                Log.e(TAG, "Update not supported for URI: " + uri);
        }
        
        Log.d(TAG, "Updated " + updatedCount + " records");
        return updatedCount;
    }
}
