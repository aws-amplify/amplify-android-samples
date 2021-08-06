package com.amplifyframework.samples.gettingstarted;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amplifyframework.datastore.generated.model.Priority;
import com.amplifyframework.datastore.generated.model.Todo;
import com.amplifyframework.samples.core.ItemAdapter;
import com.amplifyframework.samples.core.ListActivity;

public class TodoListActivity extends ListActivity implements TodoItemAdapter.OnItemClickListener {
    private final TodoItemAdapter itemAdapter = new TodoItemAdapter(this);
    private Boolean hideStatus = true; // Whether we want to show or hide completed tasks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swiperefresh);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Swipe to delete feature
        SwipeToDelete swipeHandler = new SwipeToDelete(this) {
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                TodoItemAdapter adapter = (TodoItemAdapter) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.deleteModel(viewHolder.getAdapterPosition());
                }
            }
        };

        // Pull to refresh feature
        swipeRefresh.setOnRefreshListener(
                () -> {
                    itemAdapter.query(hideStatus);
                    swipeRefresh.setRefreshing(false);
                }
        );

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHandler);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        ItemAdapter.setContext(this);

        // Observe changes bi-directional
        itemAdapter.observe();
    }

    // Call query on start to load from backend
    @Override
    public void onStart() {
        super.onStart();
        itemAdapter.query(hideStatus);
    }

    // Inflates the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.todo_menu, menu);
        return true;
    }

    // Click listener for menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        boolean processing = false;
        if (itemID == R.id.show_hide) {
            if (item.getTitle() == getString(R.string.show_tasks)) {
                hideStatus = false;
                itemAdapter.showCompletedTasks();
                item.setTitle(getString(R.string.hide_tasks));
            } else {
                hideStatus = true;
                itemAdapter.hideCompletedTasks();
                item.setTitle(getString(R.string.show_tasks));
            }
            processing = true;
        }
        else if (itemID == R.id.created) {
            itemAdapter.sortDateCreated(hideStatus);
            processing = true;
        }
        else if (itemID == R.id.priority_asc) {
            itemAdapter.sortPriority(hideStatus, TodoItemAdapter.SortOrder.ASCENDING);
            processing = true;
        }
        else if (itemID == R.id.priority_des) {
            itemAdapter.sortPriority(hideStatus, TodoItemAdapter.SortOrder.DESCENDING);
            processing = true;
        }
        else if (itemID == R.id.name_asc) {
            itemAdapter.sortName(hideStatus, TodoItemAdapter.SortOrder.ASCENDING);
            processing = true;
        }
        else if (itemID == R.id.name_des) {
            itemAdapter.sortName(hideStatus, TodoItemAdapter.SortOrder.DESCENDING);
            processing = true;
        }
        else {
            super.onOptionsItemSelected(item);
        }
        return processing;
    }

    @Override
    public void fabAction() {
        OptionsBarFragment optionsInstance = new OptionsBarFragment(itemAdapter, true, -1, "", Priority.LOW);
        optionsInstance.show(getSupportFragmentManager(), "TAG");
    }

    // If hideStatus is true, when checkBox is clicked, remove item from list and mark it complete/incomplete
    // Else, simply get the item and mark it complete/incomplete
    @Override
    public void onCheckClick(int position, boolean isChecked) {
        Todo todo = hideStatus ? itemAdapter.removeItemFromList(position) : itemAdapter.getItem(position);
        if (isChecked) {
            itemAdapter.markComplete(todo);
        } else {
            itemAdapter.markIncomplete(position, todo);
        }
    }

    // When text is clicked, open up the OptionsBarFragment to edit item
    @Override
    public void onTextClick(int position, String text, Priority priority) {
        OptionsBarFragment optionsInstance = new OptionsBarFragment(itemAdapter, false, position, text, priority);
        optionsInstance.show(getSupportFragmentManager(), "TAG");
    }
}
