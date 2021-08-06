package com.amplifyframework.samples.gettingstarted;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.DialogFragment;

import com.amplifyframework.datastore.generated.model.Priority;
import com.amplifyframework.datastore.generated.model.Todo;
import com.amplifyframework.samples.gettingstarted.databinding.OptionsBarBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

public class OptionsBarFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String IS_NEW_ITEM = "IsNewItem";
    private static final String POSITION = "Position";
    private static final String ITEM_ADAPTER = "ItemAdapter";
    private static final String ITEM_TEXT = "ItemText";
    private static final String PRIORITY = "Priority";
    private View sheetView;
    private TodoItemAdapter itemAdapter;
    private int position;
    private boolean isNewItem;
    private Priority priority;
    private ImageButton saveBtn;
    private RadioGroup priorityRadioGroup;
    private EditText textBox;

    public OptionsBarFragment(TodoItemAdapter itemAdapter, boolean isNewItem, int position, String text, Priority priority) {
        Bundle b = new Bundle();
        b.putBoolean(IS_NEW_ITEM, isNewItem);
        b.putInt(POSITION, position);
        b.putSerializable(ITEM_ADAPTER, itemAdapter);
        b.putString(ITEM_TEXT, text);
        b.putSerializable(PRIORITY, priority);
        this.setArguments(b);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);
    }

    @Override
    public View onCreateView(
            @NotNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        OptionsBarBinding _binding = OptionsBarBinding.inflate(inflater, container, false);
        sheetView = _binding.getRoot();
        Bundle b = getArguments();
        if (b != null) {
            itemAdapter = (TodoItemAdapter) b.getSerializable(ITEM_ADAPTER);
            position = b.getInt(POSITION);
            isNewItem = b.getBoolean(IS_NEW_ITEM);
            priority = (Priority) b.getSerializable(PRIORITY);
            String text = b.getString(ITEM_TEXT);
            saveBtn = _binding.saveButton;
            ImageButton priorityBtn = _binding.priorityButton;
            ImageButton trashBtn = _binding.trashButton;
            priorityRadioGroup = _binding.radioGroupPriority;
            textBox = _binding.todoTextEntry;

            if (priority == Priority.LOW) {
                priorityRadioGroup.check(R.id.radioButton_low);
            } else if (priority == Priority.NORMAL){
                priorityRadioGroup.check(R.id.radioButton_med);
            } else if (priority == Priority.HIGH) {
                priorityRadioGroup.check(R.id.radioButton_high);
            }
            textBox.setText(text);

            // Listener for text input in textbox
            textBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    saveBtn.setEnabled(!s.toString().isEmpty());
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable s) {}
            });
            saveBtn.setEnabled(!isNewItem);
            saveBtn.setOnClickListener(this);
            priorityBtn.setOnClickListener(this);
            trashBtn.setOnClickListener(this);
        }
        return sheetView;
    }

    // Returns the priority set in RadioGroup, default is Priority.LOW
    private Priority getPriority(View view, RadioGroup priorityRadioGroup, Priority priority) {
        int selectedOption = priorityRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioBtn = view.findViewById(selectedOption);

        switch (selectedRadioBtn.getId()) {
            case (R.id.radioButton_low):
                return Priority.LOW;
            case (R.id.radioButton_med):
                return Priority.NORMAL;
            case (R.id.radioButton_high):
                return Priority.HIGH;
            default:
                return priority;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.save_button) :
                saveButtonBehavior();
                break;
            case (R.id.priority_button):
                priorityButtonBehavior();
                break;
            case (R.id.trash_button):
                trashButtonBehavior();
                break;
            default:
                break;
        }
    }

    private void saveButtonBehavior() {
        if (isNewItem) {
            String todoEntry = textBox.getText().toString();
            priority = getPriority(sheetView, priorityRadioGroup, priority);
            Todo item = itemAdapter.createModel(todoEntry, priority);
            itemAdapter.addModel(item, true);
            itemAdapter.notifyItemInserted(itemAdapter.getItemCount() - 1);
        } else {
            Todo item = itemAdapter.getItem(position);
            String todoEntry = textBox.getText().toString();
            priority = getPriority(sheetView, priorityRadioGroup, priority);
            itemAdapter.setModel(position, itemAdapter.updateModel(item, todoEntry, priority, null));
            itemAdapter.notifyItemChanged(position);
        }
        textBox.getText().clear();
    }

    private void priorityButtonBehavior() {
        if (priorityRadioGroup.getVisibility() == View.GONE) {
            priorityRadioGroup.setVisibility(View.VISIBLE);
        } else {
            priorityRadioGroup.setVisibility(View.GONE);
        }

    }

    private void trashButtonBehavior() {
        if (isNewItem) {
            dismiss();
        } else {
            itemAdapter.deleteModel(position);
            textBox.getText().clear();
            isNewItem = true;
            priority = Priority.LOW;
            priorityRadioGroup.check(R.id.radioButton_low);
        }
    }
}
