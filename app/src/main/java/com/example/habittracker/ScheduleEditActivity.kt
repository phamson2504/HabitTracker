package com.example.habittracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import com.example.habittracker.adapter.ItemTimeRemindAdapter
import com.example.habittracker.conversation.OnDataPassDatePickOfWeekly
import com.example.habittracker.conversation.OnDataPassDaysPickOfMonthLy
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Schedule
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Calendar

class ScheduleEditActivity : AppCompatActivity(), ItemTimeRemindAdapter.OnItemDeleteListener,
    OnDataPassDaysPickOfMonthLy, OnDataPassDatePickOfWeekly {
    private lateinit var textDuDate: TextView
    private lateinit var textNameHabit: TextView
    private lateinit var numOrTimeHabit: TextView
    private lateinit var txtNumOfTimes: TextView
    private lateinit var txtTimeForHabit: TextView
    private lateinit var txtTask: TextView
    private lateinit var txtNote: TextView

    private lateinit var listView: ListView
    private lateinit var adapter: ItemTimeRemindAdapter
    private val items = ArrayList<String>()
    private val daysPickWeekly = ArrayList<String>()
    private lateinit var titleHandle: TextView
    private var habitHandle: HabitHandle? = null
    private val daysPickMonthly = ArrayList<String>()
    private var everyRepeatWeekly = 0
    private lateinit var dataByTypeOfTime: MutableMap<Int, Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule_edit)
        textDuDate = findViewById(R.id.textDuDate)

        textNameHabit = findViewById(R.id.txtNameHabit)
        numOrTimeHabit = findViewById(R.id.numOrTimeHabit)
        txtTimeForHabit = findViewById(R.id.txtTimeForHabit)
        txtNumOfTimes = findViewById(R.id.txtNumOfTimes)
        txtNote = findViewById(R.id.txtNote)

        listView = findViewById(R.id.listViewTimeReminder)

        titleHandle = findViewById(R.id.txtTitleHandle)
        habitHandle = intent.getSerializableExtra("HABIT") as? HabitHandle
        "Edit Habit".also { titleHandle.text = it }
