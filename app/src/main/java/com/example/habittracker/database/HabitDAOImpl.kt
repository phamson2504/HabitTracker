package com.example.habittracker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.habittracker.model.CompletionRecord
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters

class HabitDAOImpl(private val context: Context, private val dbHelper: DatabaseHelper) : HabitDAO {

    override fun insertHabit(habit: Habit, onError: (Exception) -> Unit): Boolean {
        val db = dbHelper.writableDatabase
        var success = false

        db.beginTransaction()
        try {
            val valuesHabit = ContentValues().apply {
                put(DatabaseHelper.COLUMN_NAME_HABIT, habit.name)
                put(DatabaseHelper.COLUMN_HABIT_DESCRIPTION, habit.description)
                put(DatabaseHelper.COLUMN_HABIT_COLOR, habit.color)
                put(DatabaseHelper.COLUMN_HABIT_ICON, habit.icon)
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

    override fun getHabitByName(nameHabit: String): Habit? {
        val db = dbHelper.writableDatabase
        val query = """ 
            SELECT *  
            FROM ${DatabaseHelper.TABLE_HABIT} 
            WHERE ${DatabaseHelper.COLUMN_NAME_HABIT} = :nameHabit
            """
        val cursor = db.rawQuery(query, arrayOf(nameHabit))
        return if (cursor.moveToFirst()) {
            cursorToHabit(cursor)
        } else {
            null
        }

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
        val dayOfMonth = date.format(DateTimeFormatter.ofPattern("dd"))

        return filterHabitsByDayOfWeek(habits, dayOfWeek, dayOfMonth, date)
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
        cursor.close()
        return listHabit
    }

    override fun getHabitsByDate(startDate: String, endDate: String): MutableList<Habit> {
        val db = dbHelper.readableDatabase
        val listHabit = mutableListOf<Habit>()
        val query = """
            SELECT h.* FROM ${DatabaseHelper.TABLE_HABIT} AS h 
            INNER JOIN ${DatabaseHelper.TABLE_SCHEDULE} AS s 
            ON h.${DatabaseHelper.COLUMN_HABIT_ID} = s.${DatabaseHelper.COLUMN_SCHEDULE_HABIT_ID} 
            WHERE 
                (date(s.${DatabaseHelper.COLUMN_SCHEDULE_START_DATE}) <= date(:endDate) AND date(s.${DatabaseHelper.COLUMN_SCHEDULE_DUE_DATE}) >= date(:startDate))
            OR 
                (date(s.${DatabaseHelper.COLUMN_SCHEDULE_START_DATE}) >= date(:startDate) AND s.${DatabaseHelper.COlUMN_SCHEDULE_REPEAT_INFINITELY} = 1)
        """
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        try {
            while (cursor.moveToNext()) {
                listHabit.add(cursorToHabit(cursor))
            }
        } finally {
            cursor.close()
        }

        return listHabit
    }

    private fun cursorToHabit(cursor: Cursor): Habit {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HABIT_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME_HABIT))
        val color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HABIT_COLOR))
        val icon =  cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HABIT_ICON))
        val description =
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HABIT_DESCRIPTION))
        var schedule: Schedule? = null
        val scheduleDAO = ScheduleDAOImpl(context, dbHelper)
        if (id != -1L) {
            schedule = scheduleDAO.getScheduleByIdHabit(idHabit = id)
        }
        return Habit(id, name, description, schedule = schedule, color = color, icon = icon)
    }

    override fun updateHabit(habit: Habit, onError: (Exception) -> Unit): Boolean {
        val db = dbHelper.writableDatabase
        var success = false

        db.beginTransaction()
        try {
            val valuesHabit = ContentValues().apply {
                put(DatabaseHelper.COLUMN_NAME_HABIT, habit.name)
                put(DatabaseHelper.COLUMN_HABIT_DESCRIPTION, habit.description)
                put(DatabaseHelper.COLUMN_HABIT_COLOR, habit.color)
                put(DatabaseHelper.COLUMN_HABIT_ICON, habit.icon)
            }
            db.update(DatabaseHelper.TABLE_HABIT, valuesHabit, "id=?", arrayOf(habit.id.toString()))

            val scheduleDAO = ScheduleDAOImpl(context, dbHelper)

            habit.schedule.let { schedule ->
                schedule?.habitId = habit.id
                if (schedule != null) {
                    scheduleDAO.updateSchedule(schedule)
                }
            }

            val completionRecordDAO = CompletionRecordDAOImpl(context, dbHelper)
            val listCompletionRecord = completionRecordDAO.getCompletionRecordByHabitId(habit.id.toString())
            val listCRActive = completionRecordActive(listCompletionRecord, habit)
            updateCompletionRecordActive(listCompletionRecord,listCRActive , completionRecordDAO)

            db.setTransactionSuccessful()
            success = true
        } catch (e: Exception) {
            onError(e)
        } finally {
            db.endTransaction()
        }
        return success
    }

    private fun updateCompletionRecordActive(listCompletionRecord: MutableList<CompletionRecord>, listCRActive: MutableList<CompletionRecord>, completionRecordDAO: CompletionRecordDAOImpl) {
        listCompletionRecord.forEach {
            if (listCRActive.contains(it)){
                if ( it.currentOperationalStatus != 1){
                    it.currentOperationalStatus = 1
                    completionRecordDAO.updateActiveCompletion(it)
                }
            }else{
                if (it.currentOperationalStatus != 0){
                    it.currentOperationalStatus = 0
                    completionRecordDAO.updateActiveCompletion(it)
                }

            }
        }
    }


    override fun deleteHabit(habitId: String, onError: (Exception) -> Unit): Boolean {
    val db = dbHelper.writableDatabase
        var success = false

        db.beginTransaction()
        try {
            val db = dbHelper.writableDatabase


            val completionRecordDAO = CompletionRecordDAOImpl(context, dbHelper)
            completionRecordDAO.deleteCompletionRecordById(habitId)

            val scheduleDAO = ScheduleDAOImpl(context, dbHelper)
            scheduleDAO.deleteSchedule(habitId)

            db.delete(
                DatabaseHelper.TABLE_HABIT,
                "${DatabaseHelper.COLUMN_HABIT_ID} = ?",
                arrayOf(habitId)
            )

            db.setTransactionSuccessful()
            success = true
        } catch (e: Exception) {
            onError(e)
        } finally {
            db.endTransaction()
        }
        return success
    }

    private fun completionRecordActive(
        listCompletionRecord: MutableList<CompletionRecord>,
        habit: Habit
    ): MutableList<CompletionRecord> {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val schedule = habit.schedule ?: return mutableListOf()
        val startDate = LocalDate.parse(schedule.startDate, inputFormatter).minusDays(1)


        val cRCurrentActive = mutableListOf<CompletionRecord>()
        listCompletionRecord.forEach {
            val dateCompletionRecord = LocalDate.parse(it.date, inputFormatter)

            when (schedule) {
                is Schedule.WeeklySchedule -> {
                    val daysInWeek = schedule.daysInWeek ?: emptyList()
                    if (dateCompletionRecord.isAfter(startDate)
                        && dateCompletionRecord.isBefore(LocalDate.parse(schedule.dueDay, inputFormatter).plusMonths(1))
                        && daysInWeek.contains(dateCompletionRecord.format(DateTimeFormatter.ofPattern("EEEE")))
                    ) {
                        cRCurrentActive.add(it)
                    }
                }
                is Schedule.ScheduleEveryDayRepeat -> {
                    if (dateCompletionRecord.isAfter(startDate)) {
                        cRCurrentActive.add(it)
                    }
                }
                is Schedule.MonthlySchedule -> {
                    val daysInMonth = schedule.dateInMonth ?: emptyList()
                    val lastDateOfMonth = dateCompletionRecord.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
                    if (
                        (daysInMonth.contains("last") && lastDateOfMonth == dateCompletionRecord.dayOfMonth
                                && dateCompletionRecord.isAfter(startDate) && dateCompletionRecord.isBefore(LocalDate.parse(schedule.dueDay, inputFormatter).plusMonths(1)))
                        ||
                        (dateCompletionRecord.isAfter(startDate) && dateCompletionRecord.isBefore(LocalDate.parse(schedule.dueDay, inputFormatter).plusMonths(1))
                                && daysInMonth.contains(dateCompletionRecord.dayOfMonth.toString()))
                        ||
                        (dateCompletionRecord.dayOfMonth == lastDateOfMonth && !daysInMonth.contains("last")
                                && dateCompletionRecord.isAfter(startDate) && dateCompletionRecord.isBefore(LocalDate.parse(schedule.dueDay, inputFormatter).plusMonths(1)))
                    ) {
                        cRCurrentActive.add(it)
                    }
                }
                else -> {
                    if (dateCompletionRecord == startDate) {
                        cRCurrentActive.add(it)
                    }
                }
            }
        }
        return cRCurrentActive
    }


    private fun filterHabitsByDayOfWeek(
        habits: List<Habit>,
        currentDayOfWeek: String,
        currentDay: String,
        currentDate: LocalDate
    ): List<Habit> {
        val filteredHabits = mutableListOf<Habit>()
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (habit in habits) {
            if (currentDate.isBefore(LocalDate.parse(habit.schedule?.startDate, inputFormatter))){
                continue
            }
            val schedule = habit.schedule
            if (schedule is Schedule.WeeklySchedule) {
                val daysInWeek = schedule.daysInWeek ?: emptyList()
                if (daysInWeek.contains(currentDayOfWeek)) {
                    filteredHabits.add(habit)
                }
            } else if (schedule is Schedule.ScheduleEveryDayRepeat) {
                filteredHabits.add(habit)
            } else if (schedule is Schedule.MonthlySchedule) {
                val daysInMonth = schedule.dateInMonth ?: emptyList()
                val lastDate = currentDate.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
                if (daysInMonth.contains("last")) {
                    if (lastDate.toString() == currentDay) {
                        filteredHabits.add(habit)
                    }
                }
                if (daysInMonth.contains(currentDay.toInt().toString()) || (currentDay.toInt().toString() == lastDate.toString() && !daysInMonth.contains("last"))) {
                    filteredHabits.add(habit)
                }
            } else {
                filteredHabits.add(habit)
            }
        }
        return filteredHabits
    }
}