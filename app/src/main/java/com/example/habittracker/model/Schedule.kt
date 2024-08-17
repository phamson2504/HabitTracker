package com.example.habittracker.model

import java.io.Serializable

sealed class Schedule : Serializable{
    abstract val startDate: String
    abstract val dueDay : String
    abstract val numOfTime : Int
    abstract val timeForHabit : Int
    abstract val timeReminds: List<String>?
    abstract val scheduleType: String
    abstract var habitId : Long?

    data class ScheduleNotRepeat(
        override val startDate: String,
        override val numOfTime : Int = 0,
        override val timeForHabit : Int = 0,
        override var habitId : Long? = null,
        override val timeReminds: List<String>? = null,
    ) : Schedule(){
        override val dueDay = startDate
        override val scheduleType: String = ScheduleNotRepeat::class.java.simpleName
    }
    data class ScheduleEveryDayRepeat(
        override val startDate: String,
        override val dueDay : String,
        override val numOfTime : Int = 0,
        override val timeForHabit : Int = 0,
        override val timeReminds: List<String>? = null,
        override var habitId : Long? = null,
        val repeatInfinitely : Int = 0
    ) : Schedule() {

        override val scheduleType: String = ScheduleEveryDayRepeat::class.java.simpleName
    }

    data class WeeklySchedule(
        override val startDate: String,
        override val dueDay : String,
        override val numOfTime : Int = 0,
        override val timeForHabit : Int = 0,
        override val timeReminds: List<String>? = null,
        val daysInWeek : List<String>?,
        val numOfWeekRepeat : Int,
        override var habitId : Long? = null
    ): Schedule(){
        override val scheduleType: String = WeeklySchedule::class.java.simpleName
    }

    data class MonthlySchedule(
        override val startDate: String,
        override val dueDay : String,
        override val numOfTime : Int = 0,
        override val timeForHabit : Int = 0,
        override val timeReminds: List<String>? = null,
        val dateInMonth : List<String>?,
        val numOfMonthRepeat : Int,
        override var habitId : Long? = null
    ): Schedule(){
        override val scheduleType: String = MonthlySchedule::class.java.simpleName
    }
}
