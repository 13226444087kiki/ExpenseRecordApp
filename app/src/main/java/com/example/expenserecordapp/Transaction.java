package com.example.expenserecordapp;

import java.io.Serializable;

/**
 * 消费记录实体类
 * 用于存储单条消费记录的信息
 */
public class Transaction implements Serializable {

    // 字段定义
    private double amount;      // 金额
    private String type;        // 类型（收入/支出）
    private String category;    // 类别
    private String note;        // 备注信息
    private String date;        // 日期

    // 默认构造方法
    public Transaction() {
        // 空构造方法，用于Firebase等需要默认构造方法的框架
    }

    /**
     * 带参数的构造方法
     * @param amount 金额
     * @param category 类别
     * @param note 备注信息
     * @param date 日期
     */
    public Transaction(double amount, String category, String note, String date) {
        this.amount = amount;
        this.type = "支出";
        this.category = category;
        this.note = note;
        this.date = date;
    }

    /**
     * 带参数的构造方法
     * @param amount 金额
     * @param type 类型
     * @param category 类别
     * @param note 备注信息
     * @param date 日期
     */
    public Transaction(double amount, String type, String category, String note, String date) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.date = date;
    }

    // Getter 和 Setter 方法

    /**
     * 获取金额
     * @return 金额
     */
    public double getAmount() {
        return amount;
    }

    /**
     * 设置金额
     * @param amount 金额
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * 获取类型
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置类型
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取类别
     * @return 类别
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置类别
     * @param category 类别
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取备注信息
     * @return 备注信息
     */
    public String getNote() {
        return note;
    }

    /**
     * 设置备注信息
     * @param note 备注信息
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * 获取日期
     * @return 日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 设置日期
     * @param date 日期
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 重写toString方法，方便调试
     * @return 对象的字符串表示
     */
    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    /**
     * 重写equals方法，用于比较两个Transaction对象是否相等
     * @param obj 要比较的对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Transaction that = (Transaction) obj;

        if (Double.compare(that.amount, amount) != 0) return false;
        if (!type.equals(that.type)) return false;
        if (!category.equals(that.category)) return false;
        if (!note.equals(that.note)) return false;
        return date.equals(that.date);
    }

    /**
     * 重写hashCode方法
     * @return 对象的哈希值
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(amount);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + type.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + note.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }
}