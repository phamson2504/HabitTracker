package com.example.habittracker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import org.threeten.bp.LocalDate
import java.util.*


class CalendarAdapter(
    private val context: Context,
    private val daysOfMonth: ArrayList<LocalDate>,
    private val currentDateSelected: LocalDate
) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private var mListener: OnItemListener? = null
    private var index = -1
    private var selectCurrentDate = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =
            LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(inflater,mListener!!)
    }

    override fun getItemCount(): Int = daysOfMonth.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDate: LocalDate = LocalDate.now()
        val cal: LocalDate =daysOfMonth[position]
        val displayMonth = cal.month
        val displayYear= cal.year
        val displayDay = cal.dayOfMonth

        holder.txtDayOfMonth.text = displayDay.toString();

        if (currentDate.isEqual(cal)){
            holder.txtDayOfMonth.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }

        holder.linearLayout.setOnClickListener {
            index = holder.adapterPosition
            selectCurrentDate = false
            holder.onItemListener.onItemClick(index,daysOfMonth[position])
            notifyDataSetChanged()
        }

        if (position in 0..6) {
            holder.linearLayout.isEnabled = false
        }
        if ( currentDateSelected.month != cal.month ){
            makeItemOtherMonth(holder)
        }

        if (index == position){
            makeItemSelected(holder)
        }
        else {
            if (currentDateSelected.dayOfMonth == displayDay &&
                    currentDateSelected.month == displayMonth &&
                        currentDateSelected.year == displayYear &&
                            selectCurrentDate){
                makeItemSelected(holder)
            }
            else {
                makeItemDefaultOfMonth(holder)
                if ( currentDateSelected.month != daysOfMonth[position].month ){
                    makeItemOtherMonth(holder)
                }
            }
        }


    }

    inner class ViewHolder(itemView: View, val onItemListener: OnItemListener) :
        RecyclerView.ViewHolder(itemView) {
        val txtDayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.item_calendar_day)
    }

    interface OnItemListener {
        fun onItemClick(position: Int, day :LocalDate)
    }

    fun setOnItemListener(listener: OnItemListener) {
        mListener = listener
    }

    private fun makeItemSelected(holder: ViewHolder) {
        holder.txtDayOfMonth.setTextColor(Color.parseColor("#FFFFFF"))
        holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.light_blue))
        holder.linearLayout.isEnabled = false
    }
    private fun makeItemOtherMonth(holder: ViewHolder) {
        holder.txtDayOfMonth.setTextColor(Color.GRAY)
        holder.linearLayout.setBackgroundColor(Color.WHITE)
    }
    private fun makeItemDefaultOfMonth (holder: ViewHolder) {
        holder.txtDayOfMonth.setTextColor(Color.BLACK)
        holder.linearLayout.setBackgroundColor(Color.WHITE)
        holder.linearLayout.isEnabled = true
    }

}