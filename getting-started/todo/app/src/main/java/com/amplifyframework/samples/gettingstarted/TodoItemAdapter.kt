package com.amplifyframework.samples.gettingstarted

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.samples.core.ItemAdapter
import java.io.Serializable
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class TodoItemAdapter(private val listener: OnItemClickListener) : ItemAdapter<Todo>(), Serializable {
    private var completedItems = mutableListOf<Todo>()

    fun createModel(name: String, priority: Priority): Todo {
        return Todo.builder()
            .name(name)
            .priority(priority)
            .completed(null)
            .build()
    }

    fun updateModel(model: Todo, name: String, priority: Priority, completedAt: Temporal.DateTime?): Todo {
        return model.copyOfBuilder()
            .name(name)
            .priority(priority)
            .completed(completedAt)
            .build()
    }

    override fun getModelClass(): Class<out Todo> {
        return Todo::class.java
    }

    override fun getLayout() = R.layout.todo_item

    override fun getViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }

    override fun query() {
        Amplify.DataStore.query(
            getModelClass(),
            { results ->
                while (results.hasNext()) {
                    val item = results.next()
                    if (item.completed == null) {
                        addItem(item)
                    } else {
                        completedItems.add(item)
                    }
                    Log.i("Tutorial", "Item loaded: ${item.id}")
                }
                activity.runOnUiThread {
                    notifyDataSetChanged()
                }
            },
            { Log.e("Tutorial", "Query Failed: $it") }
        )
    }

    fun markComplete(todo: Todo) {
        val date = Date()
        val offsetMillis = TimeZone.getDefault().getOffset(date.time).toLong()
        val offsetSeconds = TimeUnit.MILLISECONDS.toSeconds(offsetMillis).toInt()
        val temporalDateTime = Temporal.DateTime(date, offsetSeconds)
        val updatedTodo = updateModel(todo, todo.name, todo.priority, temporalDateTime)
        completedItems.add(updatedTodo)
        save(updatedTodo)
    }

    fun markIncomplete(todo: Todo) {
        completedItems.remove(todo)
        val updatedTodo = updateModel(todo, todo.name, todo.priority, null)
        save(updatedTodo)
    }

    inner class ItemViewHolder(view: View) :
        RecyclerView.ViewHolder(view), Binder<Todo>, View.OnClickListener {
        private val textView: TextView = view.findViewById(R.id.todo_row_item)
        private val checkBox: CheckBox = view.findViewById(R.id.todo_checkbox)
        private lateinit var text: String
        private lateinit var priority: Priority

        override fun bind(data: Todo) {
            textView.text = data.name
            checkBox.setCheckBoxColor(priorityColor(data.priority))
            checkBox.isChecked = data.completed != null
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
                R.id.todo_checkbox -> { listener.onCheckClick(position, checkBox) }
                R.id.todo_row_item -> { listener.onTextClick(position, text, priority) }
            }
        }
    }

    private fun priorityColor(priority: Priority): Int {
        return when (priority) {
            Priority.LOW -> Color.argb(200, 51, 181, 129)
            Priority.NORMAL -> Color.argb(200, 155, 179, 0)
            Priority.HIGH -> Color.argb(200, 201, 21, 23)
        }
    }

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

    interface OnItemClickListener {
        fun onCheckClick(position: Int, checkBox: CheckBox)
        fun onTextClick(position: Int, text: String, priority: Priority)
    }

    fun showCompletedTasks() {
        appendList(completedItems)
        notifyDataSetChanged()
    }

    fun hideCompletedTasks() {
        clearList()
        completedItems.clear()
        query()
    }
}
