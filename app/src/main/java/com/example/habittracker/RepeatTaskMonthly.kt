package com.example.habittracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.adapter.DayForMonthlyAdapter
import com.example.habittracker.conversation.OnDataPassDaysPickOfMonthLy
import com.example.habittracker.conversation.OnDayClickListener


class RepeatTaskMonthly : Fragment(), OnDayClickListener {
    private var daysPickForMonthly = ArrayList<String>()
    private var pickedListDate = ArrayList<String>()
    private var dataPassDaysPickOfMonthLy: OnDataPassDaysPickOfMonthLy? = null
    private lateinit var selectedEveryRepeat: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_repeat_task_monthly, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewMonthly)
        recyclerView.layoutManager =
            GridLayoutManager(requireContext(), 7) // 7 columns for days of the week

        val numbersListDay = (1..31).map { it.toString() }
        val days: ArrayList<String> = ArrayList(numbersListDay)
        days.add("last")
        arguments?.let {
            pickedListDate = it.getStringArrayList(ARG_REPEAT_TASK_MONTHLY) as ArrayList<String>
        }

        val adapter = DayForMonthlyAdapter(requireContext(), days, this, pickedListDate)
        recyclerView.adapter = adapter

        val spinner: Spinner = view.findViewById(R.id.spinner)
        val items = listOf("1 month", "2 month", "3 month", "4 month", "5 month", "6 month")

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
                dataPassDaysPickOfMonthLy?.onDataPassDaysPickOfMonthLy(
                    daysPickForMonthly,
                    selectedEveryRepeat.split(" ")[0].toInt()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }



        return view
    }

    override fun onDayClick(days: MutableList<String>) {
        daysPickForMonthly.clear()
        daysPickForMonthly.addAll(days)
        dataPassDaysPickOfMonthLy?.onDataPassDaysPickOfMonthLy(
            daysPickForMonthly,
            selectedEveryRepeat.split(" ")[0].toInt()
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassDaysPickOfMonthLy = context as OnDataPassDaysPickOfMonthLy
    }

    override fun onDetach() {
        super.onDetach()
        dataPassDaysPickOfMonthLy = null
    }
    companion object {
        private const val ARG_REPEAT_TASK_MONTHLY = "repeat_task_monthly"
        @JvmStatic
        fun newInstance(taskList: List<String>?) = RepeatTaskMonthly().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_REPEAT_TASK_MONTHLY, taskList?.let { ArrayList(it) })
            }
        }
    }

}