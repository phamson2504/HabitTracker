package com.example.habittracker.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.HandleHabitTouchListener
import com.example.habittracker.R
import com.example.habittracker.adapter.CalendarAdapter
import com.example.habittracker.adapter.HabitHandleCalendarAdapter
import com.example.habittracker.database.CompletionRecordDAO
import com.example.habittracker.database.CompletionRecordDAOImpl
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.CompletionRecord
import com.example.habittracker.model.Habit
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters


class HomeFragment : Fragment(), HabitHandleCalendarAdapter.OnHabitClickListener {
    private var daysOfMonth = ArrayList<LocalDate>()
    private var selectedDate: LocalDate = LocalDate.now()
    private var currentMonth: Int = 0
    private var isViewWeek: Boolean = false
    lateinit var monthOfYear: TextView
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private var recyclerView: RecyclerView? = null
    private var recyclerviewAdapter: HabitHandleCalendarAdapter? = null
    private var touchListener: HandleHabitTouchListener? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var habitDAOImpl: HabitDAOImpl
    private lateinit var completionRecordDAO: CompletionRecordDAO
    private val taskListHandle: ArrayList<HabitHandle> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dbHelper = DatabaseHelper(requireContext())
//        dbHelper.resetDatabase()

        habitDAOImpl = HabitDAOImpl(requireContext(), dbHelper)
        completionRecordDAO = CompletionRecordDAOImpl(requireContext(), dbHelper)

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        monthOfYear = view.findViewById(R.id.month_of_year)
        monthOfYear.text = selectedDate.format(formatter)

        setMonthView(view)
        val addHabitButton: AppCompatImageButton = view.findViewById(R.id.addHabit)
        addHabitButton.setOnClickListener {
            addHabit()
        }
        val previousMonthActionButton: AppCompatImageButton =
            view.findViewById(R.id.previousMonthAction)
        previousMonthActionButton.setOnClickListener {
            previousMonthAction(view)
        }
        val nextMonthActionButton: AppCompatImageButton = view.findViewById(R.id.nextMonthAction)
        nextMonthActionButton.setOnClickListener {
            nextMonthAction(view)
        }
        val upActionButton: AppCompatImageButton = view.findViewById(R.id.upAction)
        upActionButton.setOnClickListener {
            upAction(view)
        }
        // Setting up RecyclerView and attaching ItemTouchHelper
        setupRecyclerView(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.habitsRecyclerView)
        recyclerviewAdapter = HabitHandleCalendarAdapter(requireContext(), this)

        val taskList: ArrayList<Habit> = ArrayList()
        taskList.addAll(habitDAOImpl.getHabitsForToday(selectedDate) as ArrayList<Habit>)
        taskListHandle.clear()
        taskListHandle.addAll(
            completionRecordDAO.getCompletionRecordsByHabits(
                taskList,
                selectedDate
            )
        )

        recyclerviewAdapter?.setTaskList(taskListHandle)
        recyclerView?.setAdapter(recyclerviewAdapter)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        touchListener = HandleHabitTouchListener(requireActivity(), recyclerView!!)

