package com.amplifyframework.samples.gettingstarted

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Priority
import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.samples.core.ItemAdapter
import java.io.Serializable
import kotlin.reflect.KClass

class TodoItemAdapter(private val listener: OnItemClickListener) : ItemAdapter<Todo>(), Serializable {

    fun createModel(name: String, priority: Priority): Todo {
        Log.i("Tutorial", "$priority")
        return Todo.builder()
            .name(name)
            .priority(priority)
            .build()
    }

    fun updateModel(model: Todo, name: String, priority: Priority): Todo {
        Log.i("Tutorial", "$priority")
        return model.copyOfBuilder()
            .name(name)
            .priority(priority)
            .build()
    }

    override fun getModelClass(): Class<out Todo> {
        return Todo::class.java
    }

    override fun getLayout() = R.layout.todo_item

    override fun getViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(view: View) :
        RecyclerView.ViewHolder(view), Binder<Todo>, View.OnClickListener {
        private val textView: TextView = view.findViewById(R.id.todo_row_item)
        private val radioBtn: RadioButton = view.findViewById(R.id.todo_radio_button)
        private lateinit var text: String
        private lateinit var priority: Priority

        override fun bind(data: Todo) {
            textView.text = data.name
            radioBtn.setCircleColor(priorityColor(data.priority))
            text = data.name
            priority = data.priority
        }

        init {
            radioBtn.setOnClickListener(this)
            textView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            when (v?.id) {
                R.id.todo_radio_button -> { listener.onRadioClick(position) }
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

    private fun RadioButton.setCircleColor(color: Int) {
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
        fun onRadioClick(position: Int)
        fun onTextClick(position: Int, text: String, priority: Priority)
    }
}
