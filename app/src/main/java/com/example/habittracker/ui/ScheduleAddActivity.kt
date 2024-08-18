package com.example.habittracker.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.habittracker.R
import com.example.habittracker.RepeatTaskMonthly
import com.example.habittracker.RepeatTaskWeekly
import com.example.habittracker.adapter.ItemTimeRemindAdapter
import com.example.habittracker.conversation.OnDataPassDatePickOfWeekly
import com.example.habittracker.conversation.OnDataPassDaysPickOfMonthLy
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import com.google.android.material.switchmaterial.SwitchMaterial
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleAddActivity : AppCompatActivity(), ItemTimeRemindAdapter.OnItemDeleteListener,
    OnDataPassDaysPickOfMonthLy, OnDataPassDatePickOfWeekly {

    private lateinit var textDuDate: TextView
    private lateinit var txtNumOfTimes: TextView
    private lateinit var txtTimeForHabit: TextView
    private lateinit var txtTask: TextView
    private lateinit var numOrTimeHabit: TextView
    private lateinit var listView: ListView
    private lateinit var txtNameHabit: TextView
    private lateinit var adapter: ItemTimeRemindAdapter
    private val timeReminds = ArrayList<String>()
    private lateinit var currentDate: LocalDate
    private lateinit var txtNote: TextView
    private val daysPickMonthly = ArrayList<String>()
    private val daysPickWeekly = ArrayList<String>()
    private lateinit var txtSave: TextView
    private lateinit var dataByTypeOfTime: MutableMap<Int, Boolean>
    private var timeForHabit = 0
    private var numOfTime = 0
    private var everyRepeatMonthly = 0
    private var everyRepeatWeekly = 0
    private lateinit var habitDAO: HabitDAOImpl
    private val dbHelper = DatabaseHelper(this)
    private lateinit var mySwitch: SwitchMaterial


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule_add)

        habitDAO = HabitDAOImpl(this, dbHelper)

        textDuDate = findViewById(R.id.textDuDate)

        listView = findViewById(R.id.listViewTimeReminder)

        txtNote = findViewById(R.id.txtNote)

        adapter = ItemTimeRemindAdapter(this, timeReminds, this)

        txtNameHabit = findViewById(R.id.txtNameHabit)

        val layoutClickNote: View = findViewById(R.id.layoutClickNote)
        layoutClickNote.setOnClickListener {
            showEditTextDialog()
        }

        txtNumOfTimes = findViewById(R.id.txtNumOfTimes)
        txtTimeForHabit = findViewById(R.id.txtTimeForHabit)
        numOrTimeHabit = findViewById(R.id.numOrTimeHabit)
        txtTask = findViewById(R.id.txtTask)


        dataByTypeOfTime = mutableMapOf(
            txtNumOfTimes.id to false,
            txtTimeForHabit.id to false
        )

        val editTextHeight = numOrTimeHabit.layoutParams.height
        txtTask.setOnClickListener {
            val params = numOrTimeHabit.layoutParams
            params.height = 0
            numOrTimeHabit.layoutParams = params
            numOrTimeHabit.text = ""
            txtTask.isEnabled = false
            txtNumOfTimes.isEnabled = true
            txtTimeForHabit.isEnabled = true

            dataByTypeOfTime[txtNumOfTimes.id] = false
            dataByTypeOfTime[txtTimeForHabit.id] = false

            txtTask.setTextColor(Color.BLACK)
            txtTask.setBackgroundResource(R.drawable.background_textview_button)
            txtTimeForHabit.setBackgroundResource(R.drawable.background_textview_button_non_select)
            txtNumOfTimes.setBackgroundResource(R.drawable.background_textview_button_non_select)
        }

        txtTask.performClick()
        txtNumOfTimes.setOnClickListener {
            val params = numOrTimeHabit.layoutParams
            params.height = editTextHeight
            numOrTimeHabit.layoutParams = params
            numOrTimeHabit.hint = "Times"
            txtNumOfTimes.isEnabled = false
            txtTimeForHabit.isEnabled = true
            txtTask.isEnabled = true

            dataByTypeOfTime[txtNumOfTimes.id] = true
            dataByTypeOfTime[txtTimeForHabit.id] = false

            if (numOrTimeHabit.text.toString() != ""){
                timeForHabit =  numOrTimeHabit.text.toString().toInt()
            }

            numOrTimeHabit.text = ""

            if (numOfTime != 0){
                numOrTimeHabit.text = "$numOfTime"
            }

            txtNumOfTimes.setTextColor(Color.BLACK)
            txtNumOfTimes.setBackgroundResource(R.drawable.background_textview_button)
            txtTimeForHabit.setBackgroundResource(R.drawable.background_textview_button_non_select)
            txtTask.setBackgroundResource(R.drawable.background_textview_button_non_select)
        }

        txtTimeForHabit.setOnClickListener {
            val params = numOrTimeHabit.layoutParams
            params.height = editTextHeight
            numOrTimeHabit.layoutParams = params

            numOrTimeHabit.hint = "Minutes"
            txtTimeForHabit.isEnabled = false
            txtNumOfTimes.isEnabled = true
            txtTask.isEnabled = true

            dataByTypeOfTime[txtTimeForHabit.id] = true
            dataByTypeOfTime[txtNumOfTimes.id] = false

            if (numOrTimeHabit.text.toString() != ""){
                numOfTime =  numOrTimeHabit.text.toString().toInt()
            }
            numOrTimeHabit.text = ""
            if (timeForHabit != 0){
                numOrTimeHabit.text = "$timeForHabit"
            }

            txtTimeForHabit.setTextColor(Color.BLACK)
            txtTimeForHabit.setBackgroundResource(R.drawable.background_textview_button)
            txtNumOfTimes.setBackgroundResource(R.drawable.background_textview_button_non_select)
            txtTask.setBackgroundResource(R.drawable.background_textview_button_non_select)
        }

        listView.adapter = adapter
        val dateString = intent.getStringExtra("current_date")
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        currentDate = dateString?.let {
            LocalDate.parse(it, dateFormatter)
        }!!

