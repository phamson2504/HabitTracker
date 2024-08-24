package com.example.habittracker.database

import com.example.habittracker.model.Habit
import java.time.LocalDate

interface HabitDAO {
    fun insertHabit(habit: Habit, onError: (Exception) -> Unit): Boolean
    fun getHabit(id: Long): Habit?
    fun getHabitByName(nameHabit: String): Habit?
    fun getAllHabits(): List<Habit>
    fun getHabitsByDate(selectDate: String): List<Habit>
    fun updateHabit(habit: Habit, onError: (Exception) -> Unit): Boolean
    fun getHabitsByDate(startDate: String, endDate: String): MutableList<Habit>
    fun  deleteHabit(habitId: String, onError: (Exception) -> Unit): Boolean
}