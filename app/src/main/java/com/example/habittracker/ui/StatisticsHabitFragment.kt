package com.example.habittracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.habittracker.R
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.database.DatabaseHelper
import com.example.habittracker.database.HabitDAOImpl
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter


class StatisticsHabitFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var habitDAO: HabitDAOImpl
    private var listHabit = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_statistics_habit, container, false)

        // Initialize LineChart
        val dbHelper = DatabaseHelper(requireContext())
        lineChart = view.findViewById(R.id.lineChart)
        habitDAO = HabitDAOImpl(requireContext(), dbHelper)

        val startDate = "2024-08-23"
        val endDate = "2024-08-26"
        listHabit = habitDAO.getHabitsByDate(startDate, endDate)
        val countHabitByDate = countHabitOccurrencesByDate(listHabit,startDate,endDate)

        // Prepare data entries
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 75f)) // Week 1, 75% complete
        entries.add(Entry(1f, 60f)) // Week 2, 60% complete
        entries.add(Entry(2f, 90f)) // Week 3, 90% complete
        entries.add(Entry(3f, 80f)) // Week 4, 80% complete

        // Create a dataset with the entries
        val lineDataSet = LineDataSet(entries, "Weekly Completion")
        lineDataSet.setDrawValues(true) // Show the values on the chart
        lineDataSet.valueTextSize = 12f // Text size for the values
        lineDataSet.color = requireContext().getColor(R.color.teal_700) // Line color
        lineDataSet.setCircleColor(requireContext().getColor(R.color.purple_500)) // Point color
        lineDataSet.circleRadius = 5f

        // Create LineData object with the dataset
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Customize the X-axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Ensure x-axis shows each week
        xAxis.labelRotationAngle = 45f

        // Set custom labels for specific weeks
        val weekLabels = arrayOf("Week 1", "Week 3", "Week 4", "Week 5")
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() < weekLabels.size) {
                    weekLabels[value.toInt()]
                } else {
                    value.toString()
                }
            }
        }
        // Set label rotation angle for slanted text
        xAxis.labelRotationAngle = 0f

        // Customize the Y-axis
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.axisMinimum = 0f // Y-axis starts at 0%
        yAxisLeft.axisMaximum = 100f // Y-axis ends at 100%
        lineChart.axisRight.isEnabled = false // Disable right Y-axis

        // Customize the chart appearance
        lineChart.description.text = "Weekly Habit Completion"
        lineChart.legend.isEnabled = true

        // Refresh the chart
        lineChart.invalidate()

        return view
    }

    fun countHabitOccurrencesByDate(
        habits: List<Habit>,
        startDate: String,
        endDate: String
    ): Map<String, Int> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDateObj = LocalDate.parse(startDate, dateFormatter)
        val endDateObj = LocalDate.parse(endDate, dateFormatter)

        val occurrencesByDate = mutableMapOf<String, Int>()

        var currentDate = startDateObj

        while (!currentDate.isAfter(endDateObj)) {
            val currentDateStr = currentDate.format(dateFormatter)
            var occurrencesForDate = 0

            habits.forEach { habit ->
                val schedule = habit.schedule
                val habitStartDate = LocalDate.parse(schedule?.startDate, dateFormatter)
                val habitDueDate = LocalDate.parse(schedule?.dueDay, dateFormatter)

                if (habitDueDate.isBefore(currentDate) || habitStartDate.isAfter(currentDate)) {
                    return@forEach
                }

                val count = when (schedule) {
                    is Schedule.ScheduleNotRepeat -> {
                        if (habitStartDate == currentDate) 1 else 0
                    }
                    is Schedule.ScheduleEveryDayRepeat -> {
                        if ((schedule.repeatInfinitely == 1 || habitDueDate.isAfter(currentDate))) 1 else 0
                    }
                    is Schedule.WeeklySchedule -> {
                        val dayOfWeek = currentDate.dayOfWeek.value
                        if (schedule.daysInWeek?.map { it.toInt() }?.contains(dayOfWeek) == true) 1 else 0
                    }
                    is Schedule.MonthlySchedule -> {
                        val dayOfMonth = currentDate.dayOfMonth
                        if (schedule.dateInMonth?.map { it.toInt() }?.contains(dayOfMonth) == true) 1 else 0
                    }
                    else -> 0
                }

                occurrencesForDate += count
            }

            occurrencesByDate[currentDateStr] = occurrencesForDate
            currentDate = currentDate.plusDays(1)
        }
        return occurrencesByDate
    }
}