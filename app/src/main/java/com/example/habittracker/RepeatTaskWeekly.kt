package com.example.habittracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.habittracker.conversation.OnDataPassDatePickOfWeekly

class RepeatTaskWeekly : Fragment() {
    private val selectedDays = mutableListOf<String>()
    private var dataPassDaysPickOfWeekly : OnDataPassDatePickOfWeekly? = null
    private var pickedListDate = ArrayList<String>()
    private lateinit var selectedEveryRepeat: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_repeat_task_weekly, container, false)

        val spinner: Spinner = view.findViewById(R.id.spinner)
        val items = listOf("1 week", "2 week", "3 week", "4 week", "5 week", "6 week")

        val adapterSpinner =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedEveryRepeat = parent.getItemAtPosition(position).toString()
                dataPassDaysPickOfWeekly?.onDataPassDate(
                    selectedDays,
                    selectedEveryRepeat.split(" ")[0].toInt()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        val dayButtonMap = mapOf(
            R.id.mon to "Monday",
            R.id.tu to "Tuesday",
            R.id.we to "Wednesday",
            R.id.th to "Thursday",
            R.id.fi to "Friday",
            R.id.sa to "Saturday",
            R.id.su to "Sunday"
        )
        dayButtonMap.forEach { (textId, day) ->
            val textClick = view.findViewById<TextView>(textId)
            textClick.setOnClickListener {
                handleDaySelection(textClick, day)
            }
        }
        arguments?.let {
            pickedListDate = it.getStringArrayList(ARG_REPEAT_TASK_WEEKLY) as ArrayList<String>
            selectedEveryRepeat = it.getString(ARG_SELECTED_EVERY_REPEAT) as String
        }
        if (pickedListDate.size != 0){
            dayButtonMap.forEach { (textId, day) ->
                val textClick = view.findViewById<TextView>(textId)
                if (pickedListDate.contains(day)){
                    handleDaySelection(textClick, day)
                }
            }
        }

        return view
    }
    private fun handleDaySelection(textClick: TextView, day: String) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            textClick.setBackgroundResource(R.drawable.circular_background)
        } else {
            selectedDays.add(day)
            textClick.setBackgroundResource(R.drawable.circular_background_select)
        }
        dataPassDaysPickOfWeekly?.onDataPassDate(
            selectedDays, selectedEveryRepeat.split(" ")[0].toInt()
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassDaysPickOfWeekly = context as OnDataPassDatePickOfWeekly
    }

    override fun onDetach() {
        super.onDetach()
        dataPassDaysPickOfWeekly = null
    }
    companion object{
        private const val  ARG_REPEAT_TASK_WEEKLY = "repeat_task_weekly"
        private const val  ARG_SELECTED_EVERY_REPEAT = "selected_every_repeat"
        @JvmStatic
        fun newInstance(weeklyList: List<String>?, selectedEveryRepeat: String) = RepeatTaskWeekly().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_REPEAT_TASK_WEEKLY, weeklyList?.let { ArrayList(it) })
                putString(ARG_SELECTED_EVERY_REPEAT, selectedEveryRepeat)
            }
        }
    }
}