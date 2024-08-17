package com.example.habittracker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class HabitDAOImpl(private val context: Context, private val dbHelper: DatabaseHelper) : HabitDAO {

    override fun insertHabit(habit: Habit, onError: (Exception) -> Unit): Boolean {
        val db = dbHelper.writableDatabase
        var success = false

        db.beginTransaction()
        try {
            val valuesHabit = ContentValues().apply {
                put(DatabaseHelper.COLUMN_NAME_HABIT, habit.name)
                put(DatabaseHelper.COLUMN_HABIT_DESCRIPTION, habit.description)
            }
            val habitId = db.insertOrThrow(DatabaseHelper.TABLE_HABIT, null, valuesHabit)

            val scheduleDAO = ScheduleDAOImpl(context, dbHelper)
            if (habitId != -1L) {
                habit.schedule.let { schedule ->
                    schedule?.habitId = habitId
                    if (schedule != null) {
                        scheduleDAO.insertSchedule(schedule)
                    }
                }
            }
            db.setTransactionSuccessful()
            success = true
        } catch (e: Exception) {
            onError(e)
        } finally {
            db.endTransaction()
        }
        return success
    }

    override fun getHabit(id: Long): Habit? {
//        val db = dbHelper.readableDatabase
        TODO("Not yet implemented")
    }

    override fun getAllHabits(): List<Habit> {
        val db = dbHelper.writableDatabase
        val listHabit = mutableListOf<Habit>()
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_HABIT}", null)
        while (cursor.moveToNext()) {
            listHabit.add(cursorToHabit(cursor))
        }
        return listHabit
    }

    fun getHabitsForToday(date: LocalDate): List<Habit> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val selectDate = date.format(formatter)

        // Retrieve records from the database
        val habits = getHabitsByDate(selectDate)

        // Filter habits by the current day of the week
        val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE"))
        val dayOfMonth =  date.format(DateTimeFormatter.ofPattern("dd"))

        return filterHabitsByDayOfWeek(habits, dayOfWeek, dayOfMonth)
    }

    override fun getHabitsByDate(selectDate: String): List<Habit> {
        val db = dbHelper.writableDatabase
        val listHabit = mutableListOf<Habit>()
        val query = """
            SELECT h.* FROM ${DatabaseHelper.TABLE_HABIT} AS h 
            INNER JOIN ${DatabaseHelper.TABLE_SCHEDULE} AS s 
            ON h.${DatabaseHelper.COLUMN_HABIT_ID} = s.${DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID} 
            WHERE (date(:selectDate) BETWEEN date(s.${DatabaseHelper.COLUMN_SCHEDULE_START_DATE}) AND date(s.${DatabaseHelper.COLUMN_SCHEDULE_DUE_DATE})) 
            OR (date(:selectDate) >= date(s.${DatabaseHelper.COLUMN_SCHEDULE_START_DATE}) AND s.${DatabaseHelper.COlUMN_SCHEDULE_REPEAT_INFINITELY} = 1)
        """
        val cursor = db.rawQuery(query, arrayOf(selectDate))
        while (cursor.moveToNext()) {
            listHabit.add(cursorToHabit(cursor))
        }
        return listHabit
    }

    private fun cursorToHabit(cursor: Cursor): Habit {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HABIT_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_HABIT))
        val description =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HABIT_DESCRIPTION))
        var schedule: Schedule? = null
        val scheduleDAO = ScheduleDAOImpl(context, dbHelper)
        if (id != -1L) {
            schedule = scheduleDAO.getScheduleByIdHabit(idHabit = id)
        }
        return Habit(id, name, description, schedule = schedule)
    }

    override fun updateHabit(habit: Habit): Int {
        TODO("Not yet implemented")
    }

    override fun deleteHabit(id: Long): Int {
        TODO("Not yet implemented")
    }

    private fun filterHabitsByDayOfWeek(habits: List<Habit>, currentDayOfWeek: String, currentDay : String): List<Habit> {
        val filteredHabits = mutableListOf<Habit>()

        for (habit in habits) {
            val schedule = habit.schedule
            if (schedule is Schedule.WeeklySchedule) {
                val daysInWeek = schedule.daysInWeek ?: emptyList()
                if (daysInWeek.contains(currentDayOfWeek)) {
                    filteredHabits.add(habit)
                }
            } else if (schedule is Schedule.ScheduleEveryDayRepeat) {
                filteredHabits.add(habit)
            } else if (schedule is Schedule.MonthlySchedule){
                val daysInMonth = schedule.dateInMonth ?: emptyList()
                if (daysInMonth.contains(currentDay)){
                    filteredHabits.add(habit)
                }
            } else {
                filteredHabits.add(habit)
            }
        }
        return filteredHabits
    }
}