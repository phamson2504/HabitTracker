package com.example.habittracker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.model.Habit
import com.example.habittracker.model.Schedule

class TaskHabitsAdapter(
    private val context: Context,
    private val taskHabits: ArrayList<Habit>,
    private val listener: OnHabitClickListener
) : RecyclerView.Adapter<TaskHabitsAdapter.ViewHolder>(), Filterable {

    val factor = 0.7f

    private var filteredList: List<Habit> = taskHabits

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(context).inflate(R.layout.task_habit, parent , false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskHabit = filteredList[position]
        holder.txtName.text = taskHabit.name
        holder.txtTaskDesc.text = when{
            taskHabit.schedule is Schedule.WeeklySchedule -> "Weekly"
            taskHabit.schedule is Schedule.MonthlySchedule -> "Monthly"
            taskHabit.schedule is Schedule.ScheduleEveryDayRepeat -> "Daily"
            else -> "No repeat"
        }
        if (!taskHabit.icon.isNullOrBlank()){
            holder.iconHabit.text = taskHabit.icon
        }else{
            holder.iconHabit.text = taskHabit.name.first().toString()
        }
        if (taskHabit.color != null){
            val backgroundRow = holder.rowFG.background as GradientDrawable
            backgroundRow.setColor(Color.parseColor(taskHabit.color))
            val iconBackgroundColor = ColorUtils.blendARGB(Color.parseColor(taskHabit.color), Color.BLACK, 1 - factor)
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
        holder.rowFG.setOnClickListener {
            listener.onHabitClick(position)
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchText = constraint?.toString()?.lowercase() ?: ""

                filteredList = if (searchText.isEmpty()) {
                    taskHabits
                } else {
                    taskHabits.filter {
                        it.name.lowercase().contains(searchText)
                    }
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<Habit>
                notifyDataSetChanged()
            }
        }
    }

    fun updateList(filteredTasks: List<Habit>) {
        this.filteredList = filteredTasks
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val txtName = itemView.findViewById<TextView>(R.id.task_name)
        val txtTaskDesc = itemView.findViewById<TextView>(R.id.task_desc)
        val iconHabit: TextView = itemView.findViewById(R.id.txt_icon_habit)
        val layoutIconHabit: View = itemView.findViewById(R.id.layout_icon_habit)
        val rowFG : View = itemView.findViewById(R.id.rowFG)
    }
    interface OnHabitClickListener {
        fun onHabitClick(position: Int)
    }
}