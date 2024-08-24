package com.example.habittracker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.adapter.HabitCalendarInfoAdapter
import com.example.habittracker.database.CompletionRecordDAO
import com.example.habittracker.database.CompletionRecordDAOImpl
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters

class InforHabitActivity : AppCompatActivity() {

    private var habit: HabitHandle? = null
    private lateinit var clickHabitEdit: ImageButton
    private lateinit var backPreviousImageButton: ImageButton
    private lateinit var selectedDate: LocalDate
    private var currentMonth: Int = 0
    lateinit var monthOfYear: TextView
    private var daysOfMonth = ArrayList<LocalDate>()
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var dayCompleteInMonth: MutableMap<LocalDate, Int>
    private var dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private lateinit var habitDAOImpl: HabitDAOImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_infor_habit)

        dbHelper = DatabaseHelper(this)
//        dbHelper.resetDatabase()

        habitDAOImpl = HabitDAOImpl(this, dbHelper)
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
        // Optional: Adjust status bar icon color
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true

        monthOfYear = findViewById(R.id.month_of_year_info)

        habit = intent.getSerializableExtra("HABIT") as? HabitHandle

        val dateString = intent.getStringExtra("current_date")
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        selectedDate = dateString?.let {
            LocalDate.parse(it, dateFormatter)
        }!!

        habit?.let {
            Toast.makeText(this, "HABIT: $it", Toast.LENGTH_SHORT).show()
        }


        clickHabitEdit = findViewById(R.id.clickHabitEdit)
        clickHabitEdit.setOnClickListener {
            val intent = Intent(this, ScheduleEditActivity::class.java)
            intent.putExtra("HABIT", habit)
            startActivity(intent)
        }

        backPreviousImageButton = findViewById(R.id.back_previous)
        backPreviousImageButton.setOnClickListener {
            onBackPressed()
        }
        setMonthView()

        val previousMonthActionButton: AppCompatImageButton =
            findViewById(R.id.previousMonthActionInfo)
        previousMonthActionButton.setOnClickListener {
            previousMonthAction()
        }
        val nextMonthActionButton: AppCompatImageButton = findViewById(R.id.nextMonthActionInfo)
        nextMonthActionButton.setOnClickListener {
            nextMonthAction()
        }
        val deleteAction : ImageView = findViewById(R.id.delete_icon)
        deleteAction.setOnClickListener {
            val deleteAction = habitDAOImpl.deleteHabit(habit?.habit?.id.toString()) { exception ->
                Log.e("DatabaseError", "Failed to delete habit ${habit?.habit?.id}: ${exception.message}")
            }
            if (deleteAction){
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("ACTION", "NAVIGATE_TO_CALENDAR")
                startActivity(intent)
            }
        }
    }

    private fun setMonthView() {
        currentMonth = selectedDate.month.value

        monthOfYear.text = selectedDate.format(formatter)
        daysOfMonth = ArrayList()

        val recyclerView: RecyclerView = findViewById(R.id.calendarRecyclerViewInfo)

        daysInMonthArray()

        dbHelper = DatabaseHelper(this)
        val completionRecordDAO: CompletionRecordDAO = CompletionRecordDAOImpl(this, dbHelper)
        val listRecordHabit = completionRecordDAO.getCompletionRecordInMonthByHabitId(
            habit?.habit?.id.toString(),
            selectedDate
        )
        listRecordHabit.forEachIndexed { index, record ->
            if (habit?.habit?.schedule?.numOfTime != 0 || habit?.habit?.schedule?.timeForHabit != 0) {
                listRecordHabit[index] = habit?.habit?.getCompletionRecordForToday(
                    record.numOfTimesCompleted,
                    record.timeForHabit,
                    record.date
                )!!
            }
        }
        dayCompleteInMonth = listRecordHabit.associateTo(mutableMapOf()) { record ->
            val date = LocalDate.parse(record.date, dateFormatter)
            date to record.isCompleted
        }

        val dayOfMonthHabit = dayOfMonthHabit(false)

        val dayOfHabitAll = dayOfMonthHabit(true)

        val dayCompleteInMonthCurrent = mutableMapOf<String, Int>()
        val completeRecord = completionRecordDAO.getCompletionRecordByHabitId(habit?.habit?.id.toString())
        completeRecord.forEachIndexed { index, record ->
            if (habit?.habit?.schedule?.numOfTime != 0 || habit?.habit?.schedule?.timeForHabit != 0) {
                completeRecord[index] = habit?.habit?.getCompletionRecordForToday(
                    record.numOfTimesCompleted,
                    record.timeForHabit,
                    record.date
                )!!
            }
        }

        dayCompleteInMonth.forEach { (day, record) ->
            if (dayOfMonthHabit?.contains(day) == true) {
                dayCompleteInMonthCurrent[day.dayOfMonth.toString()] = record
            }
        }

        var totalHabitComplete = 0
        completeRecord.forEach {
            if (it.isCompleted == 3) {
                totalHabitComplete++
            }
        }

        val txtProcess: TextView = findViewById(R.id.percentProcess)
        if (dayOfHabitAll != null)
            txtProcess.text =
                ((completeRecord.size.toDouble() / dayOfHabitAll.size.toDouble()) * 100).toInt()
                    .toString()

        val txtCompletionRate = findViewById<TextView>(R.id.completionRate)
        if (dayOfHabitAll != null){
            txtCompletionRate.text =
                ((totalHabitComplete / dayOfHabitAll.size.toDouble()) * 100).toInt()
                    .toString()
        }

        val layoutManager = GridLayoutManager(this, 7)
        recyclerView.layoutManager = layoutManager

        val calendarAdapter = HabitCalendarInfoAdapter(
            this,
            daysOfMonth,
            selectedDate,
            dayCompleteInMonthCurrent,
            dayOfMonthHabit
        )
        recyclerView.adapter = calendarAdapter

    }

    private fun daysInMonthArray(): ArrayList<LocalDate> {

        val firstOfMonth: LocalDate = selectedDate.withDayOfMonth(1)
        val firstDayOfWeekInMonth =
            firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val lastDayOfWeekInMonth = selectedDate.with(TemporalAdjusters.lastDayOfMonth())
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

        var currentDate = firstDayOfWeekInMonth
        while (!currentDate.isAfter(lastDayOfWeekInMonth)) {
            daysOfMonth.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }
        return daysOfMonth
    }

    private fun dayOfMonthHabit(isAll: Boolean): MutableList<LocalDate>? {
        val startDay = habit?.habit?.schedule?.startDate
        val schedule = habit?.habit?.schedule
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var dateStart = LocalDate.parse(startDay, dateFormatter)
        val firstOfMonth: LocalDate = selectedDate.withDayOfMonth(1)
        var lastDayOfMonth: LocalDate
        val currentDay = LocalDate.now()
        if (dateStart.isAfter(currentDay)) {
            return null
        } else if (dateStart.isBefore(firstOfMonth)) {
            dateStart = firstOfMonth
        }
        if (schedule is Schedule.WeeklySchedule || schedule is Schedule.MonthlySchedule) {
            lastDayOfMonth = LocalDate.parse(schedule.dueDay, dateFormatter)
            if (currentDay.isBefore(lastDayOfMonth)) {
                lastDayOfMonth = currentDay
            }
        } else if (schedule is Schedule.ScheduleEveryDayRepeat) {
            if (schedule.repeatInfinitely == 1) {
                lastDayOfMonth = currentDay
            } else {
                lastDayOfMonth = LocalDate.parse(schedule.dueDay, dateFormatter)
                if (currentDay.isBefore(lastDayOfMonth)) {
                    lastDayOfMonth = currentDay
                }
            }
        } else {
            lastDayOfMonth = dateStart
        }
        if (isAll){
            dateStart = LocalDate.parse(startDay, dateFormatter)
        }else{
            if ((lastDayOfMonth.month < currentDay.month && lastDayOfMonth.year < currentDay.year)
                || (lastDayOfMonth.month > selectedDate.month && lastDayOfMonth.year == selectedDate.year)
                || (lastDayOfMonth.year < selectedDate.year)
            ) {
                lastDayOfMonth = selectedDate.with(TemporalAdjusters.lastDayOfMonth())
            }
        }

        var day = dateStart

        val dayHaveToSet = mutableListOf<LocalDate>()
        while (day.isBefore(lastDayOfMonth) || day.equals(lastDayOfMonth)) {
            if (schedule is Schedule.WeeklySchedule) {
                val daysInWeek = schedule.daysInWeek ?: emptyList()
                if (daysInWeek.contains(day.format(DateTimeFormatter.ofPattern("EEEE")))) {
                    dayHaveToSet.add(day)
                }
            } else if (schedule is Schedule.ScheduleEveryDayRepeat) {
                dayHaveToSet.add(day)
            } else if (schedule is Schedule.MonthlySchedule) {
                val daysInMonth = schedule.dateInMonth ?: emptyList()
                if (daysInMonth.contains("last")) {
                    val lastDate =
                        selectedDate.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth.toString()
                    if (lastDate == day.dayOfMonth.toString()) {
                        dayHaveToSet.add(day)
                    }
                }
                if (daysInMonth.contains(day.dayOfMonth.toString())) {
                    dayHaveToSet.add(day)
                }
            } else {
                dayHaveToSet.add(day)
            }
            day = day.plusDays(1)
        }
        return dayHaveToSet
    }

private fun previousMonthAction() {
    currentMonth -= 1
    selectedDate = selectedDate.minusMonths(1)
    setMonthView()
}

private fun nextMonthAction() {
    currentMonth += 1
    selectedDate = selectedDate.plusMonths(1)
    setMonthView()

}


override fun onBackPressed() {
    super.onBackPressed()
    finish()
}
}