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
}