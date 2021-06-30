package com.example.core

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

import com.amplifyframework.datastore.DataStoreException


abstract class ListActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab : View = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            fabAction()
        }
    }

    abstract fun fabAction()
//    private suspend fun start() {
//        try {
//            Amplify.DataStore.start()
//            Log.i("ListActivity", "DataStore started")
//        } catch (error: DataStoreException) {
//            Log.e("ListActivity", "Error starting DataStore", error)
//        }
//    }
//
//    private suspend fun stop() {
//        try {
//            Amplify.DataStore.stop()
//            Log.i("ListActivity", "DataStore stopped")
//        } catch (error: DataStoreException) {
//            Log.e("ListActivity", "Error stopping DataStore", error)
//        }
//    }

//    private suspend fun save(item: T) {
//        Amplify.DataStore.query(Todo::class)
//            .catch { Log.e("ListActivity", "Query failed", it) }
//            .map { it.copyOfBuilder().id("new Title").build() }
//            .onEach { Amplify.DataStore.save(it) }
//            .catch { Log.e("ListActivity", "Update Failed", it) }
//            .collect { Log.i("ListActivity", "Updated a todo") }
//    }

}