        touchListener?.setSwipeable(
            R.id.rowFG,
            R.id.rowBG,
            object : HandleHabitTouchListener.OnSwipeOptionsListener {
                override fun onSwipeOption(position: Int) {
                    val habit = taskListHandle[position]

                    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                    val dateString = selectedDate.format(dateFormatter)

                    val intent = Intent(requireContext(), InforHabitActivity::class.java)
                    intent.putExtra("current_date", dateString)
                    intent.putExtra("HABIT", habit)
                    startActivity(intent)

                    recyclerView?.addOnItemTouchListener(touchListener!!)
                }
            })
    }


    private fun setMonthView(view: View) {
        setupRecyclerView(view)
        currentMonth = selectedDate.month.value

        monthOfYear.text = selectedDate.format(formatter)
        daysOfMonth = ArrayList()

        val recyclerView: RecyclerView = view.findViewById(R.id.calendarRecyclerView)

        daysInMonthArray()

        val layoutManager = GridLayoutManager(requireContext(), 7)
        recyclerView.layoutManager = layoutManager

        val calendarAdapter = CalendarAdapter(requireContext(), daysOfMonth, selectedDate)
        recyclerView.adapter = calendarAdapter

        calendarAdapter.setOnItemListener(object : CalendarAdapter.OnItemListener {
            override fun onItemClick(position: Int, day: LocalDate) {
                selectedDate = day
                setupRecyclerView(view)
                monthOfYear.text = selectedDate.format(formatter)
                if (selectedDate.month.value != currentMonth) {
                    setMonthView(view)
                }
//                val message = "Selected Date $day " + selectedDate.format(formatter)
//                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })


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

    private fun previousMonthAction(view: View?) {
        currentMonth -= 1
        selectedDate = selectedDate.minusMonths(1)
        view?.let { changeMonth(it) }
    }

    private fun nextMonthAction(view: View?) {
        currentMonth += 1
        selectedDate = selectedDate.plusMonths(1)
        view?.let { changeMonth(it) }
    }

    private fun changeMonth(view: View) {
        if (!isViewWeek) {
            setMonthView(view)
        } else {
            setWeekView(view)
        }
    }

    private fun upAction(view: View) {

        if (this.isViewWeek) {
            setMonthView(view)
            isViewWeek = false
        } else {
            setWeekView(view)
            isViewWeek = true

        }

    }

    private fun setWeekView(view: View) {
        setupRecyclerView(view)
        currentMonth = selectedDate.month.value
        monthOfYear.text = selectedDate.format(formatter)
        daysInMonthArray()

        val daysOfWeek: ArrayList<LocalDate> = ArrayList()

        val positionOfMonth = daysOfMonth.indexOf(selectedDate)

        val rowWeekOfMonth: Int = if (positionOfMonth == 0 || positionOfMonth % 7 >= 0) {
            positionOfMonth / 7 + 1
        } else {
            positionOfMonth / 7
        }
        val dateOfWeek = daysOfMonth.subList(rowWeekOfMonth * 7 - 7, rowWeekOfMonth * 7)
        daysOfWeek.addAll(dateOfWeek)

        val recyclerView: RecyclerView = view.findViewById(R.id.calendarRecyclerView)

        val layoutManager = GridLayoutManager(requireContext(), 7)
        recyclerView.layoutManager = layoutManager

        val calendarAdapter = CalendarAdapter(requireContext(), daysOfWeek, selectedDate)
        recyclerView.adapter = calendarAdapter

        calendarAdapter.setOnItemListener(object : CalendarAdapter.OnItemListener {
            override fun onItemClick(position: Int, day: LocalDate) {
                selectedDate = day
                setupRecyclerView(view)
                monthOfYear.text = selectedDate.format(formatter)
                if (selectedDate.month.value != currentMonth) {
                    setWeekView(view)
                }
                val message = "Selected Date $day " + selectedDate.format(formatter)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addHabit() {
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dateString = selectedDate.format(dateFormatter)

        val intent = Intent(requireContext(), ScheduleAddActivity::class.java)
        intent.putExtra("current_date", dateString)
        startActivity(intent)
    }

    override fun onClickHabitTask(habit: HabitHandle, position: Int) {
        if (habit.habit.schedule?.timeForHabit != 0 || habit.habit.schedule.numOfTime != 0) {
            showDaiLogCompleteHabit(habit.habit, habit.completionRecord, position)
        } else {
            val completionRecord = if (habit.completionRecord == null) {
                habit.habit.completionRecordForToday(
                    numOfTimesCompleted = 0,
                    timeForHabit = 0,
                    selectDate = selectedDate,
                    isComplete = true
                )
            } else if (habit.completionRecord?.isCompleted == 0) {
                habit.habit.completionRecordForToday(
                    numOfTimesCompleted = 0,
                    timeForHabit = 0,
                    selectDate = selectedDate,
                    isComplete = true
                )
            } else {
                habit.habit.completionRecordForToday(
                    numOfTimesCompleted = 0,
                    timeForHabit = 0,
                    selectDate = selectedDate,
                    isComplete = false
                )
            }
            if (habit.completionRecord == null) {
                completionRecordDAO.insertCompletionRecord(completionRecord)
            } else {
                completionRecordDAO.updateCompletionRecord(completionRecord, selectedDate)
            }
            habit.completionRecord = completionRecord
            taskListHandle[position] = habit
            recyclerviewAdapter?.updateItemImage(position)
        }
    }

    private fun showDaiLogCompleteHabit(
        habit: Habit,
        completionRecord: CompletionRecord?,
        position: Int
    ) {

        val dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dailog_edit_complete_habit1, null
        )
        val nameHabit = dialogView.findViewById<TextView>(R.id.textTitle)
        val numOfHabitEditText = dialogView.findViewById<EditText>(R.id.numOfHabitEditText)
        val numOfHabit = dialogView.findViewById<TextView>(R.id.numOfHabit)
        val numEditForHabit = dialogView.findViewById<TextView>(R.id.numEditForHabit)
        val typeOfHabit = dialogView.findViewById<TextView>(R.id.textView1)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        numEditForHabit.text = "0"
        if (habit.schedule?.numOfTime != 0) {
            numOfHabit.text = habit.schedule?.numOfTime.toString()
            if (completionRecord != null) {
                numEditForHabit.text = completionRecord.numOfTimesCompleted.toString()
            }
            typeOfHabit.text = "times"

        } else if (habit.schedule.timeForHabit != 0) {
            numOfHabit.text = habit.schedule.timeForHabit.toString()
            if (completionRecord != null) {
                numEditForHabit.text = completionRecord.timeForHabit.toString()
            }
            typeOfHabit.text = "minutes"
        }
        nameHabit.text = habit.name



        numOfHabitEditText.post {
            numOfHabitEditText.setText(numEditForHabit.text.toString())
            numOfHabitEditText.requestFocus()
            numOfHabitEditText.setSelection(0, numOfHabitEditText.text.length)
        }

        numOfHabitEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val inputText = p0.toString()
                if (inputText.isNotEmpty()) {
                    numEditForHabit.text = inputText
                } else {
                    numOfHabitEditText.setText("0")
                    numOfHabitEditText.setSelection(0, numOfHabitEditText.text.length)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        val okButton = dialogView.findViewById<TextView>(R.id.okButton)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)

        okButton.setOnClickListener {
            val completion: CompletionRecord
            if (completionRecord == null) {
                completion = if (habit.schedule?.numOfTime != 0) {
                    habit.completionRecordForToday(
                        numOfTimesCompleted = numOfHabitEditText.text.toString().toInt(),
                        timeForHabit = 0,
                        selectDate = selectedDate,
                        isSetTimeOrNumHabit = true
                    )
                } else {
                    habit.completionRecordForToday(
                        numOfTimesCompleted = 0,
                        timeForHabit = numOfHabitEditText.text.toString().toInt(),
                        selectDate = selectedDate,
                        isSetTimeOrNumHabit = true
                    )
                }
                Log.v("completionRecord", "" + completionRecord)
                completionRecordDAO.insertCompletionRecord(completion)

                taskListHandle[position].completionRecord = completion
                recyclerviewAdapter?.updateItemImage(position)
            } else {
                completion = if (habit.schedule?.numOfTime != 0) {
                    habit.completionRecordForToday(
                        numOfTimesCompleted = numOfHabitEditText.text.toString().toInt(),
                        timeForHabit = 0,
                        selectDate = selectedDate,
                        isSetTimeOrNumHabit = true
                    )
                } else {
                    habit.completionRecordForToday(
                        numOfTimesCompleted = 0,
                        timeForHabit = numOfHabitEditText.text.toString().toInt(),
                        selectDate = selectedDate,
                        isSetTimeOrNumHabit = true
                    )
                }
                Log.v("completionRecord", "" + completionRecord)
                completionRecordDAO.updateCompletionRecord(completion, selectedDate)

                taskListHandle[position].completionRecord = completion
                recyclerviewAdapter?.updateItemImage(position)
            }

            dialog.dismiss()
        }
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        touchListener?.let { recyclerView?.addOnItemTouchListener(it) }
    }
}