package com.example.termproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.my_search_view)
    }
}