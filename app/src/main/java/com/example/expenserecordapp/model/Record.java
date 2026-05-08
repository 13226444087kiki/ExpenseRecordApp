package com.example.expenserecordapp.model;

/**
 * 记账记录数据模型类
 * 用于教学演示 RecyclerView 的循环复用机制
 * 体现单一职责原则：只负责数据存储
 */
public class Record {

    // 记录类型枚举
    public enum RecordType {
        INCOME("收入", android.R.color.holo_green_light),
        EXPENSE("支出", android.R.color.holo_red_light);

        private final String displayName;
        private final int colorResId;

        RecordType(String displayName, int colorResId) {
            this.displayName = displayName;
            this.colorResId = colorResId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getColorResId() {
            return colorResId;
        }
    }

    // 消费类别枚举
    public enum Category {
        FOOD("餐饮", android.R.drawable.ic_menu_compass),
        TRANSPORT("交通", android.R.drawable.ic_menu_mapmode),
        SHOPPING("购物", android.R.drawable.ic_menu_share),
        ENTERTAINMENT("娱乐", android.R.drawable.ic_menu_camera),
        SALARY("工资", android.R.drawable.ic_menu_agenda),
        OTHER("其他", android.R.drawable.ic_menu_more);

        private final String displayName;
        private final int iconResId;

        Category(String displayName, int iconResId) {
            this.displayName = displayName;
            this.iconResId = iconResId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    // 字段定义
    private int id;                 // 记录ID
    private String title;           // 记录标题
    private double amount;          // 金额
    private String date;            // 日期
    private RecordType type;        // 收入/支出类型
    private Category category;      // 消费类别
    private String description;     // 详细描述

    // 构造方法
    public Record(int id, String title, double amount, String date,
                  RecordType type, Category category, String description) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
    }

    // Getter 方法
    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public RecordType getType() { return type; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }

    // 格式化金额显示
    public String getFormattedAmount() {
        return String.format("%s¥ %.2f",
            type == RecordType.INCOME ? "+" : "-",
            Math.abs(amount));
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", type=" + type +
                ", category=" + category +
                ", description='" + description + '\'' +
                '}';
    }
}