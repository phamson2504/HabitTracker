package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.adapter.HabitCalendarInfoAdapter
import com.example.habittracker.database.CompletionRecordDAO
import com.example.habittracker.database.CompletionRecordDAOImpl
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters

class InforHabitActivity : AppCompatActivity() {

    private var habit: HabitHandle? = null
    private lateinit var clickHabitEdit : ImageButton
    private lateinit var backPreviousImageButton: ImageButton
    private lateinit var selectedDate: LocalDate
    private var currentMonth: Int = 0
    lateinit var monthOfYear: TextView
    private var daysOfMonth = ArrayList<LocalDate>()
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var dayCompleteInMonth : MutableMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_infor_habit)

        monthOfYear= findViewById(R.id.month_of_year_info)

        habit = intent.getSerializableExtra("HABIT") as? HabitHandle

        val dateString = intent.getStringExtra("current_date")
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        selectedDate = dateString?.let {
            LocalDate.parse(it, dateFormatter)
        }!!

        habit?.let {
            Toast.makeText(this, "HABIT: $it", Toast.LENGTH_SHORT).show()
        }
        dbHelper = DatabaseHelper(this)
        val completionRecordDAO: CompletionRecordDAO = CompletionRecordDAOImpl(this,dbHelper)
        val listRecordHabit = completionRecordDAO.getCompletionRecordInMonthByHabitId(habit?.habit?.id.toString(),selectedDate)

        dayCompleteInMonth = listRecordHabit.associateTo(mutableMapOf()) { record ->
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(record.date, dateFormatter)
            val day = date.dayOfMonth
            day.toString() to record.isCompleted
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
    }
    private fun setMonthView() {
        currentMonth = selectedDate.month.value

        monthOfYear.text = selectedDate.format(formatter)
        daysOfMonth = ArrayList()

        val recyclerView: RecyclerView = findViewById(R.id.calendarRecyclerViewInfo)

        daysInMonthArray()
        val dayOfMonthHabit = dayOfMonthHabit()

        val layoutManager = GridLayoutManager(this, 7)
        recyclerView.layoutManager = layoutManager

        val calendarAdapter = HabitCalendarInfoAdapter(this, daysOfMonth, selectedDate , dayCompleteInMonth, dayOfMonthHabit)
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

    private fun dayOfMonthHabit(): MutableList<LocalDate>? {
        val startDay = habit?.habit?.schedule?.startDate
        val schedule = habit?.habit?.schedule
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var dateStart = LocalDate.parse(startDay, dateFormatter)
        val firstOfMonth: LocalDate = selectedDate.withDayOfMonth(1)
        var lastDayOfMonth : LocalDate
        val currentDay = LocalDate.now()
        if (dateStart.isAfter(currentDay)){
            return null
        }else if (dateStart.isBefore(firstOfMonth)){
            dateStart = firstOfMonth
        }
        if (schedule is Schedule.WeeklySchedule || schedule is Schedule.MonthlySchedule) {
            lastDayOfMonth = LocalDate.parse(schedule.dueDay, dateFormatter)
            if (currentDay.isBefore(lastDayOfMonth)){
                lastDayOfMonth = currentDay
            }
        }else if (schedule is Schedule.ScheduleEveryDayRepeat){
            if (schedule.repeatInfinitely == 1){
                lastDayOfMonth =currentDay
            }else{
                lastDayOfMonth = LocalDate.parse(schedule.dueDay, dateFormatter)
                if (currentDay.isBefore(lastDayOfMonth)){
                    lastDayOfMonth = currentDay
                }
            }
        }else {
            lastDayOfMonth = dateStart
        }

        if ((lastDayOfMonth.month < currentDay.month && lastDayOfMonth.month > selectedDate.month)
            || (lastDayOfMonth.year < currentDay.year && lastDayOfMonth.year > selectedDate.year)){
            lastDayOfMonth =  selectedDate.with(TemporalAdjusters.lastDayOfMonth())
        }


        var day = dateStart
        val dayHaveToSet = mutableListOf<LocalDate>()
        while (!day.isAfter(lastDayOfMonth)) {
            if (schedule is Schedule.WeeklySchedule) {
                val daysInWeek = schedule.daysInWeek ?: emptyList()
                if (daysInWeek.contains(day.format(DateTimeFormatter.ofPattern("EEEE")))) {
                    dayHaveToSet.add(day)
                }
            } else if (schedule is Schedule.ScheduleEveryDayRepeat) {
                dayHaveToSet.add(day)
            } else if (schedule is Schedule.MonthlySchedule){
                val daysInMonth = schedule.dateInMonth ?: emptyList()
                if (daysInMonth.contains(day.dayOfMonth.toString())){
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