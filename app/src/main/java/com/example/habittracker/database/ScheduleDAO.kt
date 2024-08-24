package com.example.habittracker.database

import com.example.habittracker.model.Schedule
import java.time.LocalDate

interface ScheduleDAO {
    fun insertSchedule(schedule: Schedule): Long
    fun getScheduleByIdHabit(idHabit: Long):  Schedule?
    fun updateSchedule(schedule: Schedule): Int
    fun deleteSchedule(habitId: String): Int
}