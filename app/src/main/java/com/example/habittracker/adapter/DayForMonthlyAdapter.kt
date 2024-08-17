package com.example.habittracker.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.adapter.CalendarAdapter.ViewHolder
import com.example.habittracker.conversation.OnDayClickListener

class DayForMonthlyAdapter(
    private val context: Context,
    private val days: List<String>,
    private val listener: OnDayClickListener,
    private val daysMonthlyPicked: List<String>,
) : RecyclerView.Adapter<DayForMonthlyAdapter.DayViewHolder>() {

    private val daysSelect : MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.dayTextView.text = day
        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                if (!daysSelect.contains(day)){
                    daysSelect.add(day)
                    makeItemSelected(holder)
                }else{
                    daysSelect.remove(day)
                    makeItemDefaultOfMonth(holder)
                }
                listener.onDayClick(daysSelect)
            }
        }
        if (daysMonthlyPicked.contains(day)){
            daysSelect.add(day)
            makeItemSelected(holder)
        }
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)  {
        val dayTextView : TextView = itemView.findViewById(R.id.cellDayText)
        val itemCalendarDay : LinearLayout = itemView.findViewById(R.id.item_calendar_day)
    }
    private fun makeItemSelected(holder: DayViewHolder) {
        holder.dayTextView.setTextColor(Color.parseColor("#FFFFFF"))
        holder.itemCalendarDay.setBackgroundColor(ContextCompat.getColor(context, R.color.light_blue))
        holder.itemCalendarDay.isEnabled = false
    }
    private fun makeItemDefaultOfMonth (holder: DayViewHolder) {
        holder.dayTextView.setTextColor(Color.BLACK)
        holder.itemCalendarDay.setBackgroundColor(Color.WHITE)
        holder.itemCalendarDay.isEnabled = true
    }

}