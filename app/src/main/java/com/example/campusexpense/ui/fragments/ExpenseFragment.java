package com.example.campusexpense.ui.fragments;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusexpense.R;
import com.example.campusexpense.data.database.AppDatabase;
import com.example.campusexpense.data.database.BudgetDao;
import com.example.campusexpense.data.database.CategoryDao;
import com.example.campusexpense.data.database.ExpenseDao;
import com.example.campusexpense.data.model.Budget;
import com.example.campusexpense.data.model.Category;
import com.example.campusexpense.data.model.Expense;
import com.example.campusexpense.ui.expense.CategoryExpenseAdapter;
import com.example.campusexpense.ui.expense.ExpenseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private TabLayout tabLayout;
    private Spinner monthSpinner;
    private Spinner categoryFilterSpinner;
    private TextView totalExpenseText;
    private TextView expenseCountText;
    private TextView emptyView;

    private ExpenseDao expenseDao;
    private CategoryDao categoryDao;
    private BudgetDao budgetDao;
    private SharedPreferences sharedPreferences;
    private int currentUserId;

    private CategoryExpenseAdapter categoryAdapter;
    private ExpenseRecyclerAdapter expenseAdapter;

    private List<Category> categoryList;
    private List<CategoryExpenseAdapter.CategoryExpenseItem> categoryExpenseList;
    private List<Expense> expenseList;

    private int currentMonth;
    private int currentYear;
    private int selectedCategoryId = -1;
    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);
        tabLayout = view.findViewById(R.id.tabLayout);
        monthSpinner = view.findViewById(R.id.monthSpinner);
        categoryFilterSpinner = view.findViewById(R.id.categoryFilterSpinner);
        totalExpenseText = view.findViewById(R.id.totalExpenseText);
        expenseCountText = view.findViewById(R.id.expenseCountText);
        emptyView = view.findViewById(R.id.emptyView);

        sharedPreferences = requireContext().getSharedPreferences("UserSession", 0);
        currentUserId = sharedPreferences.getInt("userId", -1);

        AppDatabase database = AppDatabase.getInstance(requireContext());
        expenseDao = database.expenseDao();
        categoryDao = database.categoryDao();
        budgetDao = database.budgetDao();

        categoryList = new ArrayList<>();
        categoryExpenseList = new ArrayList<>();
        expenseList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        setupTabs();
        setupSpinners();
        setupRecyclerView();

        fabAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData(); // Refresh data every time fragment is viewed
    }

    private void setupTabs() {
        if (tabLayout.getTabCount() == 0) { // Prevent adding tabs multiple times
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_by_category));
            tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_by_date));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    currentTab = tab.getPosition();
                    refreshData();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void setupSpinners() {
        Calendar calendar = Calendar.getInstance();
        List<String> months = new ArrayList<>();
        int currentMonthIndex = calendar.get(Calendar.MONTH);
        int currentYearValue = calendar.get(Calendar.YEAR);

        for (int i = -6; i <= 6; i++) {
            calendar.set(currentYearValue, currentMonthIndex + i, 1);
            months.add(new SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.getTime()));
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(6);
        monthSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                cal.set(currentYearValue, currentMonthIndex + (position - 6), 1);
                currentMonth = cal.get(Calendar.MONTH);
                currentYear = cal.get(Calendar.YEAR);
                refreshData();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // This part needs to be in refreshData to get latest categories

        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.all_categories));
        categoryList.clear();
        categoryList.addAll(categoryDao.getAll());
        for (Category cat : categoryList) {
            categoryNames.add(cat.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
        categoryFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCategoryId = -1;
                } else {
                    selectedCategoryId = categoryList.get(position - 1).getId();
                }
                refreshData();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        categoryAdapter = new CategoryExpenseAdapter(categoryExpenseList, (categoryId, categoryName) -> {
            showCategoryExpensesDialog(categoryId, categoryName);
        });
        categoryAdapter.setContext(requireContext());

        expenseAdapter = new ExpenseRecyclerAdapter(expenseList, categoryList,
                expense -> showEditDialog(expense),
                expense -> showDeleteDialog(expense));
        expenseAdapter.setContext(requireContext());
    }

    private void refreshData() {
        // Refresh category spinner every time
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add(getString(R.string.all_categories));
        List<Category> currentCategories = categoryDao.getAll();
        for (Category cat : currentCategories) {
            categoryNames.add(cat.getName());
        }
        ArrayAdapter<String> categoryAdapterSpinner = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapterSpinner);


        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startDate = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endDate = calendar.getTimeInMillis();

        if (currentTab == 0) {
            refreshCategoryData(startDate, endDate);
        } else {
            refreshDateData(startDate, endDate);
        }
    }

    private void refreshCategoryData(long startDate, long endDate) {
        categoryList.clear();
        categoryList.addAll(categoryDao.getAll());
        categoryExpenseList.clear();

        List<Category> categoriesToShow = new ArrayList<>();
        if (selectedCategoryId == -1) {
            categoriesToShow.addAll(categoryList);
        } else {
            for (Category cat : categoryList) {
                if (cat.getId() == selectedCategoryId) {
                    categoriesToShow.add(cat);
                    break;
                }
            }
        }

        int totalCount = 0;
        for (Category category : categoriesToShow) {
            List<Expense> expenses = expenseDao.getExpensesByCategoryAndDateRange(currentUserId, category.getId(), startDate, endDate);
            double totalExpense = 0;
            for(Expense e : expenses) totalExpense += e.getAmount();

            Budget budget = budgetDao.getBudgetByCategoryAndUser(currentUserId, category.getId());

            // Show a category if it has expenses OR a budget is set for it
            if (totalExpense > 0 || budget != null) {
                totalCount += expenses.size();
                categoryExpenseList.add(new CategoryExpenseAdapter.CategoryExpenseItem(
                        category.getId(),
                        category.getName(),
                        totalExpense,
                        expenses.size(),
                        budget
                ));
            }
        }

        categoryAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(categoryAdapter);

        updateStatistics(startDate, endDate, totalCount);
        updateEmptyView();
    }

    private void refreshDateData(long startDate, long endDate) {
        expenseList.clear();
        if (selectedCategoryId == -1) {
            expenseList.addAll(expenseDao.getExpensesByDateRange(currentUserId, startDate, endDate));
        } else {
            expenseList.addAll(expenseDao.getExpensesByCategoryAndDateRange(currentUserId, selectedCategoryId, startDate, endDate));
        }

        categoryList.clear();
        categoryList.addAll(categoryDao.getAll());
        expenseAdapter.setCategoryList(categoryList);

        expenseAdapter.updateExpenses(expenseList);
        recyclerView.setAdapter(expenseAdapter);

        updateStatistics(startDate, endDate, expenseList.size());
        updateEmptyView();
    }

    private void updateStatistics(long startDate, long endDate, int count) {
        Double total = selectedCategoryId == -1 ?
                expenseDao.getTotalExpensesByDateRange(currentUserId, startDate, endDate) :
                expenseDao.getTotalExpensesByCategoryAndDateRange(currentUserId, selectedCategoryId, startDate, endDate);

        double totalExpense = total != null ? total : 0.0;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        totalExpenseText.setText(currencyFormat.format(totalExpense));
        expenseCountText.setText(getResources().getQuantityString(R.plurals.transaction_count, count, count));
    }

    private void updateEmptyView() {
        boolean isEmpty = (currentTab == 0 && categoryExpenseList.isEmpty()) ||
                (currentTab == 1 && expenseList.isEmpty());

        if (isEmpty) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddDialog() {
        // ... (code is correct and does not need to be changed)
    }

    private void showEditDialog(Expense expense) {
        // ... (code is correct and does not need to be changed)
    }

    private void showDeleteDialog(Expense expense) {
        // ... (code is correct and does not need to be changed)
    }

    private void showCategoryExpensesDialog(int categoryId, String categoryName) {
        // ... (code is correct and does not need to be changed)
    }
}
