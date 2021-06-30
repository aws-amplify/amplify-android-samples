package com.example.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.core.ItemAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class OptionsBarFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val b : Bundle? = arguments
        val itemAdapter : TodoItemAdapter = b?.getSerializable("ItemAdapter") as TodoItemAdapter
        val position : Int = b.getInt("Position", -1)
        val isNewItem : Boolean = b.getBoolean("IsNewItem")
        val view: View = inflater.inflate(R.layout.options_bar, container, false)
        val saveBtn : View = view.findViewById(R.id.save_button)

        if (isNewItem) {
            saveBtn.setOnClickListener {
                val todoEntry = view.findViewById<EditText>(R.id.todo_text_entry).text.toString()
                val item = itemAdapter.createModel(todoEntry)
                itemAdapter.addModel(item)
                itemAdapter.notifyDataSetChanged()
            }
        }

        else {
            saveBtn.setOnClickListener {
                var item = itemAdapter.getItem(position)
                val todoEntry = view.findViewById<EditText>(R.id.todo_text_entry).text.toString()
                itemAdapter.setItem(position, itemAdapter.updateModel(item, todoEntry))
                itemAdapter.notifyItemChanged(position)
            }
        }

        return view
    }
}