package com.example.campusexpense.ui.budget;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpense.R;
import com.example.campusexpense.data.database.ExpenseDao;
import com.example.campusexpense.data.model.Budget;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetRecyclerAdapter extends RecyclerView.Adapter<BudgetRecyclerAdapter.ViewHolder> {
    private List<Budget> budgets;
    private List<String> categoryNames;
    private OnEditClickListener onEditClick;
    private OnDeleteClickListener onDeleteClick;
    private ExpenseDao expenseDao;
    private int userId;

    public interface OnEditClickListener {
        void onEditClick(Budget budget);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Budget budget);
    }

    public BudgetRecyclerAdapter(List<Budget> budgets, List<String> categoryNames, OnEditClickListener onEditClick, OnDeleteClickListener onDeleteClick, ExpenseDao expenseDao, int userId) {
        this.budgets = budgets;
        this.categoryNames = categoryNames;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.expenseDao = expenseDao;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        String categoryName = categoryNames.get(position);
        holder.categoryNameText.setText(categoryName);
        holder.periodText.setText(budget.getPeriod());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        holder.amountText.setText(currencyFormat.format(budget.getAmount()));

        Calendar calendar = Calendar.getInstance();
        long startDate;
        long endDate;

        if ("Weekly".equalsIgnoreCase(budget.getPeriod())) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTimeInMillis();

            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            endDate = calendar.getTimeInMillis();
        } else { // Default to Monthly
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTimeInMillis();

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            endDate = calendar.getTimeInMillis();
        }

        Double spentAmount = expenseDao.getTotalExpensesByCategoryAndDateRange(userId, budget.getCategoryId(), startDate, endDate);
        if (spentAmount == null) {
            spentAmount = 0.0;
        }

        double percentage = 0;
        if (budget.getAmount() > 0) {
            percentage = (spentAmount / budget.getAmount()) * 100;
        }

        int displayPercentage = (int) Math.min(percentage, 100.0);
        holder.progressBar.setProgress(displayPercentage);
        holder.progressText.setText(String.format(Locale.US, "%d%%", displayPercentage));

        if (percentage > 100) {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            holder.progressText.setTextColor(Color.RED);
        } else if (percentage > 80) {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Orange
            holder.progressText.setTextColor(Color.parseColor("#808080")); // Gray
        } else {
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
            holder.progressText.setTextColor(Color.parseColor("#808080")); // Gray
        }

        holder.editButton.setOnClickListener(v -> {
            if (onEditClick != null) {
                onEditClick.onEditClick(budget);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClick != null) {
                onDeleteClick.onDeleteClick(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText, periodText, amountText, progressText;
        ImageButton editButton, deleteButton;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            periodText = itemView.findViewById(R.id.periodText);
            amountText = itemView.findViewById(R.id.amountText);
            progressText = itemView.findViewById(R.id.progressText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
