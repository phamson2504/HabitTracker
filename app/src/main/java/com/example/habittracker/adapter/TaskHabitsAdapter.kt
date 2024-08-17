package com.example.habittracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.model.Habit

class TaskHabitsAdapter(
    private val context: Context,
    private val taskHabits: ArrayList<Habit>
) : RecyclerView.Adapter<TaskHabitsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(context).inflate(R.layout.task_habit, parent , false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = taskHabits.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskHabit = taskHabits[position]
        holder.txtName.text = taskHabit.name
        holder.txtTaskDesc.text = when{
            taskHabit.schedule?.numOfTime != 0 ->  taskHabit.schedule?.numOfTime.toString()+" times"
            taskHabit.schedule?.timeForHabit != 0 ->  taskHabit.schedule.timeForHabit.toString()+" minutes"
            else -> null
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val txtName = itemView.findViewById<TextView>(R.id.task_name)
        val txtTaskDesc = itemView.findViewById<TextView>(R.id.task_desc)
    }
}