//        Toast.makeText(this, "Received date: $currentDate", Toast.LENGTH_SHORT).show()

        val back: ImageButton = findViewById(R.id.back_to_home_from_schedule)
        back.setOnClickListener {
            onBackPressed()
        }

        val textDailyClick: TextView = findViewById(R.id.dailyClick)
        val textMonthlyClick: TextView = findViewById(R.id.monthlyClick)
        val textWeeklyClick: TextView = findViewById(R.id.weeklyClick)

        textDailyClick.setOnClickListener {
            textMonthlyClick.setBackgroundResource(R.drawable.text_view_background)
            textWeeklyClick.setBackgroundResource(R.drawable.text_view_background)
            textDailyClick.setBackgroundResource(R.drawable.text_view_background_select)
            dailyClick()
        }

        val repeatTaskWeekly = RepeatTaskWeekly()
        textWeeklyClick.setOnClickListener {
            textDailyClick.setBackgroundResource(R.drawable.text_view_background)
            textMonthlyClick.setBackgroundResource(R.drawable.text_view_background)
            textWeeklyClick.setBackgroundResource(R.drawable.text_view_background_select)

            weeklyClick(repeatTaskWeekly)
        }

        val repeatTaskMonthly = RepeatTaskMonthly()

        textMonthlyClick.setOnClickListener {
            textDailyClick.setBackgroundResource(R.drawable.text_view_background)
            textWeeklyClick.setBackgroundResource(R.drawable.text_view_background)
            textMonthlyClick.setBackgroundResource(R.drawable.text_view_background_select)
            monthlyClick(repeatTaskMonthly)
        }

        val datePicker: View = findViewById(R.id.datePicker)
        datePicker.setOnClickListener {
            datePickerAction(textDuDate)
        }


        val myLayout: LinearLayout = findViewById(R.id.layout_repeat)
        mySwitch = findViewById(R.id.material_switch)
        mySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                myLayout.visibility = View.VISIBLE
                val params = myLayout.layoutParams
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT
                myLayout.layoutParams = params
                //set color
                mySwitch.thumbTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.colorSwitchThumbOn
                    )
                )
                mySwitch.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.colorSwitchTrackOn
                    )
                )
                textDailyClick.performClick()
            } else {
                myLayout.visibility = View.INVISIBLE
                val params = myLayout.layoutParams
                params.height = 0
                myLayout.layoutParams = params
                mySwitch.thumbTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.colorSwitchThumbOff
                    )
                )
                mySwitch.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.colorSwitchTrackOff
                    )
                )
            }
        }

        val getTimeRemindButton: View = findViewById(R.id.getTimeReminders)
        getTimeRemindButton.setOnClickListener {
            getTimeReminders()
        }

        txtSave = findViewById(R.id.txtSave)
        txtSave.setOnClickListener {
            if (saveHabit()){
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("ACTION", "NAVIGATE_TO_CALENDAR")
                startActivity(intent)
            }

        }
    }

    fun setListViewHeightBasedOnItems() {
        val listAdapter = listView.adapter ?: return
        var totalHeight = 0
        val adapterCount = listAdapter.count
        for (i in 0 until adapterCount) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (adapterCount - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

    private fun saveHabit() : Boolean {
        val habit: Habit
        var schedule: Schedule? = null
        txtNameHabit
        txtNote
        timeReminds
        if (txtNameHabit.text.isNullOrBlank()) {
            Toast.makeText(this, "Please enter name habit", Toast.LENGTH_SHORT).show()
            return false
        }
        if (habitDAO.getHabitByName(txtNameHabit.text.toString()) != null){
            Toast.makeText(this, "Habit has existed", Toast.LENGTH_SHORT).show()
            return false
        }
        val numOfTime = if (dataByTypeOfTime[txtNumOfTimes.id] == true) {
            if (numOrTimeHabit.text.toString() != "") numOrTimeHabit.text.toString().toInt() else 0
        } else {
            0
        }

        val timeForHabit = if (dataByTypeOfTime[txtTimeForHabit.id] == true) {
            if (numOrTimeHabit.text.toString() != "") numOrTimeHabit.text.toString().toInt() else 0
        } else {
            0
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // create schedule
        if (supportFragmentManager.findFragmentByTag("Monthly_TAG") != null) {
            if (daysPickMonthly.size == 0) {
                Toast.makeText(this, "Please select at least 1 day", Toast.LENGTH_SHORT).show()
            } else {
                val dueDate = if (textDuDate.text.toString() == "_/_/_") {
                    currentDate.plusMonths(everyRepeatMonthly.toLong()).format(formatter)
                }else{
                    convertDate(textDuDate.text.toString()).toString()
                }
                schedule = Schedule.MonthlySchedule(
                    currentDate.format(formatter),
                    dueDate,
                    numOfTime,
                    timeForHabit,
                    timeReminds,
                    daysPickMonthly,
                    everyRepeatMonthly
                )
            }
        } else if (supportFragmentManager.findFragmentByTag("Weekly_TAG") != null) {
            if (daysPickWeekly.size == 0) {
                Toast.makeText(this, "Please select at least 1 date", Toast.LENGTH_SHORT).show()
            } else {
                val dueDate = if (textDuDate.text.toString() == "_/_/_") {
                    currentDate.plusWeeks(everyRepeatWeekly.toLong()).format(formatter)
                }else{
                    convertDate(textDuDate.text.toString()).toString()
                }
                schedule = Schedule.WeeklySchedule(
                    currentDate.format(formatter),
                    dueDate,
                    numOfTime,
                    timeForHabit,
                    timeReminds,
                    daysPickWeekly,
                    everyRepeatWeekly
                )
            }
        } else if (mySwitch.isChecked) {
            if (textDuDate.text.toString() != "_/_/_"){
                schedule = Schedule.ScheduleEveryDayRepeat(
                    startDate = currentDate.format(formatter),
                    convertDate(textDuDate.text.toString()).toString(),
                    numOfTime = numOfTime,
                    timeForHabit = timeForHabit,
                    timeReminds = timeReminds
                )

            }else{
                schedule = Schedule.ScheduleEveryDayRepeat(
                    startDate = currentDate.format(formatter),
                    "",
                    numOfTime = numOfTime,
                    timeForHabit = timeForHabit,
                    timeReminds = timeReminds,
                    repeatInfinitely = 1
                )
            }

        } else {
            schedule = Schedule.ScheduleNotRepeat(
                startDate = currentDate.format(formatter),
                numOfTime = numOfTime,
                timeForHabit = timeForHabit,
                timeReminds = timeReminds
            )
        }
        // create habit
        if (schedule != null) {
            habit = Habit(
                name = txtNameHabit.text.toString(),
                description = txtNote.text.toString(),
                schedule = schedule
            )
            Log.e("habit", "habit: ${habit}")
            habitDAO.insertHabit(habit) { exception ->
                Log.e("DatabaseError", "Failed to add habit and schedule: ${exception.message}")
            }

        }
        return true
    }

    private fun convertDate(dateString: String): String? {
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    }

    private fun showEditTextDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextDialog)
        val textDefault = "Currently, you don't have any notes"
        if (!txtNote.text.equals(textDefault)) {
            editText.setText(txtNote.text)
        }
        AlertDialog.Builder(this)
            .setTitle("Take Note")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // Get the text from EditText and set it to the TextView
                val inputText = editText.text.toString()
                txtNote.text = inputText
                if (inputText == "") {
                    txtNote.text = textDefault
                }

            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun dailyClick() {
        val fragment = supportFragmentManager.findFragmentByTag("Weekly_TAG")
            ?: supportFragmentManager.findFragmentByTag("Monthly_TAG")
        fragment?.let {
            supportFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    private fun weeklyClick(repeatTaskWeekly: RepeatTaskWeekly) {
        val fragment = supportFragmentManager.findFragmentByTag("Weekly_TAG")
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .show(fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, repeatTaskWeekly, "Weekly_TAG")
                .commit()
        }
    }

    private fun monthlyClick(repeatTaskMonthly: RepeatTaskMonthly) {
        val fragment = supportFragmentManager.findFragmentByTag("Monthly_TAG")

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .show(fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, repeatTaskMonthly, "Monthly_TAG")
                .commit()
        }
    }

    private fun datePickerAction(textView: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.MyDatePickerDialogTheme,
            { view, year, monthOfYear, dayOfMonth ->
                textView.text =
                    (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun getTimeReminders() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            R.style.CustomTimePickerDialog,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timeReminds.add(formattedTime)
                adapter.notifyDataSetChanged()

            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    override fun onItemDelete(position: Int) {
        timeReminds.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    override fun onDataPassDaysPickOfMonthLy(days: List<String>, everyRepeat: Int) {
        daysPickMonthly.clear()
        daysPickMonthly.addAll(days)
        everyRepeatMonthly = everyRepeat
    }

    override fun onDataPassDate(days: List<String>, everyRepeat: Int) {
        daysPickWeekly.clear()
        daysPickWeekly.addAll(days)
        everyRepeatWeekly = everyRepeat
        Toast.makeText(this, "Received date: $days, $everyRepeat", Toast.LENGTH_SHORT).show()
    }
}