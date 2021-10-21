package com.amplifyframework.samples.gettingstarted

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.samples.gettingstarted.databinding.OptionsBarBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OptionsBarFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var _binding: OptionsBarBinding? = null
    private val binding get() = _binding!!
    private lateinit var sheetView: View
    private lateinit var itemAdapter: TodoItemAdapter
    private var position: Int = 0
    private var isNewItem: Boolean = false
    private lateinit var priority: Priority
    private lateinit var saveBtn: ImageButton
    private lateinit var priorityRadioGroup: RadioGroup
    private lateinit var textBox: EditText

    companion object {
        const val IS_NEW_ITEM = "IsNewItem"
        const val POSITION = "Position"
        const val ITEM_ADAPTER = "ItemAdapter"
        const val ITEM_TEXT = "ItemText"
        const val PRIORITY = "Priority"
        fun newInstance(
            itemAdapter: TodoItemAdapter,
            isNewItem: Boolean,
            position: Int,
            text: String,
            priority: Priority
        ): OptionsBarFragment {
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
        sheetView = binding.root
        val b: Bundle? = arguments
        itemAdapter = b?.getSerializable(ITEM_ADAPTER) as TodoItemAdapter
        position = b.getInt(POSITION)
        isNewItem = b.getBoolean(IS_NEW_ITEM)
        priority = b.getSerializable(PRIORITY) as Priority
        val text: String? = b.getString(ITEM_TEXT)
        saveBtn = binding.saveButton
        val priorityBtn: ImageButton = binding.priorityButton
        val trashBtn: ImageButton = binding.trashButton
        priorityRadioGroup = binding.radioGroupPriority
        textBox = binding.todoTextEntry

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
                }

                override fun afterTextChanged(s: Editable) {
                }
            }
        )
        saveBtn.isEnabled = !isNewItem
        saveBtn.setOnClickListener(this)
        priorityBtn.setOnClickListener(this)
        trashBtn.setOnClickListener(this)
        return sheetView
    }

    // Returns the priority set in RadioGroup, default is Priority.LOW
    private fun getPriority(
        view: View,
        priorityRadioGroup: RadioGroup,
        priority: Priority
    ): Priority {
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.save_button -> saveButtonBehavior()
            R.id.priority_button -> priorityButtonBehavior()
            R.id.trash_button -> trashButtonBehavior()
        }
    }

    // Saves an item when pressed
    private fun saveButtonBehavior() {
        if (isNewItem) {
            val todoEntry = textBox.text.toString()
            priority = getPriority(sheetView, priorityRadioGroup, priority)
            val item = itemAdapter.createModel(todoEntry, priority)
            itemAdapter.addModel(item, true)
            itemAdapter.notifyItemInserted(itemAdapter.itemCount - 1)
        } else {
            val item = itemAdapter.getItem(position)
            val todoEntry = textBox.text.toString()
            priority = getPriority(sheetView, priorityRadioGroup, priority)
            itemAdapter.setModel(position, itemAdapter.updateModel(item, todoEntry, priority, null))
            itemAdapter.notifyItemChanged(position)
        }
        textBox.text.clear()
    }

    // Opens and closes priorityRadioGroup when pressed
    private fun priorityButtonBehavior() {
        if (priorityRadioGroup.visibility == View.GONE) {
            priorityRadioGroup.visibility = View.VISIBLE
        } else {
            priorityRadioGroup.visibility = View.GONE
        }
    }

    // Deletes an item when pressed
    private fun trashButtonBehavior() {
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
}
