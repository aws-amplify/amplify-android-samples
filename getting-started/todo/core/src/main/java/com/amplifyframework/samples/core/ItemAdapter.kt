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
    private var items = mutableListOf<T>() // List that gets displayed by viewHolder
    companion object {
        lateinit var cont: Context
        lateinit var activity: Activity
        fun setContext(con: Context) {
            cont = con
            activity = cont as Activity
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

    // Returns the size of the list
    override fun getItemCount() = items.size

    // Returns the ViewHolder
    abstract fun getViewHolder(view: View): RecyclerView.ViewHolder

    // Returns the layout
    abstract fun getLayout(): Int

    // Returns the model class
    abstract fun getModelClass(): Class<out T>

    // Queries models from Datastore into a list
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

    // Saves models into Datastore
    fun save(model: T) {
        Amplify.DataStore.save(
            model,
            { Log.i("Tutorial", "Saved item: ${model.id}") },
            { Log.e("Tutorial", "Could not save item to DataStore", it) }
        )
    }

    // Adds a model to DataStore if save is true, otherwise only adds model to list
    fun addModel(model: T, save: Boolean) {
        items.add(model)
        if (save) save(model)
    }

    // Deletes a model from Datastore and list
    open fun deleteModel(position: Int): T {
        val item = removeItemFromList(position)
        Amplify.DataStore.delete(
            item,
            { Log.i("Tutorial", "deleted item") },
            { Log.e("Tutorial", "Could not delete item") }
        )
        return item
    }

    // Sets a model at a certain position in the list
    fun setModel(position: Int, model: T) {
        items[position] = model
        save(model)
    }

    // Returns a model at a certain position
    fun getItem(position: Int): T {
        return items[position]
    }

    // Removes a model at a certain position
    fun removeItemFromList(position: Int): T {
        val item = items[position]
        items.remove(item)
        notifyItemRemoved(position)
        return item
    }

    // Appends another list to this list
    fun appendList(list: List<T>) {
        items.addAll(list)
    }

    // Clears list
    fun clearList() {
        items.clear()
    }

    // Returns list
    fun getList(): MutableList<T> {
        return items
    }

    // Sets list with another list
    fun setList(list: MutableList<T>) {
        items = list
    }
}
