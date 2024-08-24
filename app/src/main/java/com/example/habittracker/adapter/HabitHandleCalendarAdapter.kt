package com.example.habittracker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.entity.HabitHandle
import com.example.habittracker.model.Habit

class HabitHandleCalendarAdapter(
    private val mContext: Context,
    private val onClickHabitListener: OnHabitClickListener
) :
    RecyclerView.Adapter<HabitHandleCalendarAdapter.MyViewHolder>() {
    private var habitList: List<HabitHandle>
    val factor = 0.7f
    init {
        habitList = ArrayList<HabitHandle>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.task_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val habit: HabitHandle = habitList[position]
        holder.tvTaskName.text = habit.habit.name
        if ( habit.habit.schedule?.numOfTime != 0 ){
            val numOfHabit = if(habit.completionRecord != null) habit.completionRecord?.numOfTimesCompleted.toString() else "0"
            holder.tvTaskDesc.text = buildString {
                append(numOfHabit)
                append("/")
                append(habit.habit.schedule?.numOfTime.toString())
                append(" times")
            }
        } else if ( habit.habit.schedule.timeForHabit != 0){
            val timeOfHabit = if(habit.completionRecord != null) habit.completionRecord?.timeForHabit.toString() else "0"
            holder.tvTaskDesc.text = buildString {
                append(timeOfHabit)
                append("/")
                append(habit.habit.schedule.timeForHabit.toString())
                append(" minutes")
            }
        }else{
            holder.tvTaskDesc.text = "Not Complete"
        }

        holder.rowFG.setOnClickListener {
            onClickHabitListener.onClickHabitTask(habit, position)
        }

        if (habit.completionRecord?.isCompleted == 0){
            holder.iconHandle.setImageResource(R.drawable.backgroud_uncheck_swipe_habit)
        }

        if ( habit.habit.schedule?.numOfTime != 0 || habit.habit.schedule.timeForHabit != 0){
            holder.iconHandle.setImageResource(R.drawable.plus_circle)
            if (habit.completionRecord != null){
                habit.completionRecord = habit.habit.getCompletionRecordForToday(
                    habit.completionRecord!!.numOfTimesCompleted,
                    habit.completionRecord!!.timeForHabit,
                    habit.completionRecord!!.date
                )
            }
        }
        if (habit.completionRecord?.isCompleted == 3){
                holder.iconHandle.setImageResource(R.drawable.complete_icon)
                if (habit.habit.schedule?.numOfTime ==0 && habit.habit.schedule.timeForHabit ==0) holder.tvTaskDesc.text = "Complete"
        }
        if (!habit.habit.icon.isNullOrBlank()){
            holder.iconHabit.text = habit.habit.icon
        }else{
            holder.iconHabit.text = habit.habit.name.first().toString()
        }
        if (habit.habit.color != null){
            val backgroundRow = holder.rowFG.background as GradientDrawable
            backgroundRow.setColor(Color.parseColor(habit.habit.color))
            val iconBackgroundColor = ColorUtils.blendARGB(Color.parseColor(habit.habit.color), Color.BLACK, 1 - factor)
            val iconBackground = holder.layoutIconHabit.background as GradientDrawable
            iconBackground.setColor(iconBackgroundColor)
        }else{
            val defaultBackgroundColor = Color.parseColor("#D6E0E2")
            val backgroundRow = holder.rowFG.background as GradientDrawable
            backgroundRow.setColor(defaultBackgroundColor)

            val defaultIconBackground = Color.parseColor("#A8C9D0")
            val iconBackground = holder.layoutIconHabit.background as GradientDrawable
            iconBackground.setColor(defaultIconBackground)
        }

    }

    override fun getItemCount(): Int {
        return habitList.size
    }

    fun setTaskList(habitList: List<HabitHandle>) {
        this.habitList = habitList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val tvTaskName: TextView = view.findViewById(R.id.task_name)
        val tvTaskDesc: TextView = view.findViewById(R.id.task_desc)
        val rowFG : View = view.findViewById(R.id.rowFG)
        val iconHandle: ImageView = view.findViewById(R.id.plus_circle)
        val iconHabit: TextView = view.findViewById(R.id.txt_icon_habit)
        val layoutIconHabit: View = view.findViewById(R.id.layout_icon_habit)
    }

    interface OnHabitClickListener {
        fun onClickHabitTask(habit: HabitHandle , position: Int)
    }
    fun updateItemImage(position: Int) {
        notifyItemChanged(position)
    }
}