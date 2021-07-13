package com.amplifyframework.samples.gettingstarted

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.amplifyframework.datastore.generated.model.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBarFragment : BottomSheetDialogFragment() {
    companion object {
        const val IS_NEW_ITEM = "IsNewItem"
        const val POSITION = "Position"
        const val ITEM_ADAPTER = "ItemAdapter"
        const val ITEM_TEXT = "ItemText"
        const val PRIORITY = "Priority"
        fun newInstance(itemAdapter: TodoItemAdapter, isNewItem: Boolean, position: Int, text: String, priority: Priority): OptionsBarFragment {
            val b = Bundle()
            val optionsBar = OptionsBarFragment()
            b.putBoolean(IS_NEW_ITEM, isNewItem)
            b.putInt(POSITION, position)
            b.putSerializable(ITEM_ADAPTER, itemAdapter)
            b.putString(ITEM_TEXT, text)
            b.putSerializable(PRIORITY, priority)
            optionsBar.arguments = b
            return optionsBar
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b: Bundle? = arguments
        val itemAdapter: TodoItemAdapter = b?.getSerializable(ITEM_ADAPTER) as TodoItemAdapter
        val position: Int = b.getInt(POSITION)
        var isNewItem: Boolean = b.getBoolean(IS_NEW_ITEM)
        var priority: Priority = b.getSerializable(PRIORITY) as Priority
        val text: String? = b.getString(ITEM_TEXT)
        val view: View = inflater.inflate(R.layout.options_bar, container, false)
        val saveBtn: View = view.findViewById(R.id.save_button)
        val priorityBtn: View = view.findViewById(R.id.priority_button)
        val trashBtn: View = view.findViewById(R.id.trash_button)
        val priorityRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup_priority)
        val textBox: EditText = view.findViewById(R.id.todo_text_entry)

        when (priority) {
            Priority.LOW -> priorityRadioGroup.check(R.id.radioButton_low)
            Priority.NORMAL -> priorityRadioGroup.check(R.id.radioButton_med)
            Priority.HIGH -> priorityRadioGroup.check(R.id.radioButton_high)
        }
        view.findViewById<EditText>(R.id.todo_text_entry).setText(text)

        textBox.addTextChangedListener(
            object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    saveBtn.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
                }
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
// TODO Auto-generated method stub
                }
                override fun afterTextChanged(s: Editable) {
// TODO Auto-generated method stub
                }
            }
        )
        saveBtn.isEnabled = !isNewItem
        saveBtn.setOnClickListener {
            if (isNewItem) {
                val todoEntry = textBox.text.toString()
                priority = getPriority(view, priorityRadioGroup, priority)
                val item = itemAdapter.createModel(todoEntry, priority)
                itemAdapter.addModel(item)
                itemAdapter.notifyItemInserted(itemAdapter.itemCount - 1)
                textBox.text.clear()
            } else {
                val item = itemAdapter.getItem(position)
                val todoEntry = textBox.text.toString()
                priority = getPriority(view, priorityRadioGroup, priority)
                itemAdapter.setModel(position, itemAdapter.updateModel(item, todoEntry, priority, null))
                itemAdapter.notifyItemChanged(position)
                textBox.text.clear()
            }
        }

        priorityBtn.setOnClickListener {
            if (priorityRadioGroup.visibility == View.GONE)
                priorityRadioGroup.visibility = View.VISIBLE
            else
                priorityRadioGroup.visibility = View.GONE
        }

        trashBtn.setOnClickListener {
            itemAdapter.deleteModel(position)
            textBox.text.clear()
            isNewItem = true
            priority = Priority.LOW
            priorityRadioGroup.check(R.id.radioButton_low)
        }
        return view
    }

    private fun getPriority(view: View, priorityRadioGroup: RadioGroup, priority: Priority): Priority {
        val selectedOption = priorityRadioGroup.checkedRadioButtonId
        val selectedRadioBtn: RadioButton = view.findViewById(selectedOption)
        return when (selectedRadioBtn.id) {
            R.id.radioButton_low -> Priority.LOW
            R.id.radioButton_med -> Priority.NORMAL
            R.id.radioButton_high -> Priority.HIGH
            else -> {
                priority
            }
        }
    }
}
