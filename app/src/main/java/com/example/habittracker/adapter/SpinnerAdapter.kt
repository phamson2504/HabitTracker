package com.example.habittracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.habittracker.R

class SpinnerAdapter (
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Use a simple layout or the same custom layout for the selected view
        val view = createViewFromResource(convertView, parent, position, R.layout.spinner_item)
        (view.findViewById<TextView>(R.id.spinner_text)).textSize = 18f // Set text size for selected item
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Use the custom layout for the dropdown items
        return createViewFromResource(convertView, parent, position, R.layout.spinner_item)
    }

    private fun createViewFromResource(convertView: View?, parent: ViewGroup, position: Int, resource: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val textView = view.findViewById<TextView>(R.id.spinner_text)
        textView.text = items[position]
        return view
    }
}