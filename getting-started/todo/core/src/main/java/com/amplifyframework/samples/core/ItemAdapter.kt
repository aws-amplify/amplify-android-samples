package com.amplifyframework.samples.core

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.Model
import kotlin.reflect.KClass

abstract class ItemAdapter<T : Model> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items = mutableListOf<T>()

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

    fun query() {
        Amplify.DataStore.query(
            getModelClass(),
            { results ->
                while (results.hasNext()) {
                    val item = results.next()
                    items.add(item)
                    Log.i("Tutorial", "Item loaded: ${item.id}")

                }
            },
            { Log.e("Tutorial", "Query Failed: $it") }

        )

    }

    fun addModel(model: T) {
        items.add(model)
        Amplify.DataStore.save(
            model,
            { Log.i("Tutorial", "Saved item: ${model.id}") },
            { Log.e("Tutorial", "Could not save item to DataStore", it) }
        )
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

    fun setItem(position: Int, model: T) {
        items[position] = model
        Amplify.DataStore.save(
            model,
            { Log.i("Tutorial", "Updated item: ${model.id}") },
            { Log.e("Tutorial", "Could not update item in DataStore", it) }
        )
    }

    fun getItem(position: Int): T {
        return items[position]
    }
}
