package com.example.habittracker.draf

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.example.habittracker.R
import com.example.habittracker.entity.HabitHandle

class InformationHabitFragment : Fragment() {

    private var habitHandle: HabitHandle? = null
    private lateinit var clickHabitEdit : ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            habitHandle = it.getSerializable("HABIT") as? HabitHandle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_information_habit, container, false)
        Toast.makeText(context,""+habitHandle,Toast.LENGTH_SHORT).show()

        clickHabitEdit = view.findViewById(R.id.clickHabitEdit)
        clickHabitEdit.setOnClickListener {
            val args = Bundle().apply {
                putSerializable("HABIT", habitHandle)
            }
            val scheduleEditFragment = ScheduleEditFragment().apply {
                arguments = args
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_frame, scheduleEditFragment)
                .addToBackStack(null)
                .commit()
        }
        return view
    }
}