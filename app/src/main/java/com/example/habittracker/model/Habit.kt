package com.example.habittracker.model

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Habit(
    val id: Long? = null,
    val name: String,
    val description: String,
    val schedule: Schedule? = null,
): Serializable {
    fun completionRecordForToday(
        numOfTimesCompleted: Int,
        timeForHabit: Int,
        selectDate: LocalDate,
        isComplete : Boolean = false,
        isSetTimeOrNumHabit: Boolean = false
    ): CompletionRecord {
        val dateStr = selectDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val isCompleted: Int
        if (isSetTimeOrNumHabit){
            var rateCompleted = 0f
            if (numOfTimesCompleted != 0){
                rateCompleted = (numOfTimesCompleted).toFloat()/schedule?.numOfTime!!
            }
            else if (timeForHabit != 0){
                rateCompleted = (timeForHabit).toFloat()/ schedule?.timeForHabit!!
            }
            isCompleted = when{
                rateCompleted >= 1f -> 3
                else -> 0
            }
        }else{
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
}

