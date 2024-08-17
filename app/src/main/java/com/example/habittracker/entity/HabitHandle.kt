package com.example.habittracker.entity

import com.example.habittracker.model.CompletionRecord
import com.example.habittracker.model.Habit
import java.io.Serializable

data class HabitHandle(
    val habit: Habit,
    var completionRecord: CompletionRecord? = null
) : Serializable