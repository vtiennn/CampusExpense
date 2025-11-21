package com.example.campusexpense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.campusexpense.ui.auth.LoginActivity;
import com.example.campusexpense.ui.fragments.AccountFragment;
import com.example.campusexpense.ui.fragments.AddFragment;
import com.example.campusexpense.ui.fragments.BudgetFragment;
import com.example.campusexpense.ui.fragments.CategoryFragment;
import com.example.campusexpense.ui.fragments.HomeFragment;
import com.example.campusexpense.ui.fragments.TransactionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private BottomNavigationView bottomNavigation;
    private boolean isLoggedIn(){
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
    private void goToLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        if (!isLoggedIn()){
            goToLoginActivity();
        }
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.nav_transaction) {
                        selectedFragment = new TransactionFragment();
                    } else if (itemId == R.id.nav_account) {
                        selectedFragment = new AccountFragment();
                    } else if (itemId == R.id.nav_budget) {
                        selectedFragment = new BudgetFragment();
                    } else if (itemId == R.id.nav_add) {
                        selectedFragment = new AddFragment();
                    }
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }
                    return true;
                    });
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void navigateToCategoryFragment() {
        CategoryFragment categoryFragment = new CategoryFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, categoryFragment)
                .addToBackStack(null)
                .commit();
    }
}