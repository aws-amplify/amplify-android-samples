package com.example.todo

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.listfragments.OptionsBarFragment

class TodoListActivity : com.example.core.ListActivity(), TodoItemAdapter.OnItemClickListener {
    private val itemAdapter: TodoItemAdapter = TodoItemAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = itemAdapter
    }

    override fun fabAction() {
        val b = Bundle()
        var optionsBar = OptionsBarFragment()
        b.putSerializable("ItemAdapter", itemAdapter)
        b.putBoolean("IsNewItem", true)
        optionsBar.arguments = b
        optionsBar.show(supportFragmentManager, "TAG")
    }

    override fun onRadioClick(position: Int) {
        itemAdapter.deleteModel(position)
    }

    // In progress of passing in data to optionsBarFragment
    override fun onTextClick(position: Int) {

        val b = Bundle()
        var optionsBar = OptionsBarFragment()
        b.putSerializable("ItemAdapter", itemAdapter)
        b.putBoolean("IsNewItem", false)
        b.putInt("Position", position)
        optionsBar.arguments = b
        optionsBar.show(supportFragmentManager, "TAG")
    }
}