//        habitHandle?.let {
//            Toast.makeText(this, "HABIT: $it", Toast.LENGTH_SHORT).show()
//        }
        txtTask = findViewById(R.id.txtTaskEdit)


        dataByTypeOfTime = mutableMapOf(
            txtNumOfTimes.id to false,
            txtTimeForHabit.id to false
        )

        val editTextHeight = numOrTimeHabit.layoutParams.height
        txtTask.setOnClickListener {
            val params = numOrTimeHabit.layoutParams
            params.height = 0
            numOrTimeHabit.layoutParams = params
            "".also { numOrTimeHabit.text = it }
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

            "".also { numOrTimeHabit.text = it }
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

            "".also { numOrTimeHabit.text = it }
            txtTimeForHabit.setTextColor(Color.BLACK)
            txtTimeForHabit.setBackgroundResource(R.drawable.background_textview_button)
            txtNumOfTimes.setBackgroundResource(R.drawable.background_textview_button_non_select)
            txtTask.setBackgroundResource(R.drawable.background_textview_button_non_select)
        }

        textNameHabit.text = habitHandle?.habit?.name

        if (habitHandle?.habit?.schedule?.numOfTime  != 0){
            txtNumOfTimes.performClick()
            numOrTimeHabit.text = habitHandle?.habit?.schedule?.numOfTime.toString()
        }else if (habitHandle?.habit?.schedule?.timeForHabit != 0){
            txtTimeForHabit.performClick()
            numOrTimeHabit.text = habitHandle?.habit?.schedule?.timeForHabit.toString()
        }

        val back: ImageButton = findViewById(R.id.back_to_home_from_schedule)
        back.setOnClickListener {
            onBackPressed()
        }

        val textDailyClick: TextView = findViewById(R.id.dailyClick)
        val textWeeklyClick: TextView = findViewById(R.id.weeklyClick)
        val textMonthlyClick: TextView = findViewById(R.id.monthlyClick)
        val schedule = habitHandle?.habit?.schedule
        var repeatTaskMonthly = RepeatTaskMonthly()
        var repeatTaskWeekly = RepeatTaskWeekly()

        if ( schedule is Schedule.MonthlySchedule) {
            repeatTaskMonthly = RepeatTaskMonthly.newInstance(schedule.dateInMonth)
        }else if (schedule is Schedule.WeeklySchedule){
            repeatTaskWeekly = RepeatTaskWeekly.newInstance(schedule.daysInWeek, schedule.numOfWeekRepeat.toString() + " Week")
        }

        textDailyClick.setOnClickListener {
            textMonthlyClick.setBackgroundResource(R.drawable.text_view_background)
            textWeeklyClick.setBackgroundResource(R.drawable.text_view_background)
            textDailyClick.setBackgroundResource(R.drawable.text_view_background_select)
            dailyClick()
        }


        textWeeklyClick.setOnClickListener {
            textDailyClick.setBackgroundResource(R.drawable.text_view_background)
            textMonthlyClick.setBackgroundResource(R.drawable.text_view_background)
            textWeeklyClick.setBackgroundResource(R.drawable.text_view_background_select)
            weeklyClick(repeatTaskWeekly)
        }

        textMonthlyClick.setOnClickListener {
            textDailyClick.setBackgroundResource(R.drawable.text_view_background)
            textWeeklyClick.setBackgroundResource(R.drawable.text_view_background)
            textMonthlyClick.setBackgroundResource(R.drawable.text_view_background_select)
            monthlyClick(repeatTaskMonthly)
        }
        if (!schedule?.dueDay.equals("")){
            textDuDate.text = schedule?.dueDay
        }


        val datePicker: View = findViewById(R.id.datePicker)
        datePicker.setOnClickListener {
            datePickerAction()
        }

        val myLayout: LinearLayout = findViewById(R.id.layout_repeat)
        val mySwitch: SwitchMaterial = findViewById(R.id.material_switch)
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

        val habit =habitHandle?.habit
        when (habit?.schedule) {
            is Schedule.MonthlySchedule -> {
                mySwitch.performClick()
                textMonthlyClick.performClick()

            }
            is Schedule.WeeklySchedule -> {
                mySwitch.performClick()
                textWeeklyClick.performClick()
            }
            is Schedule.ScheduleEveryDayRepeat -> {
                mySwitch.performClick()
                textDailyClick.performClick()
            }
            else -> {}
        }

        val getTimeRemindButton: View = findViewById(R.id.getTimeReminders)
        getTimeRemindButton.setOnClickListener {
            getTimeReminders()

        }
        if(schedule?.timeReminds!!.isNotEmpty()){
            items.addAll(schedule.timeReminds!!)
        }

        val layoutClickNote: View = findViewById(R.id.layoutClickNoteEdit)
        layoutClickNote.setOnClickListener {
            showEditTextDialog()
        }

        txtNote.text = habit?.description

        adapter = ItemTimeRemindAdapter(this, items, this)

        listView.adapter = adapter

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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

    private fun datePickerAction() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.MyDatePickerDialogTheme,
            { view, year, monthOfYear, dayOfMonth ->
                textDuDate.text =
                    (buildString {
                        append(dayOfMonth.toString())
                        append("/")
                        append((monthOfYear + 1))
                        append("/")
                        append(year)
                    })

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
                items.add(formattedTime)
                adapter.notifyDataSetChanged()

            },
            hour,
            minute,
            true
        ) // Use 24-hour format

        timePickerDialog.show()
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

    override fun onItemDelete(position: Int) {
        items.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    override fun onDataPassDaysPickOfMonthLy(days: List<String>, everyRepeat: Int) {
        daysPickMonthly.addAll(days)
        Toast.makeText(this, "Received date: $days, $everyRepeat", Toast.LENGTH_SHORT).show()
    }

    override fun onDataPassDate(days: List<String>, everyRepeat: Int) {
        daysPickWeekly.clear()
        daysPickWeekly.addAll(days)
        everyRepeatWeekly = everyRepeat
    }
}