package com.example.habittracker.model

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.io.Serializable

data class Habit(
    val id: Long? = null,
    val name: String,
    val description: String,
    val schedule: Schedule? = null,
    val color: String? = null,
    val icon: String? = null,
) : Serializable {
    fun completionRecordForToday(
        numOfTimesCompleted: Int,
        timeForHabit: Int,
        selectDate: LocalDate,
        isComplete: Boolean = false,
        isSetTimeOrNumHabit: Boolean = false
    ): CompletionRecord {
        val dateStr = selectDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val isCompleted: Int
        if (isSetTimeOrNumHabit) {
            var rateCompleted = 0f
            if (numOfTimesCompleted != 0) {
                rateCompleted = (numOfTimesCompleted).toFloat() / schedule?.numOfTime!!
            } else if (timeForHabit != 0) {
                rateCompleted = (timeForHabit).toFloat() / schedule?.timeForHabit!!
            }
            isCompleted = when {
                rateCompleted >= 1f -> 3
                else -> 0
            }
        } else {
            isCompleted = if (isComplete) 3 else 0
        }

        return CompletionRecord(
            date = dateStr,
            numOfTimesCompleted = numOfTimesCompleted,
            timeForHabit = timeForHabit,
            isCompleted = isCompleted,
            habitId = id ?: 0L
        )
    }

    fun getCompletionRecordForToday(
        numOfTimesCompleted: Int,
        timeForHabit: Int,
        selectDate: String,
    ): CompletionRecord {
        val isCompleted: Int

        var rateCompleted = 0f
        if (numOfTimesCompleted != 0 && schedule?.numOfTime != 0) {
            rateCompleted = (numOfTimesCompleted).toFloat() / schedule?.numOfTime!!
        } else if (timeForHabit != 0 && schedule?.timeForHabit != 0) {
            rateCompleted = (timeForHabit).toFloat() / schedule?.timeForHabit!!
        }
        isCompleted = when {
            rateCompleted >= 1f -> 3
            else -> 0
        }

        return CompletionRecord(
            date = selectDate,
            numOfTimesCompleted = numOfTimesCompleted,
            timeForHabit = timeForHabit,
            isCompleted = isCompleted,
            habitId = id ?: 0L
        )
    }

}

