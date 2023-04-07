package com.example.termproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var exposedDropdownMenu: TextInputLayout
    private lateinit var bookList: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.my_search_view)
        exposedDropdownMenu = findViewById(R.id.my_exposed_dropdown_menu)
        bookList = findViewById(R.id.book_list)

        setupExposedDropdownMenu()
    }
    private fun setupExposedDropdownMenu() {
        val items = resources.getStringArray(R.array.dropdown)
        val adapter = ArrayAdapter(this, R.layout.dropdown_menu_item, items)

        val autoCompleteTextView = exposedDropdownMenu.editText as? AutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)

        // Set the first item as the default selected item
        autoCompleteTextView?.setText(adapter.getItem(0), false)

        autoCompleteTextView?.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            // Handle the selected item here
        }
    }

}
