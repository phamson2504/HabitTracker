package com.example.habittracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.habittracker.R
import com.example.habittracker.ui.ScheduleAddActivity
import com.example.habittracker.ui.ScheduleEditActivity

class ItemTimeRemindAdapter(
    private val context: Context,
    private val listTimeRemind: ArrayList<String>,
    private val listener: OnItemDeleteListener
) : BaseAdapter() {

    override fun getCount(): Int {
        return listTimeRemind.size
    }

    override fun getItem(position: Int): String {
        return listTimeRemind[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface OnItemDeleteListener {
        fun onItemDelete(position: Int)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_time_reminder, parent, false)

        val textViewItem: TextView = view.findViewById(R.id.itemTimeRemind)
        val buttonDelete: ImageButton = view.findViewById(R.id.viewButtonDelTimeRemind)

        val item = getItem(position)
        textViewItem.text = item

        buttonDelete.setOnClickListener {
            listener.onItemDelete(position)
        }
        return view
    }
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        if (context is ScheduleAddActivity) {
            (context as ScheduleAddActivity).setListViewHeightBasedOnItems()
        }else if (context is ScheduleEditActivity){
            (context as ScheduleEditActivity).setListViewHeightBasedOnItems()
        }
    }

}