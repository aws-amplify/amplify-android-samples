package com.amplifyframework.samples.core

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.Model

abstract class ItemAdapter<T : Model>() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = mutableListOf<T>()
    companion object {
        private lateinit var context: Context
        lateinit var activity: Activity
        fun setContext(con: Context) {
            context = con
            activity = context as Activity
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(getLayout(), parent, false)
        return getViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as Binder<T>).bind(item)
    }

    interface Binder<T> {
        fun bind(data: T)
    }

    override fun getItemCount() = items.size
    abstract fun getViewHolder(view: View): RecyclerView.ViewHolder
    abstract fun getLayout(): Int
    abstract fun getModelClass(): Class<out T>

    open fun query() {
        Amplify.DataStore.query(
            getModelClass(),
            { results ->
                while (results.hasNext()) {
                    val item = results.next()
                    items.add(item)
                    Log.i("Tutorial", "Item loaded: ${item.id}")
                }
                activity.runOnUiThread {
                    notifyDataSetChanged()
                }
            },
            { Log.e("Tutorial", "Query Failed: $it") }
        )
    }

    fun save(model: T) {
        Amplify.DataStore.save(
            model,
            { Log.i("Tutorial", "Saved item: ${model.id}") },
            { Log.e("Tutorial", "Could not save item to DataStore", it) }
        )
    }

    fun addModel(model: T) {
        items.add(model)
        save(model)
    }

    fun deleteModel(position: Int) {
        val item = items[position]
        Amplify.DataStore.delete(
            item,
            { Log.i("Tutorial", "deleted item") },
            { Log.e("Tutorial", "Could not delete item") }
        )
        items.remove(item)
        notifyItemRemoved(position)
    }

    fun setModel(position: Int, model: T) {
        items[position] = model
        save(model)
    }

    fun getItem(position: Int): T {
        return items[position]
    }

    fun removeItemFromList(position: Int): T {
        val item = items[position]
        items.remove(item)
        notifyItemRemoved(position)
        return item
    }

    fun addItem(model: T) {
        items.add(model)
    }

    fun appendList(list: List<T>) {
        items.addAll(list)
    }

    fun clearList() {
        items.clear()
    }

    fun printList() {
        Log.i("Tutorial", items.toString())
    }
}
