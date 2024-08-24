package com.example.habittracker.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.adapter.TaskHabitsAdapter
import com.example.habittracker.database.CompletionRecordDAO
import com.example.habittracker.database.CompletionRecordDAOImpl
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class TaskHabitFragment : Fragment(), TaskHabitsAdapter.OnHabitClickListener {
    private var tasksHabit = ArrayList<Habit>()
    private lateinit var habitDAOImpl: HabitDAOImpl
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var txtSearch: EditText
    private lateinit var closeIcon: View
    private lateinit var searchDropdownLayout: LinearLayout
    private lateinit var searchIcon: ImageView
    private lateinit var taskHabitsAdapter : TaskHabitsAdapter
    private lateinit var listCheckedBox : MutableMap<String,Boolean>
    private lateinit var completionRecordDAO: CompletionRecordDAO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        habitDAOImpl = HabitDAOImpl(requireContext(), dbHelper)
        completionRecordDAO = CompletionRecordDAOImpl(requireContext(), dbHelper)

        tasksHabit = habitDAOImpl.getAllHabits() as ArrayList<Habit>

        closeIcon = view.findViewById(R.id.closeSearch)
        txtSearch = view.findViewById(R.id.txtSearch)
        val recyclerViewTaskHabit = view.findViewById<RecyclerView>(R.id.allTask)
        taskHabitsAdapter = TaskHabitsAdapter(requireContext(),tasksHabit, this)
        recyclerViewTaskHabit.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewTaskHabit.adapter = taskHabitsAdapter

        searchDropdownLayout = view.findViewById(R.id.searchDropdownLayout)
        searchIcon = view.findViewById(R.id.imageViewSearch)

        searchIcon.setOnClickListener {
            if (searchDropdownLayout.visibility == View.GONE) {
                searchDropdownLayout.visibility = View.VISIBLE
            } else {
                searchDropdownLayout.visibility = View.GONE
            }
        }
        closeIcon.setOnClickListener {
            searchDropdownLayout.visibility = View.GONE
            txtSearch.text.clear()
            taskHabitsAdapter.filter.filter("")
        }
        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Perform search when text is changed
                taskHabitsAdapter.filter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        listCheckedBox = mutableMapOf(
            "dailySelected" to false,
            "weeklySelected" to false,
            "monthlySelected" to false,
            "noRepeatSelect" to false
        )

        val showFilterHabit = view.findViewById<ImageView>(R.id.showFilterHabit)
        showFilterHabit.setOnClickListener {
            showCheckBoxDialog()
        }
    }
    private fun showCheckBoxDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_task_habit, null)

        val checkBoxDaily = dialogView.findViewById<CheckBox>(R.id.checkBoxDaily)
        val checkBoxWeekly = dialogView.findViewById<CheckBox>(R.id.checkBoxWeekly)
        val checkBoxMonthly = dialogView.findViewById<CheckBox>(R.id.checkBoxMonthly)
        val checkBoxNoRepeat = dialogView.findViewById<CheckBox>(R.id.checkBoxNoRepeat)

        if ( listCheckedBox["dailySelected"] == true){
            checkBoxDaily.isChecked = true
        }
        if (listCheckedBox["weeklySelected"] == true){
            checkBoxWeekly.isChecked = true
        }
        if (listCheckedBox["monthlySelected"] == true){
            checkBoxMonthly.isChecked = true
        }
        if (listCheckedBox["noRepeatSelect"] == true){
            checkBoxNoRepeat.isChecked = true
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                listCheckedBox["dailySelected"] = checkBoxDaily.isChecked
                listCheckedBox["weeklySelected"] = checkBoxWeekly.isChecked
                listCheckedBox["monthlySelected"]  = checkBoxMonthly.isChecked
                listCheckedBox["noRepeatSelect"]  = checkBoxNoRepeat.isChecked

                filterTasks(listCheckedBox)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        // Show the dialog
        dialogBuilder.create().show()
    }
    private fun filterTasks(listCheckedBox:MutableMap<String,Boolean>) {
        // Assuming you have a list of tasks or habits
        var filteredTasks = tasksHabit.filter { task ->
            (listCheckedBox["dailySelected"] == true && task.schedule is Schedule.ScheduleEveryDayRepeat)
                    || (listCheckedBox["weeklySelected"] == true  && task.schedule is Schedule.WeeklySchedule)
                    || (listCheckedBox["monthlySelected"] == true && task.schedule is Schedule.MonthlySchedule)
                    || (listCheckedBox["noRepeatSelect"] == true && task.schedule is Schedule.ScheduleNotRepeat)
        }
        val allUnchecked = listCheckedBox.all { !it.value }
        if (allUnchecked) {
            filteredTasks = tasksHabit
        }
        taskHabitsAdapter.updateList(filteredTasks)
    }

    override fun onHabitClick(position: Int) {
        val taskListHandle: ArrayList<HabitHandle> = ArrayList()
        taskListHandle.addAll(
            completionRecordDAO.getCompletionRecordsByHabits(
                tasksHabit,
                LocalDate.now()
            )
        )

        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dateString = LocalDate.now().format(dateFormatter)

        val intent = Intent(requireContext(), InforHabitActivity::class.java)
        intent.putExtra("current_date", dateString)
        intent.putExtra("HABIT", taskListHandle[position])
        startActivity(intent)
    }

}