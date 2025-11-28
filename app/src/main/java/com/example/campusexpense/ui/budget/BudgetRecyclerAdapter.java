package com.example.campusexpense.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpense.R;
import com.example.campusexpense.data.model.Budget;
import com.example.campusexpense.ui.category.CategoryRecyclerAdapter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetRecyclerAdapter extends RecyclerView.Adapter<BudgetRecyclerAdapter.ViewHolder> {
    private List<Budget> budgetList;
    private List<String> categoryNames;

    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Budget budget);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Budget budget);
    }
    @Override
    public int getItemCount() { return budgetList.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText;
        TextView periodText;
        TextView amountText;
        ProgressBar progressBar;
        TextView progressText;
        ImageButton editButton;
        ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            periodText = itemView.findViewById(R.id.periodText);
            amountText = itemView.findViewById(R.id.amountText);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
    public BudgetRecyclerAdapter(List<Budget> budgetList, List<String> categoryNames,
                                 OnEditClickListener onEditClickListener, OnDeleteClickListener onDeleteClickListener){
        this.budgetList = budgetList;
        this.categoryNames = categoryNames;
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_card, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String categoryName = (position < categoryNames.size()) ? categoryNames.get(position) : "Unknown";
        holder.categoryNameText.setText(categoryName);
        holder.periodText.setText(budget.getPeriod());
        holder.amountText.setText(currencyFormat.format(budget.getAmount()));
        double spent = 0.0;
        double percentage = budget.getAmount() > 0 ? (spent / budget.getAmount()) * 100 : 0;
        int progress = (int) Math.min(Math.max(percentage, 0), 100);
        holder.progressBar.setProgress(progress);
        holder.progressText.setText(String.format(Locale.getDefault(), "%.0f%% used", percentage));
        holder.editButton.setOnClickListener(v -> onEditClickListener.onEditClick(budget));
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(budget));
    }
}
