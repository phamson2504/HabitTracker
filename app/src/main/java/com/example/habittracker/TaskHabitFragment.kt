package com.example.habittracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.adapter.SpinnerAdapter
import com.example.habittracker.adapter.TaskHabitsAdapter
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule

class TaskHabitFragment : Fragment() {
    private var tasksHabit = ArrayList<Habit>()
    private lateinit var habitDAOImpl: HabitDAOImpl
    private lateinit var dbHelper: DatabaseHelper
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
        tasksHabit = habitDAOImpl.getAllHabits() as ArrayList<Habit>

        var recyclerViewTaskHabit = view.findViewById<RecyclerView>(R.id.allTask)
        var taskHabitsAdapter = TaskHabitsAdapter(requireContext(),tasksHabit)
        recyclerViewTaskHabit.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewTaskHabit.adapter = taskHabitsAdapter

        val spinner = view.findViewById<Spinner>(R.id.spinnerTypeOfTask)
        val items = listOf("All habit", "Repeat daily", "Repeat Weekly", "Repeat Monthly", "Just one")

        val adapterSpinner =SpinnerAdapter(requireContext(), items)
        spinner.adapter = adapterSpinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newTask : ArrayList<Habit> = when(parent.getItemAtPosition(position).toString()){
                    "All habit" ->tasksHabit
                    "Repeat daily" -> tasksHabit.filter { it.schedule?.scheduleType.equals(Schedule.ScheduleEveryDayRepeat::class.java.simpleName) } as ArrayList<Habit>
                    "Repeat Weekly" ->tasksHabit.filter { it.schedule?.scheduleType.equals(Schedule.WeeklySchedule::class.java.simpleName) } as ArrayList<Habit>
                    "Repeat Monthly" ->tasksHabit.filter { it.schedule?.scheduleType.equals(Schedule.MonthlySchedule::class.java.simpleName) } as ArrayList<Habit>
                    else -> tasksHabit.filter { it.schedule?.scheduleType.equals(Schedule.ScheduleNotRepeat::class.java.simpleName) } as ArrayList<Habit>
                }
                taskHabitsAdapter = TaskHabitsAdapter(requireContext(),newTask)
                recyclerViewTaskHabit.layoutManager = LinearLayoutManager(requireContext())
                recyclerViewTaskHabit.adapter = taskHabitsAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

}