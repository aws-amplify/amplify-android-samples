package com.amplifyframework.samples.gettingstarted

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.samples.core.ListActivity

class TodoListActivity : ListActivity(), TodoItemAdapter.OnItemClickListener {
    private val itemAdapter: TodoItemAdapter = TodoItemAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = itemAdapter
    }

    override fun fabAction() {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, true, -1)
        optionsInstance.show(supportFragmentManager, "TAG")
    }

    override fun onRadioClick(position: Int) {
        itemAdapter.deleteModel(position)
    }

    override fun onTextClick(position: Int) {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, false, position)
        optionsInstance.show(supportFragmentManager, "TAG")
    }
}