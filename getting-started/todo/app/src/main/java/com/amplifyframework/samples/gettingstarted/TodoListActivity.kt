package com.amplifyframework.samples.gettingstarted

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.samples.core.ListActivity

class TodoListActivity : ListActivity(), TodoItemAdapter.OnItemClickListener {
    private val itemAdapter: TodoItemAdapter = TodoItemAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = itemAdapter
    }

    override fun onStart() {
        super.onStart()
        Log.i("Tutorial", "here1")
        itemAdapter.query()
        itemAdapter.notifyDataSetChanged()
        Log.i("Tutorial", "here2")
        Log.i("Tutorial", "here3")
    }

    override fun fabAction() {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, true, -1, "", Priority.LOW)
        optionsInstance.show(supportFragmentManager, "TAG")
    }

    override fun onRadioClick(position: Int) {
        itemAdapter.deleteModel(position)
    }

    override fun onTextClick(position: Int, text: String, priority: Priority) {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, false, position, text, priority)
        optionsInstance.show(supportFragmentManager, "TAG")
    }
}
