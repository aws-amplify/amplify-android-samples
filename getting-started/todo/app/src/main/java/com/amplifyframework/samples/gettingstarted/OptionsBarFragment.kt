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
import com.amplifyframework.samples.gettingstarted.databinding.OptionsBarBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBarFragment : BottomSheetDialogFragment() {
    private var _binding: OptionsBarBinding? = null
    private val binding get() = _binding!!

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
        _binding = OptionsBarBinding.inflate(inflater, container, false)
        val view = binding.root
        val b: Bundle? = arguments
        val itemAdapter: TodoItemAdapter = b?.getSerializable(ITEM_ADAPTER) as TodoItemAdapter
        val position: Int = b.getInt(POSITION)
        var isNewItem: Boolean = b.getBoolean(IS_NEW_ITEM)
        var priority: Priority = b.getSerializable(PRIORITY) as Priority
        val text: String? = b.getString(ITEM_TEXT)
        val saveBtn = binding.saveButton
        val priorityBtn = binding.priorityButton
        val trashBtn = binding.trashButton
        val priorityRadioGroup = binding.radioGroupPriority
        val textBox = binding.todoTextEntry

        when (priority) {
            Priority.LOW -> priorityRadioGroup.check(R.id.radioButton_low)
            Priority.NORMAL -> priorityRadioGroup.check(R.id.radioButton_med)
            Priority.HIGH -> priorityRadioGroup.check(R.id.radioButton_high)
        }
        textBox.setText(text)

        // Listener for text input in textbox
        textBox.addTextChangedListener(
            object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    saveBtn.isEnabled = s.toString().isNotBlank()
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
        // Saves an item when save button is clicked
        saveBtn.setOnClickListener {
            if (isNewItem) {
                val todoEntry = textBox.text.toString()
                priority = getPriority(view, priorityRadioGroup, priority)
                val item = itemAdapter.createModel(todoEntry, priority)
                itemAdapter.addModel(item, true)
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

        // Makes priority radio buttons visible/gone
        priorityBtn.setOnClickListener {
            if (priorityRadioGroup.visibility == View.GONE) {
                priorityRadioGroup.visibility = View.VISIBLE
            } else {
                priorityRadioGroup.visibility = View.GONE
            }
        }

        // Deletes the model when trash button is clicked
        trashBtn.setOnClickListener {
            if (isNewItem) {
                dismiss()
            } else {
                itemAdapter.deleteModel(position)
                textBox.text.clear()
                isNewItem = true
                priority = Priority.LOW
                priorityRadioGroup.check(R.id.radioButton_low)
                dismiss()
            }
        }
        return view
    }

    // Returns the priority set in RadioGroup, default is Priority.LOW
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
