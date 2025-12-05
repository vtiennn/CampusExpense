package com.example.campusexpense.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.campusexpense.data.model.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);
    @Update
    void update(Budget budget);
    @Delete
    void delete(Budget budget);
    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY createdAt DESC")
    List<Budget> getAllBudgetsByUser(int userId);
    @Query("SELECT * FROM budgets WHERE userId = :userid AND categoryId = :categoryId")
    Budget getBudgetByCategoryAndUser(int userid, int categoryId);
    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBudgetById(int id);
}