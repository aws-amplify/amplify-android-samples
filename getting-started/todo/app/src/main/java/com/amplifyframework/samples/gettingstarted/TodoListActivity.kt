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
    private var hideStatus: Boolean = true // Whether we want to show or hide completed tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Swipe to delete feature
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

    // Call query on start to load from backend
    override fun onStart() {
        super.onStart()
        itemAdapter.query(hideStatus)
    }

    // Inflates the options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.todo_menu, menu)
        return true
    }

    // Click listener for menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.show_hide -> {
                if (item.title == getString(R.string.show_tasks)) {
                    hideStatus = false
                    itemAdapter.showCompletedTasks()
                    item.title = getString(R.string.hide_tasks)
                } else {
                    hideStatus = true
                    itemAdapter.hideCompletedTasks()
                    item.title = getString(R.string.show_tasks)
                }
                true
            }
            R.id.created -> {
                itemAdapter.sortDateCreated(hideStatus)
                true
            }
            R.id.priority_asc -> {
                itemAdapter.sortPriorityAsc(hideStatus)
                true
            }
            R.id.priority_des -> {
                itemAdapter.sortPriorityDes(hideStatus)
                true
            }
            R.id.name_asc -> {
                itemAdapter.sortNameAsc(hideStatus)
                true
            }
            R.id.name_des -> {
                itemAdapter.sortNameDes(hideStatus)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun fabAction() {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, true, -1, "", Priority.LOW)
        optionsInstance.show(supportFragmentManager, "TAG")
    }

    // If hideStatus is true, when checkBox is clicked, remove item from list and mark it complete/incomplete
    // Else, simply get the item and mark it complete/incomplete
    override fun onCheckClick(position: Int, isChecked: Boolean) {
        val todo: Todo = if (hideStatus)
            itemAdapter.removeItemFromList(position)
        else
            itemAdapter.getItem(position)

        if (isChecked)
            itemAdapter.markComplete(todo)
        else
            itemAdapter.markIncomplete(todo)
    }

    // When text is clicked, open up the OptionsBarFragment to edit item
    override fun onTextClick(position: Int, text: String, priority: Priority) {
        val optionsInstance = OptionsBarFragment.newInstance(itemAdapter, false, position, text, priority)
        optionsInstance.show(supportFragmentManager, "TAG")
    }
}
