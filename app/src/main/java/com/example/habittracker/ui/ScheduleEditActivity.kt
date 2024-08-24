package com.example.habittracker.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.habittracker.R
import com.example.habittracker.RepeatTaskMonthly
import com.example.habittracker.RepeatTaskWeekly
import com.example.habittracker.adapter.ItemTimeRemindAdapter
import com.example.habittracker.conversation.OnDataPassDatePickOfWeekly
import com.example.habittracker.conversation.OnDataPassDaysPickOfMonthLy
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import com.google.android.material.switchmaterial.SwitchMaterial
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleEditActivity : AppCompatActivity(), ItemTimeRemindAdapter.OnItemDeleteListener,
    OnDataPassDaysPickOfMonthLy, OnDataPassDatePickOfWeekly {
    private lateinit var textDuDate: TextView
    private lateinit var textStartDate: TextView
    private lateinit var textNameHabit: TextView
    private lateinit var numOrTimeHabit: TextView
    private lateinit var txtNumOfTimes: TextView
    private lateinit var txtTimeForHabit: TextView
    private lateinit var txtTask: TextView
    private lateinit var txtNote: TextView

    private var timeForHabit = 0
    private var numOfTime = 0
    private lateinit var listView: ListView
    private lateinit var adapter: ItemTimeRemindAdapter
    private val timeReminds = ArrayList<String>()
    private val daysPickWeekly = ArrayList<String>()
    private lateinit var titleHandle: TextView
    private var habitHandle: HabitHandle? = null
    private val daysPickMonthly = ArrayList<String>()
    private var everyRepeatWeekly = 0
    private var everyRepeatMonthly = 0
    private lateinit var dataByTypeOfTime: MutableMap<Int, Boolean>
    private lateinit var habitDAO: HabitDAOImpl
    private val dbHelper = DatabaseHelper(this)
    private lateinit var mySwitch: SwitchMaterial
    private var selectedColorHex: String = "#D6E0E2"
    private lateinit var txtEmoji: TextView
    private lateinit var colorPicked : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule_edit)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
        // Optional: Adjust status bar icon color
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        habitDAO = HabitDAOImpl(this, dbHelper)

        textDuDate = findViewById(R.id.textDuDate)
        textStartDate = findViewById(R.id.textStartDate)
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

        textNameHabit.text = habitHandle?.habit?.name

        if (habitHandle?.habit?.schedule?.numOfTime  != 0){
            txtTimeForHabit.performClick()
            numOrTimeHabit.text = habitHandle?.habit?.schedule?.numOfTime.toString()
        }else if (habitHandle?.habit?.schedule?.timeForHabit != 0){
            txtNumOfTimes.performClick()
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
            repeatTaskMonthly = RepeatTaskMonthly.newInstance(schedule.dateInMonth, schedule.numOfMonthRepeat)

        }else if (schedule is Schedule.WeeklySchedule){
            repeatTaskWeekly = RepeatTaskWeekly.newInstance(
                schedule.daysInWeek,
                schedule.numOfWeekRepeat.toString() + " week"
            )
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
            textDuDate.text = convertDateToDisplay(schedule?.dueDay.toString())
        }


        val datePicker: View = findViewById(R.id.datePicker)
        datePicker.setOnClickListener {
            datePickerAction(textDuDate)
        }

        val startDatePicker: View = findViewById(R.id.datePickerStartDate)
        startDatePicker.setOnClickListener {
            datePickerAction(textStartDate)
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
            timeReminds.addAll(schedule.timeReminds!!)
        }

        val layoutClickNote: View = findViewById(R.id.layoutClickNoteEdit)
        layoutClickNote.setOnClickListener {
            showEditTextDialog()
        }


        txtNote.text = habit?.description

        adapter = ItemTimeRemindAdapter(this, timeReminds, this)

        listView.adapter = adapter

        textStartDate.setText(convertDateToDisplay(schedule.startDate))

        val txtSave: TextView = findViewById(R.id.saveEdit)
        txtSave.setOnClickListener {
            if (saveEditHabit(habit!!)){
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("ACTION", "NAVIGATE_TO_CALENDAR")
                startActivity(intent)
            }
        }
        if (habit?.color != null){
            selectedColorHex = habit.color
        }
        txtEmoji = findViewById(R.id.iconForHabitEdit)
        if (habit?.icon != null){
            txtEmoji.text = habit.icon
        }

        colorPicked = findViewById(R.id.colorHabitPickedEdit)
        val chooseEmojiClick =findViewById<View>(R.id.linearLayoutChoseEmojiEdit)
        chooseEmojiClick.setOnClickListener {
            showEmojiPickerDialog()
        }
        val choseColor :View = findViewById(R.id.choseColorEdit)
        choseColor.setOnClickListener {
            showColorPickerDialog()
        }

    }
    private fun showEmojiPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_emoji_picker, null)
        val emojiEditText = dialogView.findViewById<EditText>(R.id.emojiEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Pick an Emoji")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val emoji = emojiEditText.text.toString()
                if (isValidEmoji(emoji)) {
                    txtEmoji.text = emoji
                }else if (emoji == ""){
                    txtEmoji.text = "~"
                }
                else {
                    Toast.makeText(this, "Please enter a valid single emoji.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    private fun isValidEmoji(text: String): Boolean {
        val emojiRegex = Regex("[\\p{So}\\p{Cn}]") // Regex to match emojis
        return emojiRegex.containsMatchIn(text) && text.length == 2
    }

    private fun showColorPickerDialog() {
        val dialog = Dialog(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null)
        dialog.setContentView(view)

        val colorPicker = view.findViewById<ColorPicker>(R.id.colorPicker)
        val btnOk = view.findViewById<Button>(R.id.btnOk)

        colorPicker.addSVBar(view.findViewById(R.id.svbar))
        colorPicker.addOpacityBar(view.findViewById(R.id.opacitybar))
        colorPicker.setColor(Color.parseColor(selectedColorHex))

        colorPicker.onColorChangedListener = OnColorChangedListener { color ->
            // Handle color change
            selectedColorHex = String.format("#%06X", 0xFFFFFF and colorPicker.color)
        }

        btnOk.setOnClickListener {
            val background = colorPicked.background as GradientDrawable
            background.setColor(Color.parseColor(selectedColorHex))
            Toast.makeText(this,""+selectedColorHex,Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun saveEditHabit(habit: Habit) : Boolean {
        var schedule: Schedule? = null
        if (textNameHabit.text.isNullOrBlank()) {
            Toast.makeText(this, "Please enter name habit", Toast.LENGTH_SHORT).show()
            return false
        }
        val habitFinder = habitDAO.getHabitByName(textNameHabit.text.toString())
        if (habitFinder != null) {
            if (habitFinder.id != habit.id) {
                Toast.makeText(this, "Habit has existed", Toast.LENGTH_SHORT).show()
                return false
            }
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
                    convertToDate(textStartDate.text.toString()).plusWeeks(everyRepeatMonthly.toLong())
                        .format(formatter)
                }else{
                    convertDate(textDuDate.text.toString())
                }
                schedule = Schedule.MonthlySchedule(
                    convertDate(textStartDate.text.toString()),
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
                    convertToDate(textStartDate.text.toString()).plusWeeks(everyRepeatWeekly.toLong()).format(formatter)
                }else{
                    convertDate(textDuDate.text.toString())
                }
                schedule = Schedule.WeeklySchedule(
                    convertDate(textStartDate.text.toString()),
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
                    startDate = convertDate(textStartDate.text.toString()),
                    convertDate(textDuDate.text.toString()),
                    numOfTime = numOfTime,
                    timeForHabit = timeForHabit,
                    timeReminds = timeReminds
                )

            }else{
                schedule = Schedule.ScheduleEveryDayRepeat(
                    startDate = convertDate(textStartDate.text.toString()),
                    "",
                    numOfTime = numOfTime,
                    timeForHabit = timeForHabit,
                    timeReminds = timeReminds,
                    repeatInfinitely = 1
                )
            }

        } else {
            schedule = Schedule.ScheduleNotRepeat(
                startDate = convertDate(textStartDate.text.toString()),
                numOfTime = numOfTime,
                timeForHabit = timeForHabit,
                timeReminds = timeReminds
            )
        }
        val editHabit : Habit
        // create habit
        if (schedule != null) {
            var iconHabit  = ""
            if (txtEmoji.text.toString() != "~"){
                iconHabit = txtEmoji.text.toString()
            }
            editHabit = Habit(
                id = habit.id,
                name = textNameHabit.text.toString(),
                description = txtNote.text.toString(),
                schedule = schedule,
                color = selectedColorHex,
                icon = iconHabit
            )

            habitDAO.updateHabit(editHabit) { exception ->
                Log.e("DatabaseError", "Failed to edit habit and schedule: ${exception.message}")
            }

        }
        return true
    }

    private fun convertDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    }
    private fun convertDateToDisplay(dateString: String): String {
        val outputFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    }
    private fun convertToDate(dateString: String) : LocalDate {
        val inputFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        return LocalDate.parse(dateString, inputFormatter)
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
                timeReminds.add(formattedTime)
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
    }
}