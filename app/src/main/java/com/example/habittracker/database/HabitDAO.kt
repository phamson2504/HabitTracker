package com.example.habittracker.database

import com.example.habittracker.model.Habit
import java.time.LocalDate

interface HabitDAO {
    fun insertHabit(habit: Habit, onError: (Exception) -> Unit): Boolean
    fun getHabit(id: Long): Habit?
    fun getHabitByName(nameHabit: String): Habit?
    fun getAllHabits(): List<Habit>
    fun getHabitsByDate(selectDate: String): List<Habit>
    fun updateHabit(habit: Habit): Int
    fun deleteHabit(id: Long): Int
}