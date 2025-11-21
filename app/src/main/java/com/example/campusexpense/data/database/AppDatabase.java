package com.example.campusexpense.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.campusexpense.data.model.Category;
import com.example.campusexpense.data.model.User;

@Database(entities = {User.class, Category.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    private static final String DATABASE_NAME = "campus.expense.db";

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
