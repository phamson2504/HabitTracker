package com.example.habittracker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import org.threeten.bp.LocalDate

class HabitCalendarInfoAdapter(
    private val context: Context,
    private val calendarList: ArrayList<LocalDate>,
    private val firstDateInMonth: LocalDate,
    private val dayCompleteInMonth: MutableMap<String, Int>,
    private val dayOfMonthHabit: MutableList<LocalDate>?
) :
    RecyclerView.Adapter<HabitCalendarInfoAdapter.ViewHolder>()
{
    private var index = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =
            LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(inflater)
    }

    override fun getItemCount(): Int =calendarList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDate: LocalDate = LocalDate.now()
        val cal: LocalDate =calendarList[position]
        val displayMonth = cal.month
        val displayYear= cal.year
        val displayDay = cal.dayOfMonth

        holder.txtDayOfMonth.text = displayDay.toString();

        if (currentDate.isEqual(cal)){
            holder.txtDayOfMonth.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }

        if (position in 0..6) {
            holder.linearLayout.isEnabled = false
        }
        if ( firstDateInMonth.month != cal.month ){
            makeItemOtherMonth(holder)
        }
        if (dayOfMonthHabit?.contains(cal) == true &&  firstDateInMonth.month == cal.month){
            holder.linearLayout.setBackgroundResource(R.drawable.background_day_calendar_1)
        }
        if (dayCompleteInMonth.containsKey(displayDay.toString()) && dayOfMonthHabit?.contains(cal) == true
            &&  firstDateInMonth.month == cal.month){
            val complete = dayCompleteInMonth[displayDay.toString()]
            when (complete) {
                0 -> {
                    holder.linearLayout.setBackgroundResource(R.drawable.background_day_calendar_0)
                }
                3 -> {
                    holder.linearLayout.setBackgroundResource(R.drawable.background_day_calendar_3)
                }
            }
        }


    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val txtDayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.item_calendar_day)
    }

    private fun makeItemOtherMonth(holder: ViewHolder) {
        holder.txtDayOfMonth.setTextColor(Color.GRAY)
        holder.linearLayout.setBackgroundColor(Color.WHITE)
    }
}