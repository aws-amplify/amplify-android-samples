package com.amplifyframework.samples.gettingstarted

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CheckBox
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.samples.core.ItemAdapter
import com.amplifyframework.samples.core.ListActivity

class TodoListActivity : ListActivity(), TodoItemAdapter.OnItemClickListener {
    private val itemAdapter: TodoItemAdapter = TodoItemAdapter(this)
    private var showStatus: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val swipeHandler = object : SwipeToDelete(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as TodoItemAdapter
                adapter.deleteModel(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        ItemAdapter.setContext(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.todo_menu, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        itemAdapter.query(showStatus)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.show_hide -> {
                if (item.title == getString(R.string.show_tasks)) {
                    showStatus = false
                    itemAdapter.showCompletedTasks()
                    item.title = getString(R.string.hide_tasks)
                } else {
                    showStatus = true
                    itemAdapter.hideCompletedTasks()
                    item.title = getString(R.string.show_tasks)
                }
                true
            }
            R.id.created -> {
                itemAdapter.sortDateCreated(showStatus)
                true
            }
            R.id.priority_asc -> {
                itemAdapter.sortPriorityAsc(showStatus)
                true
            }
            R.id.priority_des -> {
                itemAdapter.sortPriorityDes(showStatus)
                true
            }
            R.id.name_asc -> {
                itemAdapter.sortNameAsc(showStatus)
                true
            }
            R.id.name_des -> {
                itemAdapter.sortNameDes(showStatus)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun fabAction() {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, true, -1, "", Priority.LOW)
        optionsInstance.show(supportFragmentManager, "TAG")
    }

    override fun onCheckClick(position: Int, checkBox: CheckBox) {
        val isChecked = checkBox.isChecked
        val todo: Todo = if (showStatus)
            itemAdapter.removeItemFromList(position)
        else
            itemAdapter.getItem(position)

        if (isChecked)
            itemAdapter.markComplete(todo)
        else
            itemAdapter.markIncomplete(todo)
    }

    override fun onTextClick(position: Int, text: String, priority: Priority) {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, false, position, text, priority)
        optionsInstance.show(supportFragmentManager, "TAG")
    }
}
