package com.example.todo

import com.example.core.ItemAdapter
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.datastore.generated.model.Todo
import java.io.Serializable
import kotlin.reflect.KClass

class TodoItemAdapter(private val listener: OnItemClickListener) : ItemAdapter<Todo>(), Serializable {

    override fun createModel(string: String): Todo {
        return Todo.builder()
            .name(string)
            .build()
    }

    override fun updateModel(model: Todo, string: String): Todo {
        return model.copyOfBuilder()
            .name(string)
            .build()
    }

    override fun getModelClass(): KClass<out Todo> {
        return Todo::class
    }

    override fun getLayout() = R.layout.todo_item

    override fun getViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }

    inner class ItemViewHolder(view: View)
        : RecyclerView.ViewHolder(view), Binder<Todo>, View.OnClickListener {
        private val textView: TextView = view.findViewById(R.id.todo_row_item)
        private val radioBtn: View = view.findViewById(R.id.todo_radio_button)

        override fun bind(data: Todo) {
            textView.text = data.name
        }

        init {
            radioBtn.setOnClickListener(this)
            textView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position : Int = adapterPosition
            when (v?.id) {
                R.id.todo_radio_button -> { listener.onRadioClick(position) }
                R.id.todo_row_item -> { listener.onTextClick(position) }
            }

        }
    }

    interface OnItemClickListener {
        fun onRadioClick(position: Int)
        fun onTextClick(position: Int)
    }
}