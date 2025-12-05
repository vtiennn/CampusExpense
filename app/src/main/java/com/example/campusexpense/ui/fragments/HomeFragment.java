package com.example.campusexpense.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpense.R;
import com.example.campusexpense.data.database.AppDatabase;
import com.example.campusexpense.data.database.BudgetDao;
import com.example.campusexpense.data.database.CategoryDao;
import com.example.campusexpense.data.database.ExpenseDao;
import com.example.campusexpense.data.model.Budget;
import com.example.campusexpense.data.model.Category;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView welcomeText, totalExpenseText, expenseMonthText;
    private TextView budgetCategoryText, budgetAmountText, budgetSpentText;
    private TextView transactionsCountText, avgDayText, budgetCountText;
    private TextView summaryTotalBudgetText, summarySpentText, summaryRemainingText;
    private TextView expenseDistributionCategoryText, expenseDistributionAmountText, expenseDistributionPercentageText;
    private ProgressBar budgetProgressBar, expenseDistributionBar;

    private SharedPreferences sharedPreferences;
    private ExpenseDao expenseDao;
    private BudgetDao budgetDao;
    private CategoryDao categoryDao;
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        initializeDatabase();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(); // Refresh data every time the fragment is shown
    }

    private void initializeViews(View view) {
        welcomeText = view.findViewById(R.id.welcomeText);
        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        expenseMonthText = view.findViewById(R.id.expenseMonthText);
        budgetCategoryText = view.findViewById(R.id.budgetCategoryText);
        budgetAmountText = view.findViewById(R.id.budgetAmountText);
        budgetSpentText = view.findViewById(R.id.budgetSpentText);
        transactionsCountText = view.findViewById(R.id.transactionsCountText);
        avgDayText = view.findViewById(R.id.avgDayText);
        budgetCountText = view.findViewById(R.id.budgetCountText);
        summaryTotalBudgetText = view.findViewById(R.id.summaryTotalBudgetText);
        summarySpentText = view.findViewById(R.id.summarySpentText);
        summaryRemainingText = view.findViewById(R.id.summaryRemainingText);
        expenseDistributionCategoryText = view.findViewById(R.id.expenseDistributionCategoryText);
        expenseDistributionAmountText = view.findViewById(R.id.expenseDistributionAmountText);
        expenseDistributionPercentageText = view.findViewById(R.id.expenseDistributionPercentageText);
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar);
        expenseDistributionBar = view.findViewById(R.id.expenseDistributionBar);
    }

    private void initializeDatabase() {
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);
        AppDatabase db = AppDatabase.getInstance(requireContext());
        expenseDao = db.expenseDao();
        budgetDao = db.budgetDao();
        categoryDao = db.categoryDao();
    }

    private void updateUI() {
        // Welcome Text
        String username = sharedPreferences.getString("username", "User");
        welcomeText.setText("Welcome, " + username);

        // --- Correct Date Range for "This Month" --- //
        Calendar calendar = Calendar.getInstance();
        expenseMonthText.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
        // Set to the beginning of the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();

        // Set to the end of the last day of the month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endOfMonth = calendar.getTimeInMillis();

        int daysInMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        // Currency Formatter
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));

        // --- Get Real Data --- //

        // Total Expense
        Double totalExpenseMonth = expenseDao.getTotalExpensesByDateRange(currentUserId, startOfMonth, endOfMonth);
        if (totalExpenseMonth == null) totalExpenseMonth = 0.0;
        totalExpenseText.setText(currencyFormat.format(totalExpenseMonth));

        // Quick Stats
        int transactionsCount = expenseDao.getExpenseCountByDateRange(currentUserId, startOfMonth, endOfMonth);
        transactionsCountText.setText(String.valueOf(transactionsCount));

        double avgPerDay = daysInMonth > 0 ? totalExpenseMonth / daysInMonth : 0.0;
        avgDayText.setText(currencyFormat.format(avgPerDay));

        List<Budget> allBudgets = budgetDao.getAllBudgetsByUser(currentUserId);
        budgetCountText.setText(String.valueOf(allBudgets.size()));

        // Total Monthly Budget
        double totalMonthlyBudget = 0.0;
        for (Budget budget : allBudgets) {
            if ("Monthly".equalsIgnoreCase(budget.getPeriod())) {
                totalMonthlyBudget += budget.getAmount();
            }
        }

        // Featured Budget Card (using the first monthly budget found)
        Budget featuredBudget = null;
        for (Budget budget : allBudgets) {
            if ("Monthly".equalsIgnoreCase(budget.getPeriod())) {
                featuredBudget = budget;
                break;
            }
        }

        if (featuredBudget != null) {
            Category category = categoryDao.getById(featuredBudget.getCategoryId());
            Double spentForCategory = expenseDao.getTotalExpensesByCategoryAndDateRange(currentUserId, featuredBudget.getCategoryId(), startOfMonth, endOfMonth);
            if (spentForCategory == null) spentForCategory = 0.0;

            budgetCategoryText.setText(category != null ? category.getName() : "N/A");
            budgetAmountText.setText("Budget: " + currencyFormat.format(featuredBudget.getAmount()));
            budgetSpentText.setText(currencyFormat.format(spentForCategory));

            int budgetProgress = 0;
            if (featuredBudget.getAmount() > 0) {
                budgetProgress = (int) ((spentForCategory / featuredBudget.getAmount()) * 100);
            }
            budgetProgressBar.setProgress(budgetProgress);

            // Expense Distribution Card (based on the same featured budget)
            expenseDistributionCategoryText.setText(category != null ? category.getName() : "N/A");
            expenseDistributionAmountText.setText(currencyFormat.format(spentForCategory));

            int expenseDistProgress = 0;
            if (totalExpenseMonth > 0) {
                expenseDistProgress = (int) ((spentForCategory / totalExpenseMonth) * 100);
                expenseDistributionPercentageText.setText(String.format(Locale.US, "%.1f%%", (spentForCategory / totalExpenseMonth) * 100));
            } else {
                expenseDistributionPercentageText.setText("0.0%");
            }
            expenseDistributionBar.setProgress(expenseDistProgress);
        }

        // Monthly Summary
        summaryTotalBudgetText.setText(currencyFormat.format(totalMonthlyBudget));
        summarySpentText.setText(currencyFormat.format(totalExpenseMonth));
        summaryRemainingText.setText(currencyFormat.format(totalMonthlyBudget - totalExpenseMonth));
    }
}
