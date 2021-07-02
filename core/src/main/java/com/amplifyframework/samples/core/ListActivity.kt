package com.amplifyframework.samples.core

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


abstract class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab: View = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            fabAction()
        }
    }

    abstract fun fabAction()
}
