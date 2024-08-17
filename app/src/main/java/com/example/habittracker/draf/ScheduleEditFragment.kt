package com.example.habittracker.draf

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.habittracker.R
import com.example.habittracker.RepeatTaskMonthly
import com.example.habittracker.RepeatTaskWeekly
import com.example.habittracker.adapter.ItemTimeRemindAdapter
import com.example.habittracker.entity.HabitHandle
import com.google.android.material.switchmaterial.SwitchMaterial
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Calendar

class ScheduleEditFragment : Fragment(), ItemTimeRemindAdapter.OnItemDeleteListener {
    private lateinit var textDuDate: TextView
    private lateinit var listView: ListView
    private lateinit var adapter: ItemTimeRemindAdapter
    private val items = ArrayList<String>()
    private lateinit var titleHandle: TextView
    private lateinit var currentDate: LocalDate
    private var habitHandle: HabitHandle? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retainInstance = true

        textDuDate = view.findViewById(R.id.textDuDate)

        listView = view.findViewById(R.id.listViewTimeReminder)

        adapter = ItemTimeRemindAdapter(requireContext(), items, this)

        titleHandle = view.findViewById(R.id.txtTitleHandle)

        listView.adapter = adapter

        // Retrieve arguments



        //set Title Handle
        if (arguments?.getString("type_handle").equals("save")){
            val dateString = arguments?.getString("current_date")
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

            "New Habit".also { titleHandle.text = it }
            currentDate = dateString?.let {
                LocalDate.parse(it, dateFormatter)
            }!!
        }
        else{
            habitHandle = arguments?.getSerializable("HABIT") as? HabitHandle
            "Edit Habit".also { titleHandle.text = it }
            habitHandle?.let {
                Toast.makeText(requireContext(), "HABIT: $it", Toast.LENGTH_SHORT).show()
            }
        }
        // Use the LocalDate
        currentDate?.let {
            Toast.makeText(requireContext(), "Received date: $it", Toast.LENGTH_SHORT).show()
        }


        val textDailyClick: TextView = view.findViewById(R.id.dailyClick)

        textDailyClick.setOnClickListener {
            dailyClick()
        }
        val textWeeklyClick: TextView = view.findViewById(R.id.weeklyClick)
        val repeatTaskWeekly = RepeatTaskWeekly()
        textWeeklyClick.setOnClickListener {
            weeklyClick(repeatTaskWeekly)
        }
        val textMonthlyClick: TextView = view.findViewById(R.id.monthlyClick)
        val repeatTaskMonthly = RepeatTaskMonthly()

        textMonthlyClick.setOnClickListener {
            monthlyClick(repeatTaskMonthly)
        }

        val datePicker: View = view.findViewById(R.id.datePicker)
        datePicker.setOnClickListener {
            datePickerAction()
        }

        val myLayout: LinearLayout = view.findViewById(R.id.layout_repeat)
        val mySwitch: SwitchMaterial = view.findViewById(R.id.material_switch)
        mySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                myLayout.visibility = View.VISIBLE
                val params = myLayout.layoutParams
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT
                myLayout.layoutParams = params
                //set color
                mySwitch.thumbTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorSwitchThumbOn
                    )
                )
                mySwitch.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
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
                        requireContext(),
                        R.color.colorSwitchThumbOff
                    )
                )
                mySwitch.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorSwitchTrackOff
                    )
                )
            }
        }

        val getTimeRemindButton: View = view.findViewById(R.id.getTimeReminders)
        getTimeRemindButton.setOnClickListener {
            getTimeReminders()
        }


    }

    private fun dailyClick() {
        val fragment = childFragmentManager.findFragmentByTag("Weekly_TAG")
            ?: childFragmentManager.findFragmentByTag("Monthly_TAG")
        fragment?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .commit()
        }
    }

    private fun weeklyClick(repeatTaskWeekly: RepeatTaskWeekly) {
        val fragment = childFragmentManager.findFragmentByTag("Weekly_TAG")
        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .show(fragment)
                .commit()
        } else {
            childFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, repeatTaskWeekly, "Weekly_TAG")
                .commit()
        }
    }

    private fun monthlyClick(repeatTaskMonthly: RepeatTaskMonthly) {
        val fragment = childFragmentManager.findFragmentByTag("Monthly_TAG")

        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .show(fragment)
                .commit()
        } else {
            childFragmentManager.beginTransaction()
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
            requireContext(),
            R.style.MyDatePickerDialogTheme,
            { view, year, monthOfYear, dayOfMonth ->
                textDuDate.text =
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
            requireContext(),
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

    override fun onItemDelete(position: Int) {
        items.removeAt(position)
        adapter.notifyDataSetChanged()
    }
}