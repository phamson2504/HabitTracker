package com.example.habittracker.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "Habit.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_HABIT = "habits"
        const val COLUMN_HABIT_ID = "id"
        const val COLUMN_NAME_HABIT = "name"
        const val COLUMN_HABIT_DESCRIPTION = "description"

        const val TABLE_SCHEDULE = "schedule"
        const val COLUMN_SCHEDULE_ID = "id"
        const val COLUMN_SCHEDULE_TYPE = "schedule_type"
        const val COLUMN_SCHEDULE_TIME_FOR_HABIT = "time"
        const val COLUMN_SCHEDULE_TIME_REMINDS = "time_reminds"
        const val COLUMN_SCHEDULE_NUM_OF_TIMES = "num_of_time"
        const val COLUMN_SCHEDULE_START_DATE = "start_date"
        const val COLUMN_SCHEDULE_DUE_DATE= "due_date"
        const val COLUMN_SCHEDULE_DAYS_IN_WEEK = "days_in_week"
        const val COLUMN_SCHEDULE_DATE_IN_MONTH = "date_in_month"
        const val COLUMN_SCHEDULE_NUM_OF_WEEK_REPEAT = "num_of_week_repeat"
        const val COLUMN_SCHEDULE_NUM_OF_MONTH_REPEAT = "num_of_month_repeat"
        const val COlUMN_SCHEDULE_REPEAT_INFINITELY = "repeat_infinitely"
        const val COLUMN_SCHEDULE_HABIT_ID = "habit_id"

        const val TABLE_HISTORY = "history"
        const val COLUMN_HISTORY_ID = "id"
        const val COLUMN_HISTORY_DATE = "date"
        const val  COLUMN_HISTORY_IS_COMPLETED = "is_completed"
        const val COLUMN_HISTORY_TIME_FOR_HABIT = "time"
        const val COLUMN_HISTORY_TIMES_COMPLETED = "times_complete"
        const val COLUMN_HISTORY_HABIT_ID = "habit_id"

        private const val CREATE_TABLE_HABIT =
            "CREATE TABLE " + TABLE_HABIT + " (" +
                    COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME_HABIT + " TEXT NOT NULL, " +
                    COLUMN_HABIT_DESCRIPTION + " TEXT)"

        private const val CREATE_TABLE_SCHEDULE =
            "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                    COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SCHEDULE_TYPE + " TEXT, "+
                    COLUMN_SCHEDULE_TIME_FOR_HABIT + " INTEGER, "+
                    COLUMN_SCHEDULE_TIME_REMINDS +" TEXT, " +
                    COLUMN_SCHEDULE_NUM_OF_TIMES + " INTEGER, "+
                    COLUMN_SCHEDULE_START_DATE + " TEXT NOT NULL, "+
                    COLUMN_SCHEDULE_DUE_DATE + " TEXT NOT NULL, "+
                    COLUMN_SCHEDULE_DAYS_IN_WEEK + " TEXT, "+
                    COLUMN_SCHEDULE_DATE_IN_MONTH + " TEXT, "+
                    COLUMN_SCHEDULE_NUM_OF_WEEK_REPEAT + " INTEGER, "+
                    COLUMN_SCHEDULE_NUM_OF_MONTH_REPEAT + " INTEGER, "+
                    COlUMN_SCHEDULE_REPEAT_INFINITELY + " INTEGER, "+
                    COLUMN_SCHEDULE_HABIT_ID + " INTEGER NOT NULL)"

        private const val CREATE_TABLE_HISTORY =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HISTORY_DATE + " TEXT NOT NULL, " +
                    COLUMN_HISTORY_TIME_FOR_HABIT + " INTEGER, "+
                    COLUMN_HISTORY_TIMES_COMPLETED + " INTEGER, " +
                    COLUMN_HISTORY_IS_COMPLETED + " INTEGER, " +
                    COLUMN_HISTORY_HABIT_ID + " INTEGER NOT NULL )"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_HABIT)
        db?.execSQL(CREATE_TABLE_SCHEDULE)
        db?.execSQL(CREATE_TABLE_HISTORY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HABIT")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        // Create tables again
        onCreate(db)
    }
    fun resetDatabase() {
//        writableDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_HABIT")
//        writableDatabase?.execSQL(CREATE_TABLE_HABIT)
//        writableDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULE")
//        writableDatabase?.execSQL(CREATE_TABLE_SCHEDULE)
        writableDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        writableDatabase?.execSQL(CREATE_TABLE_HISTORY)
    }
}