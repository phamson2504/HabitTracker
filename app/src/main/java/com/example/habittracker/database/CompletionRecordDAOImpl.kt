package com.example.habittracker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.Data
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.CompletionRecord
import com.example.habittracker.model.Habit
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class CompletionRecordDAOImpl(private val context: Context, private val dbHelper: DatabaseHelper) : CompletionRecordDAO{

    override fun insertCompletionRecord(completionRecord: CompletionRecord): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HISTORY_DATE, completionRecord.date)
            put(DatabaseHelper.COLUMN_HISTORY_TIMES_COMPLETED, completionRecord.numOfTimesCompleted)
            put(DatabaseHelper.COLUMN_HISTORY_TIME_FOR_HABIT, completionRecord.timeForHabit)
            put(DatabaseHelper.COLUMN_HISTORY_IS_COMPLETED, completionRecord.isCompleted)
            put(DatabaseHelper.COLUMN_HISTORY_HABIT_ID, completionRecord.habitId)
            put(DatabaseHelper.COLUMN_HISTORY_CURRENT_OPERATIONAL_STATUS, completionRecord.currentOperationalStatus)
        }
        return db.insertOrThrow(DatabaseHelper.TABLE_HISTORY, null, values)
    }

    override fun getCompletionRecordByHabitId(habitId: String, date: LocalDate): CompletionRecord? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectDate = date.format(formatter)

        val db = dbHelper.readableDatabase
        val sql = """SELECT * 
            FROM ${DatabaseHelper.TABLE_HISTORY} 
            WHERE ${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} = :habitId
            AND ${DatabaseHelper.COLUMN_HISTORY_DATE} = :selectDate"""
        val cursor = db.rawQuery(sql, arrayOf(habitId, selectDate))
        return if (cursor.moveToFirst()) {
            cursorCompletionRecord(cursor)
        } else {
            null
        }
    }

    override fun getCompletionRecordByHabitId(habitId: String): MutableList<CompletionRecord> {
        val db = dbHelper.readableDatabase
        val sql = """SELECT * 
            FROM ${DatabaseHelper.TABLE_HISTORY} 
            WHERE ${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} = :habitId
             AND ${DatabaseHelper.COLUMN_HISTORY_CURRENT_OPERATIONAL_STATUS} = 1"""
        val cursor = db.rawQuery(sql, arrayOf(habitId))
        val completionRecords = ArrayList<CompletionRecord>()
        if (cursor.moveToFirst()) {
            do {
                completionRecords.add(cursorCompletionRecord(cursor))
            }while (cursor.moveToNext())

        }
        cursor.close()
        return completionRecords
    }

    override fun getCompletionRecordInMonthByHabitId(habitId: String, date: LocalDate): ArrayList<CompletionRecord> {
        val monthFormatter = DateTimeFormatter.ofPattern("MM")
        val month = date.format(monthFormatter)

        val db = dbHelper.readableDatabase
        val sql = """SELECT * 
            FROM ${DatabaseHelper.TABLE_HISTORY} 
            WHERE ${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} = :habitId
            AND strftime('%m', ${DatabaseHelper.COLUMN_HISTORY_DATE}) = :month
            AND ${DatabaseHelper.COLUMN_HISTORY_CURRENT_OPERATIONAL_STATUS} = 1"""
        val cursor = db.rawQuery(sql, arrayOf(habitId, month))
        val completionRecords = ArrayList<CompletionRecord>()
        if (cursor.moveToFirst()) {
            do {
                completionRecords.add(cursorCompletionRecord(cursor))
            }while (cursor.moveToNext())

        }
        cursor.close()
        return completionRecords
    }

    override fun getCompletionRecordsByHabits(habits: ArrayList<Habit>, date: LocalDate): List<HabitHandle> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectDate = date.format(formatter)

        val db = dbHelper.readableDatabase

        val habitIds = habits.map { it.id.toString() }

        // Construct the SQL query with placeholders for habit IDs
        val placeholders = habitIds.joinToString(",") { "?" }
        val sql = """SELECT * 
        FROM ${DatabaseHelper.TABLE_HISTORY} 
        WHERE ${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} IN ($placeholders) 
        AND ${DatabaseHelper.COLUMN_HISTORY_DATE} = ?"""

        // Prepare the arguments array
        val args = habitIds.toTypedArray() + selectDate

        val cursor = db.rawQuery(sql, args)

        val completionRecordsMap = mutableMapOf<String, CompletionRecord>()
        if (cursor.moveToFirst()) {
            do {
                val habitId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_HABIT_ID))
                val completionRecord = cursorCompletionRecord(cursor)
                completionRecordsMap[habitId] = completionRecord
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Create a list of HabitHandle, including habits without CompletionRecord
        val habitHandles = habits.map { habit ->
            val completionRecord = completionRecordsMap[habit.id.toString()]
            HabitHandle(habit, completionRecord)
        }
        return habitHandles
    }

    override fun deleteCompletionRecordById(habitId: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_HISTORY,
            "${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} = ?",
            arrayOf(habitId)
        )
    }

    override fun updateActiveCompletion(completionRecord: CompletionRecord): Int {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HISTORY_CURRENT_OPERATIONAL_STATUS, completionRecord.currentOperationalStatus)
        }
        val whereClause = "${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} = ? AND ${DatabaseHelper.COLUMN_HISTORY_DATE} =  ?"

        val whereArgs = arrayOf(completionRecord.habitId.toString(), completionRecord.date)

        return db.update(DatabaseHelper.TABLE_HISTORY, contentValues, whereClause, whereArgs)
    }

    override fun updateCompletionRecord(completionRecord: CompletionRecord, date: LocalDate): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectDate = date.format(formatter)

        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HISTORY_TIMES_COMPLETED, completionRecord.numOfTimesCompleted)
            put(DatabaseHelper.COLUMN_HISTORY_TIME_FOR_HABIT, completionRecord.timeForHabit)
            put(DatabaseHelper.COLUMN_HISTORY_IS_COMPLETED, completionRecord.isCompleted)
        }
        val whereClause = "${DatabaseHelper.COLUMN_HISTORY_HABIT_ID} = ? AND ${DatabaseHelper.COLUMN_HISTORY_DATE} =  ?"

        val whereArgs = arrayOf(completionRecord.habitId.toString(),selectDate)

        return db.update(DatabaseHelper.TABLE_HISTORY, contentValues, whereClause, whereArgs)
    }

    private fun cursorCompletionRecord(cursor: Cursor): CompletionRecord {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_ID))
        val date =  cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_DATE))
        val numOfTimesCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_TIMES_COMPLETED))
        val timeForHabit = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_TIME_FOR_HABIT))
        val isCompleted =cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_IS_COMPLETED))
        val habitId =cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_HABIT_ID))
        val currentOperationalStatus =cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HISTORY_CURRENT_OPERATIONAL_STATUS))
        return CompletionRecord(date,numOfTimesCompleted, timeForHabit, isCompleted, habitId, currentOperationalStatus)
    }
}