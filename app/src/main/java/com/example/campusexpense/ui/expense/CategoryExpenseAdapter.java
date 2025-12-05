package com.example.campusexpense.ui.expense;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import com.example.campusexpense.R;
import com.example.campusexpense.data.model.Budget;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;


public class CategoryExpenseAdapter extends RecyclerView.Adapter<CategoryExpenseAdapter.ViewHolder> {

    private List<CategoryExpenseItem> categoryExpenseList;
    private OnCategoryClickListener onCategoryClickListener;
    private Context context;

    public interface OnCategoryClickListener {
        void onCategoryClick(int categoryId, String categoryName);
    }

    public static class CategoryExpenseItem {
        public int categoryId;
        public String categoryName;
        public double totalExpense;
        public int expenseCount;
        public Budget budget;

        public CategoryExpenseItem(int categoryId, String categoryName, double totalExpense, int expenseCount, Budget budget) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalExpense = totalExpense;
            this.expenseCount = expenseCount;
            this.budget = budget;
        }
    }

    public CategoryExpenseAdapter(List<CategoryExpenseItem> categoryExpenseList, OnCategoryClickListener onCategoryClickListener) {
        this.categoryExpenseList = categoryExpenseList;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryExpenseItem item = categoryExpenseList.get(position);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

        holder.categoryNameText.setText(item.categoryName);
        holder.expenseAmountText.setText(currencyFormat.format(item.totalExpense));
        holder.expenseCountText.setText(context.getResources().getQuantityString(R.plurals.transaction_count, item.expenseCount, item.expenseCount));

        if (item.budget != null && item.budget.getAmount() > 0) {
            holder.budgetLayout.setVisibility(View.VISIBLE);
            holder.budgetAmountText.setText(currencyFormat.format(item.budget.getAmount()));

            double percentage = (item.totalExpense / item.budget.getAmount()) * 100;
            int progress = (int) Math.min(percentage, 100); // Cap progress at 100
            holder.progressBar.setProgress(progress);

            holder.progressText.setText(String.format(Locale.US, "%.0f%%", Math.min(percentage, 100.0)));

            if (percentage > 100) {
                holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFD32F2F)); // Red
                holder.progressText.setTextColor(0xFFD32F2F);
            } else if (percentage > 80) {
                holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800)); // Orange
                holder.progressText.setTextColor(0xFF757575);
            } else {
                holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Green
                holder.progressText.setTextColor(0xFF757575);
            }
        } else {
            holder.budgetLayout.setVisibility(View.GONE);
            holder.progressBar.setProgress(0);
            holder.progressText.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (onCategoryClickListener != null) {
                onCategoryClickListener.onCategoryClick(item.categoryId, item.categoryName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryExpenseList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText;
        TextView expenseAmountText;
        TextView expenseCountText;
        LinearLayout budgetLayout;
        TextView budgetAmountText;
        ProgressBar progressBar;
        TextView progressText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.categoryNameText);
            expenseAmountText = itemView.findViewById(R.id.expenseAmountText);
            expenseCountText = itemView.findViewById(R.id.expenseCountText);
            budgetLayout = itemView.findViewById(R.id.budgetLayout);
            budgetAmountText = itemView.findViewById(R.id.budgetAmountText);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
        }
    }
}
