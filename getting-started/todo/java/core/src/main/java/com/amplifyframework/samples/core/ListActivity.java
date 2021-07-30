package com.amplifyframework.samples.core;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.samples.core.databinding.ActivityMainBinding;

public abstract class ListActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.floatingActionButton.setOnClickListener(v -> fabAction());
    }

    public abstract void fabAction();
}
