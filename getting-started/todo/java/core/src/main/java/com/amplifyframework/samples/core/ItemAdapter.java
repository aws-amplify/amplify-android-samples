package com.amplifyframework.samples.core;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.Model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemAdapter<T extends Model> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<T> items = new ArrayList<>(); // List that gets displayed by viewHolder
    public static Context cont;

    public static void setContext(Context con) {
        cont = con;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View adapterLayout = LayoutInflater.from(parent.getContext())
                .inflate(getLayout(), parent, false);
        return this.getViewHolder(adapterLayout);
    }

    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        T item = items.get(position);
        ((ItemAdapter.Binder<T>) holder).bind(item);
    }

    public interface Binder<T> {
        void bind(T data);
    }

    // Returns the size of the list
    public int getItemCount() {
        return items.size();
    }

    // Returns the ViewHolder
    public abstract RecyclerView.ViewHolder getViewHolder(View view);

    // Returns the layout
    public abstract int getLayout();

    // Returns the model class
    public abstract Class<T> getModelClass();

    // Queries models from Datastore into a list
    public void query() {
        Amplify.DataStore.query(
                getModelClass(),
                results -> {
                    while (results.hasNext()) {
                        T item = results.next();
                        items.add(item);
                        Log.i("Tutorial", "Item loaded: " + item.getId());
                    }
                    if (cont instanceof Activity) {
                        ((Activity) cont).runOnUiThread(this::notifyDataSetChanged);
                    }
                },
                failure -> Log.e("Tutorial", "Query Failed", failure)
        );
    }

    // Saves models into Datastore
    public void save(T model) {
        Amplify.DataStore.save(
                model,
                saved -> Log.i("Tutorial", "Saved item: " + model.getId()),
                failure -> Log.e("Tutorial", "Could not save item to DataStore", failure)
        );
    }

    // Adds a model to DataStore if save is true, otherwise only adds model to list
    public void addModel(T model, Boolean save) {
        items.add(model);
        if (save) {
            save(model);
        }
    }

    // Deletes a model from Datastore and list
    public Model deleteModel(int position) {
        Model item = removeItemFromList(position);
        Amplify.DataStore.delete(
                item,
                deleted -> Log.i("Tutorial", "deleted item"),
                failure -> Log.e("Tutorial", "Could not delete item", failure)
        );
        return item;
    }

    // Sets a model at a certain position in the list
    public void setModel(int position, T model) {
        items.set(position, model);
        save(model);
    }

    // Returns a model at a certain position
    public T getItem(int position) {
        return items.get(position);
    }

    // Removes a model at a certain position
    public T removeItemFromList(int position) {
        T item = items.get(position);
        items.remove(item);
        notifyDataSetChanged();
        return item;
    }

    // Appends another list to this list
    public void appendList(List<T> list) {
        items.addAll(list);
    }

    // Clears list
    public void clearList() {
        items.clear();
    }

    // Returns list
    public ArrayList<T> getList() {
        return items;
    }

    // Sets list with another list
    public void setList(ArrayList<T> list) {
        items = list;
    }
}
