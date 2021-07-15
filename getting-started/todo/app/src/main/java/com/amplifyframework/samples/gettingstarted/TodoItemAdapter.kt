package com.amplifyframework.samples.gettingstarted

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.QuerySortBy
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.samples.core.ItemAdapter
import java.io.Serializable
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class TodoItemAdapter(private val listener: OnItemClickListener) : ItemAdapter<Todo>(), Serializable {
    private var completedItems = mutableListOf<Todo>() // A list to hold completed items

    // Creates and returns a model
    fun createModel(name: String, priority: Priority): Todo {
        return Todo.builder()
            .name(name)
            .priority(priority)
            .completedAt(null)
            .build()
    }

    // Updates and returns an existing model
    fun updateModel(model: Todo, name: String, priority: Priority, completedAt: Temporal.DateTime?): Todo {
        return model.copyOfBuilder()
            .name(name)
            .priority(priority)
            .completedAt(completedAt)
            .build()
    }

    override fun getModelClass(): Class<out Todo> {
        return Todo::class.java
    }

    override fun getLayout() = R.layout.todo_item

    override fun getViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }

    // A custom query method specifically for TodoItemAdapter class
    fun query(showStatus: Boolean) {
        clearList()
        completedItems.clear()
        Amplify.DataStore.query(
            getModelClass(),
            { results ->
                while (results.hasNext()) {
                    val item = results.next()
                    if (item.completedAt == null) {
                        addModel(item, false)
                    } else {
                        completedItems.add(item)
                    }
                    Log.i("Tutorial", "Item loaded: ${item.id}")
                }
                if (!showStatus)
                    appendList(completedItems)
                activity.runOnUiThread {
                    notifyDataSetChanged()
                }
            },
            { Log.e("Tutorial", "Query Failed: $it") }
        )
    }

    // Sorts list by date created
    fun sortDateCreated(showStatus: Boolean) {
        query(showStatus)
    }

    // Sorts list by priority ascending
    fun sortPriorityAsc(showStatus: Boolean) {
        val newList = getList().filter { !completedItems.contains(it) }
        setList(newList.sortedBy { it.priority }.asReversed().toMutableList())
        if (!showStatus)
            appendList(completedItems.sortedBy { it.priority }.asReversed().toMutableList())
        notifyDataSetChanged()
    }

    // Sorts list by priority descending
    fun sortPriorityDes(showStatus: Boolean) {
        val newList = getList().filter { !completedItems.contains(it) }
        setList(newList.sortedBy { it.priority }.toMutableList())
        if (!showStatus)
            appendList(completedItems.sortedBy { it.priority }.toMutableList())
        notifyDataSetChanged()
    }

    // Sorts by name ascending
    fun sortNameAsc(showStatus: Boolean) {
        sort(Todo.NAME.ascending(), showStatus)
    }

    // Sorts by name descending
    fun sortNameDes(showStatus: Boolean) {
        sort(Todo.NAME.descending(), showStatus)
    }

    // Sort method that takes in a QuerySortBy and sorts using DataStore.query()
    private fun sort(q: QuerySortBy, showStatus: Boolean) {
        clearList()
        completedItems.clear()
        Amplify.DataStore.query(
            getModelClass(),
            Where.sorted(q),
            { results ->
                while (results.hasNext()) {
                    val item = results.next()
                    if (item.completedAt == null) {
                        addModel(item, false)
                    } else {
                        completedItems.add(item)
                    }
                    Log.i("Tutorial", "Item loaded: ${item.id}")
                }
                if (!showStatus)
                    appendList(completedItems)
                activity.runOnUiThread {
                    notifyDataSetChanged()
                }
            },
            { Log.e("Tutorial", "Query Failed: $it") }
        )
    }

    // Marks an item as complete by setting completedAt to current DateTime
    fun markComplete(todo: Todo) {
        val date = Date()
        val offsetMillis = TimeZone.getDefault().getOffset(date.time).toLong()
        val offsetSeconds = TimeUnit.MILLISECONDS.toSeconds(offsetMillis).toInt()
        val temporalDateTime = Temporal.DateTime(date, offsetSeconds)
        val updatedTodo = updateModel(todo, todo.name, todo.priority, temporalDateTime)
        completedItems.add(updatedTodo)
        save(updatedTodo)
    }

    // Marks an item as incomplete by setting completedAt to null
    fun markIncomplete(todo: Todo) {
        completedItems.remove(todo)
        val updatedTodo = updateModel(todo, todo.name, todo.priority, null)
        save(updatedTodo)
    }

    // Defines the colors corresponding to each Priority
    private fun priorityColor(priority: Priority): Int {
        return when (priority) {
            Priority.LOW -> Color.argb(200, 51, 181, 129) // Turquoise
            Priority.NORMAL -> Color.argb(200, 155, 179, 0) // Yellow
            Priority.HIGH -> Color.argb(200, 201, 21, 23) // Red
        }
    }

    // Sets the color of the checkBox
    private fun CheckBox.setCheckBoxColor(color: Int) {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_checked) // checked
            ),
            intArrayOf(
                color, // unchecked color
                color // checked color
            )
        )
        // set the radio button's button tint list
        buttonTintList = colorStateList
    }

    // Shows completed tasks in recyclerView
    fun showCompletedTasks() {
        appendList(completedItems)
        notifyDataSetChanged()
    }

    // Hides completed tasks in recyclerView
    fun hideCompletedTasks() {
        setList(getList().filter { !completedItems.contains(it) }.toMutableList())
        notifyDataSetChanged()
    }

    // Deletes model from ItemAdapter list and completedItems list
    override fun deleteModel(position: Int): Todo {
        val todo = super.deleteModel(position)
        if (completedItems.contains(todo))
            completedItems.remove(todo)
        return todo
    }

    // ViewHolder class
    inner class ItemViewHolder(view: View) :
        RecyclerView.ViewHolder(view), Binder<Todo>, View.OnClickListener {
        private val textView: TextView = view.findViewById(R.id.todo_row_item)
        private val checkBox: CheckBox = view.findViewById(R.id.todo_checkbox)
        private lateinit var text: String
        private lateinit var priority: Priority

        override fun bind(data: Todo) {
            textView.text = data.name
            checkBox.setCheckBoxColor(priorityColor(data.priority))
            checkBox.isChecked = data.completedAt != null
            text = data.name
            priority = data.priority
        }

        init {
            checkBox.setOnClickListener(this)
            textView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            when (v?.id) {
                R.id.todo_checkbox -> { listener.onCheckClick(position, checkBox.isChecked) }
                R.id.todo_row_item -> { listener.onTextClick(position, text, priority) }
            }
        }
    }

    interface OnItemClickListener {
        fun onCheckClick(position: Int, isChecked: Boolean)
        fun onTextClick(position: Int, text: String, priority: Priority)
    }
}
