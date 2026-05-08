package com.example.expenserecordapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenserecordapp.R;
import com.example.expenserecordapp.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private OnItemDeleteListener onItemDeleteListener;

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < transactions.size()) {
            transactions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, transactions.size());
        }
    }

    public interface OnItemDeleteListener {
        void onItemDelete(int position);
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvAmount;
        private final TextView tvDate;
        private final TextView tvNote;
        private final TextView tvType;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvType = itemView.findViewById(R.id.tv_type);
        }

        public void bind(Transaction transaction) {
            tvCategory.setText(transaction.getCategory());
            tvAmount.setText(String.format("¥ %.2f", transaction.getAmount()));
            tvDate.setText(transaction.getDate());
            tvType.setText(transaction.getType());

            // 根据类型设置颜色
            if ("收入".equals(transaction.getType())) {
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                tvType.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                tvType.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
                tvNote.setText(transaction.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }
    }
}