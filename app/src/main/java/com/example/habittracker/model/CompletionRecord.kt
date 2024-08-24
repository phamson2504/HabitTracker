package com.example.habittracker.model

import java.io.Serializable

data class CompletionRecord(
    val date: String,
    val numOfTimesCompleted: Int = 0,
    val timeForHabit: Int = 0,
    val isCompleted: Int = 0,
    val habitId: Long,
    var currentOperationalStatus: Int = 1
): Serializable
