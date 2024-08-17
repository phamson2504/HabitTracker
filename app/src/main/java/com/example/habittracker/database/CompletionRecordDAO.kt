package com.example.habittracker.database

import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.CompletionRecord
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule
import org.threeten.bp.LocalDate

interface CompletionRecordDAO {
    fun insertCompletionRecord(completionRecord: CompletionRecord): Long
    fun getCompletionRecordByHabitId(habitId: String, date: LocalDate): CompletionRecord?
    fun getCompletionRecordInMonthByHabitId(habitId: String, date: LocalDate): ArrayList<CompletionRecord>
    fun updateCompletionRecord(completionRecord: CompletionRecord, date: LocalDate): Int
    fun getCompletionRecordsByHabits(habits: ArrayList<Habit>, date: LocalDate): List<HabitHandle>
}