package com.example.termproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RatingBar
import android.widget.SearchView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

        val adapter = BookListAdapter()
        bookList.adapter = adapter
        bookList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)


        val book1:BookItem = BookItem("The Great Gatsby", "F. Scott Fitzgerald", "1925", 3.5.toFloat())
        val book2:BookItem = BookItem("The Catcher in the Rye", "J.D. Salinger", "1951", 4.0.toFloat())
        val book3:BookItem = BookItem("To Kill a Mockingbird", "Harper Lee", "1960", 3.0.toFloat())
        val book4:BookItem = BookItem("Nineteen Eighty-Four", "George Orwell", "1949", 5.0.toFloat())
        val book5:BookItem = BookItem("Jane Eyre", "Charlotte Bronte", "1847", 3.5.toFloat())
        val book6:BookItem = BookItem("Pride and Prejudice", "Jane Austen", "1813", 4.5.toFloat())


        val bookItemList:List<BookItem> = listOf<BookItem>(book1, book2, book3, book4, book5, book6)

        adapter.setBooks(bookItemList)

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
    inner class BookListAdapter :
        RecyclerView.Adapter<BookListAdapter.BookViewHolder>(){

        //a list of the movie items to load into the RecyclerView
        private var books = emptyList<BookItem>()

        internal fun setBooks(booksList: List<BookItem>) {
            books = booksList
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return books.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.book_card_view, parent, false)
            return BookViewHolder(v)
        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {

            holder.view.findViewById<TextView>(R.id.title).text = books[position].title
            holder.view.findViewById<TextView>(R.id.author).text = books[position].author
            holder.view.findViewById<TextView>(R.id.publish_date).text = books[position].publicationDate
            holder.view.findViewById<RatingBar>(R.id.rating).rating = books[position].rating
//            context?.let {
//                Glide.with(it)
//                    .load(resources.getString(R.string.picture_base_url) + movies[position].poster_path)
//                    .apply(RequestOptions().override(128, 128))
//                    .into(holder.view.findViewById(R.id.poster))
//            }

//            holder.view.findViewById<TextView>(R.id.title).text = movies[position].title
//
//            holder.view.findViewById<TextView>(R.id.rating).text =
//                movies[position].vote_average.toString()


//            holder.itemView.setOnClickListener {
//                holder.view.findNavController().navigate(R.id.action_searchResult_to_campgroundDetail,
//                    bundleOf("facilityID" to campgrounds[position].facilityID,
//                        "contractID" to campgrounds[position].contractID)
//                )
//            }
        }

        inner class BookViewHolder(val view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            override fun onClick(view: View?) {
                if (view != null) {
                    //Do some stuff here after the tap
                }
            }
        }


    }

}
