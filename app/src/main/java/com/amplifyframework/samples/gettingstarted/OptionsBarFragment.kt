package com.amplifyframework.samples.gettingstarted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.amplifyframework.datastore.generated.model.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBarFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(itemAdapter: TodoItemAdapter, isNewItem: Boolean, position: Int): OptionsBarFragment {
            val b = Bundle()
            val optionsBar = OptionsBarFragment()
            b.putBoolean("IsNewItem", isNewItem)
            b.putInt("Position", position)
            b.putSerializable("ItemAdapter", itemAdapter)
            optionsBar.arguments = b
            return optionsBar
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b: Bundle? = arguments
        val itemAdapter: TodoItemAdapter = b?.getSerializable("ItemAdapter") as TodoItemAdapter
        val position: Int = b.getInt("Position", -1)
        val isNewItem: Boolean = b.getBoolean("IsNewItem")
        val view: View = inflater.inflate(R.layout.options_bar, container, false)
        val saveBtn: View = view.findViewById(R.id.save_button)
        val priorityBtn: View = view.findViewById(R.id.priority_button)
        val priorityRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup_priority)
        var priority: Priority

        if (isNewItem) {
            saveBtn.setOnClickListener {
                val todoEntry = view.findViewById<EditText>(R.id.todo_text_entry).text.toString()
                priority = getPriority(view, priorityRadioGroup)
                val item = itemAdapter.createModel(todoEntry, priority)
                itemAdapter.addModel(item)
                itemAdapter.notifyDataSetChanged()
            }
        } else {
            saveBtn.setOnClickListener {
                val item = itemAdapter.getItem(position)
                val todoEntry = view.findViewById<EditText>(R.id.todo_text_entry).text.toString()
                priority = getPriority(view, priorityRadioGroup)
                itemAdapter.setItem(position, itemAdapter.updateModel(item, todoEntry, priority))
                itemAdapter.notifyItemChanged(position)
            }
        }

        priorityBtn.setOnClickListener {
            if (priorityRadioGroup.visibility == View.GONE)
                priorityRadioGroup.visibility = View.VISIBLE
            else
                priorityRadioGroup.visibility = View.GONE
        }
        return view
    }

    private fun getPriority(view: View, priorityRadioGroup: RadioGroup): Priority {
        val selectedOption = priorityRadioGroup.checkedRadioButtonId
        val selectedRadioBtn: RadioButton = view.findViewById(selectedOption)
        return when (selectedRadioBtn.id) {
            R.id.radioButton_low -> Priority.LOW
            R.id.radioButton_med -> Priority.NORMAL
            R.id.radioButton_high -> Priority.HIGH
            else -> {
                Priority.LOW
            }
        }
    }
}
