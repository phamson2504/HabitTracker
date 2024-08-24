package com.example.habittracker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.habittracker.model.Schedule
import com.google.gson.Gson

class ScheduleDAOImpl(private val context: Context, private val dbHelper: DatabaseHelper) :
    ScheduleDAO {

    private val gson = Gson()

    override fun insertSchedule(schedule: Schedule): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SCHEDULE_START_DATE, schedule.startDate)
            put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, schedule.scheduleType)
            put(DatabaseHelper.COLUMN_SCHEDULE_DUE_DATE, schedule.dueDay)
            put(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_TIMES, schedule.numOfTime)
            put(DatabaseHelper.COLUMN_SCHEDULE_TIME_FOR_HABIT, schedule.timeForHabit)
            put(DatabaseHelper.COLUMN_SCHEDULE_TIME_REMINDS, gson.toJson(schedule.timeReminds))
            put(DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID, schedule.habitId)
            when (schedule) {
                is Schedule.WeeklySchedule -> {
                    put(
                        DatabaseHelper.COLUMN_SCHEDULE_DAYS_IN_WEEK,
                        gson.toJson(schedule.daysInWeek)
                    )
                    put(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_WEEK_REPEAT, schedule.numOfWeekRepeat)
                }

                is Schedule.MonthlySchedule -> {
                    put(
                        DatabaseHelper.COLUMN_SCHEDULE_DATE_IN_MONTH,
                        gson.toJson(schedule.dateInMonth)
                    )
                    put(
                        DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_MONTH_REPEAT,
                        schedule.numOfMonthRepeat
                    )
                }

                is Schedule.ScheduleEveryDayRepeat -> {
                    if (schedule.dueDay == "") {
                        put(
                            DatabaseHelper.COlUMN_SCHEDULE_REPEAT_INFINITELY,
                            schedule.repeatInfinitely
                        )
                    }
                }

                else -> {}
            }
        }
        return db.insertOrThrow(DatabaseHelper.TABLE_SCHEDULE, null, values)
    }

    override fun getScheduleByIdHabit(idHabit: Long): Schedule? {
        val db = dbHelper.readableDatabase
        val sql =
            "SELECT * FROM ${DatabaseHelper.TABLE_SCHEDULE} WHERE ${DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID} = ?"
        val cursor = db.rawQuery(sql, arrayOf(idHabit.toString()))

        return if (cursor.moveToFirst()) {
            cursorToSchedule(cursor)
        } else {
            null
        }
    }


    override fun updateSchedule(schedule: Schedule): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SCHEDULE_START_DATE, schedule.startDate)
            put(DatabaseHelper.COLUMN_SCHEDULE_TYPE, schedule.scheduleType)
            put(DatabaseHelper.COLUMN_SCHEDULE_DUE_DATE, schedule.dueDay)
            put(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_TIMES, schedule.numOfTime)
            put(DatabaseHelper.COLUMN_SCHEDULE_TIME_FOR_HABIT, schedule.timeForHabit)
            put(DatabaseHelper.COLUMN_SCHEDULE_TIME_REMINDS, gson.toJson(schedule.timeReminds))
            put(DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID, schedule.habitId)
            when (schedule) {
                is Schedule.WeeklySchedule -> {
                    put(
                        DatabaseHelper.COLUMN_SCHEDULE_DAYS_IN_WEEK,
                        gson.toJson(schedule.daysInWeek)
                    )
                    put(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_WEEK_REPEAT, schedule.numOfWeekRepeat)
                }

                is Schedule.MonthlySchedule -> {
                    put(
                        DatabaseHelper.COLUMN_SCHEDULE_DATE_IN_MONTH,
                        gson.toJson(schedule.dateInMonth)
                    )
                    put(
                        DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_MONTH_REPEAT,
                        schedule.numOfMonthRepeat
                    )
                }

                is Schedule.ScheduleEveryDayRepeat -> {
                    if (schedule.dueDay == "") {
                        put(
                            DatabaseHelper.COlUMN_SCHEDULE_REPEAT_INFINITELY,
                            schedule.repeatInfinitely
                        )
                    }
                }

                else -> {}
            }
        }
        return db.update(
            DatabaseHelper.TABLE_SCHEDULE, values,
            "${DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID} = ?",
            arrayOf(schedule.habitId.toString())
        )
    }

    override fun deleteSchedule(habitId: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_SCHEDULE,
            "${DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID} = ?",
            arrayOf(habitId.toString())
        )
    }

    private fun cursorToSchedule(cursor: Cursor): Schedule {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_ID))
        val type =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_TYPE))
        val startDate =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_START_DATE))
        val dueDay =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_DUE_DATE))
        val numOfTime =
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_TIMES))
        val timeForHabit =
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_TIME_FOR_HABIT))
        val timesRemindsJson =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_TIME_REMINDS))
        val timesReminds = gson.fromJson(timesRemindsJson, Array<String>::class.java).toList()
        val habitId =
            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID))

        return when (type) {
            Schedule.WeeklySchedule::class.java.simpleName -> {
                val daysInWeekJson =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_DAYS_IN_WEEK))
                val daysInWeek = gson.fromJson(daysInWeekJson, Array<String>::class.java).toList()
                val numOfWeekRepeat =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_WEEK_REPEAT))
                Schedule.WeeklySchedule(
                    startDate,
                    dueDay,
                    numOfTime,
                    timeForHabit,
                    timesReminds,
                    daysInWeek,
                    numOfWeekRepeat,
                    habitId
                )
            }

            Schedule.MonthlySchedule::class.java.simpleName -> {
                val dateInMonthJson =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_DATE_IN_MONTH))
                val dateInMonth = gson.fromJson(dateInMonthJson, Array<String>::class.java).toList()

                val numOfMonthRepeat =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCHEDULE_NUM_OF_MONTH_REPEAT))
                Schedule.MonthlySchedule(
                    startDate,
                    dueDay,
                    numOfTime,
                    timeForHabit,
                    timesReminds,
                    dateInMonth,
                    numOfMonthRepeat,
                    habitId
                )
            }

            Schedule.ScheduleEveryDayRepeat::class.java.simpleName -> {
                val repeatInfinitely =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COlUMN_SCHEDULE_REPEAT_INFINITELY))
                Schedule.ScheduleEveryDayRepeat(
                    startDate,
                    dueDay,
                    numOfTime,
                    timeForHabit,
                    timesReminds,
                    habitId,
                    repeatInfinitely
                )
            }

            else -> Schedule.ScheduleNotRepeat(
                startDate, numOfTime, timeForHabit, habitId, timesReminds
            )
        }
    }
}