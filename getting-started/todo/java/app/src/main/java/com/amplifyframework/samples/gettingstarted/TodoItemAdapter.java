package com.amplifyframework.samples.gettingstarted;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.model.query.QuerySortBy;
import com.amplifyframework.core.model.query.Where;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.datastore.generated.model.Priority;
import com.amplifyframework.datastore.generated.model.Todo;
import com.amplifyframework.samples.core.ItemAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TodoItemAdapter extends ItemAdapter<Todo> implements Serializable {
    private final List<Todo> completedItems;
    private final TodoItemAdapter.OnItemClickListener listener;

    // Reacts dynamically to updates of data to the underlying Storage Engine
    public void observe() {
        Amplify.DataStore.observe(Todo.class,
                started -> Log.i("MyAmplifyApp", "Observation began."),
                change -> Log.i("MyAmplifyApp", change.item().toString()),
                failure -> Log.e("MyAmplifyApp", "Observation failed.", failure),
                () -> Log.i("MyAmplifyApp", "Observation complete.")
        );
    }

    public TodoItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
        completedItems = new ArrayList<>();
    }

    // Creates and returns a model
    public Todo createModel(String name, Priority priority) {
        return Todo.builder()
                .name(name)
                .priority(priority)
                .completedAt(null)
                .build();
    }

    // Updates and returns an existing model
    public Todo updateModel(Todo model, String name, Priority priority, Temporal.DateTime completedAt) {
        return model.copyOfBuilder()
                .name(name)
                .priority(priority)
                .completedAt(completedAt)
                .build();
    }

    @Override
    public Class<Todo> getModelClass() {
        return Todo.class;
    }

    @Override
    public int getLayout() {
        return R.layout.todo_item;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    // A custom query method specifically for TodoItemAdapter class
    public void query(Boolean showStatus) {
        clearList();
        completedItems.clear();
        Amplify.DataStore.query(
                getModelClass(),
                results -> {
                    while (results.hasNext()) {
                        Todo item = results.next();
                        if (item.getCompletedAt() == null) {
                            addModel(item, false);
                        } else {
                            completedItems.add(item);
                        }
                        Log.i("Tutorial", "Item loaded: " + item.getId());
                    }
                    if (!showStatus) {
                        appendList(completedItems);
                    }
                    if (cont instanceof Activity) {
                        ((Activity)cont).runOnUiThread(this::notifyDataSetChanged);
                    }
                },
                failure -> Log.e("Tutorial", "Query Failed", failure)
        );
    }

    // Sorts list by date created
    public void sortDateCreated(Boolean showStatus) {
        query(showStatus);
    }

    public enum SortOrder {
        ASCENDING, DESCENDING
    }

    public static class PrioritySorter implements Comparator<Todo> {
        private final SortOrder sort;

        public PrioritySorter(SortOrder sort) {
            this.sort = sort;
        }

        public int compare(Todo t1, Todo t2) {
            if (sort == SortOrder.ASCENDING) {
                return t1.getPriority().compareTo(t2.getPriority());
            } else if (sort == SortOrder.DESCENDING) {
                return t2.getPriority().compareTo(t1.getPriority());
            } else {
                return 0;
            }
        }
    }

    // Sorts list by priority ascending
    public void sortPriority(Boolean showStatus, SortOrder sort) {
        ArrayList<Todo> list = getList();
        ArrayList<Todo> newList = new ArrayList<>();
        for (Todo item : list) {
            if (!completedItems.contains(item)) {
                newList.add(item);
            }
        }

        Collections.sort(newList, new PrioritySorter(sort));
        setList(newList);
        if (!showStatus) {
            Collections.sort(completedItems, new PrioritySorter(sort));
            appendList(completedItems);
        }
    }

    // Sorts by name
    public void sortName(Boolean showStatus, SortOrder sort) {
        if (sort == SortOrder.ASCENDING) {
            sort(Todo.NAME.ascending(), showStatus);
        } else if (sort == SortOrder.DESCENDING) {
            sort(Todo.NAME.descending(), showStatus);
        }
    }

    // Sort method that takes in a QuerySortBy and sorts using DataStore.query()
    private void sort(QuerySortBy sortBy, Boolean showStatus) {
        clearList();
        completedItems.clear();
        Amplify.DataStore.query(
                getModelClass(),
                Where.sorted(sortBy),
                results -> {
                    while (results.hasNext()) {
                        Todo item = results.next();
                        if (item.getCompletedAt() == null) {
                            addModel(item, false);
                        } else {
                            completedItems.add(item);
                        }
                        Log.i("Tutorial", "Item loaded: " + item.getId());
                    }
                    if (!showStatus) {
                        appendList(completedItems);
                    }
                    if (cont instanceof Activity) {
                        ((Activity)cont).runOnUiThread(this::notifyDataSetChanged);
                    }
                },
                failure -> Log.e("Tutorial", "Query Failed", failure)
        );
    }

    // Marks an item as complete by setting completedAt to current DateTime
    public void markComplete(Todo todo) {
        Date date = new Date();
        int offsetMillis = TimeZone.getDefault().getOffset(date.getTime());
        int offsetSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(offsetMillis);
        Temporal.DateTime temporalDateTime = new Temporal.DateTime(date, offsetSeconds);
        Todo updatedTodo = updateModel(todo, todo.getName(), todo.getPriority(), temporalDateTime);
        completedItems.add(updatedTodo);
        save(updatedTodo);
    }

    // Marks an item as incomplete by setting completedAt to null
    public void markIncomplete(int position, Todo todo) {
        completedItems.remove(todo);
        Todo updatedTodo = updateModel(todo, todo.getName(), todo.getPriority(), null);
        setModel(position, updatedTodo);
    }

    // Defines the colors corresponding to each Priority
    private int priorityColor(Priority priority) {
        int color = 0;
        if (priority == Priority.LOW) {
            color = ContextCompat.getColor(cont, R.color.blue);
        } else if (priority == Priority.NORMAL) {
            color = ContextCompat.getColor(cont, R.color.yellow);
        } else if (priority == Priority.HIGH) {
            color = ContextCompat.getColor(cont, R.color.red);
        }
        return color;
    }

    // Sets the color of the checkBox
    private void setCheckBoxColor(AppCompatCheckBox checkBox, int color) {
        ColorStateList colorstateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, // unchecked
                        new int[]{android.R.attr.state_checked} // checked
                },
                new int[]{
                        color, // unchecked color
                        color // checked color
                }
        );
        // set the radio button's button tint list
        //noinspection RestrictedApi
        checkBox.setButtonTintList(colorstateList);
    }

    // Shows completed tasks in recyclerView
    public void showCompletedTasks() {
        appendList(completedItems);
        notifyDataSetChanged();
    }

    // Hides completed tasks in recyclerView
    public void hideCompletedTasks() {
        ArrayList<Todo> list = getList();
        ArrayList<Todo> newList = new ArrayList<>();
        boolean contained;
        for (Todo todo : list) {
            contained = false;
            for (Todo completedTodo : completedItems) {
                if (todo.getId().equals(completedTodo.getId())) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                newList.add(todo);
            }
        }
        setList(newList);
        notifyDataSetChanged();
    }

    // Deletes model from ItemAdapter list and completedItems list
    @Override
    public Todo deleteModel(int position) {
        Todo todo = (Todo) super.deleteModel(position);
        completedItems.remove(todo);
        return todo;
    }

    // ViewHolder class
    public class ItemViewHolder extends ViewHolder implements Binder<Todo>, OnClickListener {
        private final TextView textView;
        private final CheckBox checkBox;
        private String text;
        private Priority priority;

        public ItemViewHolder(@NonNull View view) {
            super(view);
            this.textView = view.findViewById(R.id.todo_row_item);
            this.checkBox = view.findViewById(R.id.todo_checkbox);
            this.checkBox.setOnClickListener(this);
            this.textView.setOnClickListener(this);
        }

        @Override
        public void bind(Todo data) {
            textView.setText(data.getName());
            setCheckBoxColor((AppCompatCheckBox) checkBox, priorityColor(data.getPriority()));
            checkBox.setChecked(data.getCompletedAt() != null);
            text = data.getName();
            priority = data.getPriority();
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (v.getId() == R.id.todo_checkbox) {
                listener.onCheckClick(position, checkBox.isChecked());
            } else if (v.getId() == R.id.todo_row_item) {
                listener.onTextClick(position, text, priority);
            }
        }
    }

    interface OnItemClickListener {
        void onCheckClick(int position, boolean isChecked);

        void onTextClick(int position, String text, Priority priority);
    }

}
