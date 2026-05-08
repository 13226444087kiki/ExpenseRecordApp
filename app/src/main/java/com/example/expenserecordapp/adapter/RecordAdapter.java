package com.example.expenserecordapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenserecordapp.R;
import com.example.expenserecordapp.model.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * 记账记录适配器
 * 体现适配器模式：将数据对象转换为可显示的列表项
 * 体现单一职责原则：只负责数据绑定和视图创建
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<Record> records = new ArrayList<>();
    private OnItemDeleteListener onItemDeleteListener;

    /**
     * 设置数据并刷新列表
     * 体现数据与视图分离的思想
     */
    public void setRecords(List<Record> records) {
        this.records = records;
        notifyDataSetChanged(); // 通知 RecyclerView 数据已更新
    }

    /**
     * 设置删除监听器
     */
    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    /**
     * 移除项目
     */
    public void removeItem(int position) {
        if (position >= 0 && position < records.size()) {
            records.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, records.size());
        }
    }

    /**
     * 创建 ViewHolder
     * 体现 ViewHolder 复用机制：RecyclerView 会复用已创建的 ViewHolder
     * 避免频繁创建和销毁视图，提升性能
     */
    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载列表项布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    /**
     * 绑定数据到 ViewHolder
     * 体现数据与视图的绑定关系
     */
    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = records.get(position);
        holder.bind(record);
    }

    /**
     * 返回数据项数量
     */
    @Override
    public int getItemCount() {
        return records.size();
    }

    /**
     * 删除监听器接口
     */
    public interface OnItemDeleteListener {
        void onItemDelete(int position);
    }

    /**
     * ViewHolder 内部类
     * 体现 ViewHolder 模式：持有视图引用，避免重复调用 findViewById
     * 这是 RecyclerView 性能优化的关键
     */
    static class RecordViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCategory;
        private final TextView tvTitle;
        private final TextView tvAmount;
        private final TextView tvDate;
        private final TextView tvDescription;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            // 初始化视图组件
            // 注意：findViewById 只调用一次，之后直接使用引用
            ivCategory = itemView.findViewById(R.id.iv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        /**
         * 绑定数据到视图
         */
        public void bind(Record record) {
            // 设置类别图标
            ivCategory.setImageResource(record.getCategory().getIconResId());

            // 设置标题
            tvTitle.setText(record.getTitle());

            // 设置金额（带正负号）
            String amountText = String.format("¥%.2f", record.getAmount());
            tvAmount.setText(amountText);

            // 根据收入/支出设置金额颜色
            int amountColor;
            if (record.getType() == Record.RecordType.INCOME) {
                amountColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
            } else {
                amountColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
            }
            tvAmount.setTextColor(amountColor);

            // 设置日期
            tvDate.setText(record.getDate());

            // 设置描述
            if (record.getDescription() != null && !record.getDescription().isEmpty()) {
                tvDescription.setText(record.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
        }
    }
}