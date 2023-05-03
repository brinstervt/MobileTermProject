package com.example.termproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.termproject.DTOs.BookItem
import com.example.termproject.backend.DataAccess
import com.example.termproject.databinding.FragmentListBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListFragment : Fragment() {

    private lateinit var binding : FragmentListBinding
    private lateinit var database: DatabaseReference
    private val userID = Firebase.auth.currentUser?.uid
    private val access = DataAccess()
    private var shelfList:MutableList<String> = mutableListOf("All Books", "Currently Reading", "To Read", "Read")

    @Suppress("DEPRECATION")
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //initialization
        binding = FragmentListBinding.inflate(layoutInflater)
        val view = binding.root
        database = Firebase.database.reference
        //initialize views
        val searchBtn = binding.search
        val shelfSelect = binding.shelfSelect
        val settingsButton=binding.settingsButton
        //initialize recyclers
        val bookList = binding.bookList
        val bookAdapter = BookListAdapter()
        bookList.adapter = bookAdapter
        bookList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        //initialize top buttons
        searchBtn.setOnClickListener{
            view.findNavController().navigate(R.id.action_listFragment_to_searchFragment)
        }
        settingsButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_listFragment_to_settingsFragment)
        }
        //add shelf
        binding.addShelfButton.setOnClickListener {
            val name = binding.addShelfText.text.toString()
            if (name.isNotBlank() && !shelfList.contains(name)){
                access.addShelf(name, userID.toString())
                binding.addShelfText.text.clear()
                shelfList.add(name)
                setupExposedDropdownMenu(shelfSelect, bookAdapter)
            }
        }

        //initially setup dropdown with basic shelves
        setupExposedDropdownMenu(shelfSelect, bookAdapter)
        //initially populates the list of books
        bookAdapter.populateAllBooks()
        // retrieve and set additional custom shelves
        GlobalScope.launch{
            database.child("userInfo/$userID/shelves").get().addOnSuccessListener {
                it.children.forEach {shelf ->
                    if(!shelfList.contains(shelf.key.toString())) {
                        shelfList.add(shelf.key.toString())
                        activity?.runOnUiThread {
                            setupExposedDropdownMenu(shelfSelect, bookAdapter)
                        }
                    }
                }
            }
        }

        return view
    }

    private fun setupExposedDropdownMenu(shelfSelect:TextInputLayout, bookAdapter:BookListAdapter) {
        val items = shelfList
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, items)

        val autoCompleteTextView = shelfSelect.editText as? AutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)

        // Set the first item as the default selected item
        autoCompleteTextView?.setText(adapter.getItem(0), false)

        autoCompleteTextView?.setOnItemClickListener { adapterView, _, position, _ ->
            val shelf = (adapterView[position] as MaterialTextView).text.toString()

            if (shelf == "All Books") { bookAdapter.populateAllBooks() }
                else { bookAdapter.populateFromShelf(shelf) }
        }
    }



    inner class BookListAdapter :
        RecyclerView.Adapter<BookListAdapter.BookViewHolder>(){

        private var books = mutableListOf<BookItem>()

        internal fun populateAllBooks(){
            books.clear()
            database.child("userInfo/$userID/books").get().addOnSuccessListener{
                var max = 20 //sets max results
                it.children.forEach { bookData ->
                    if(max > 0) {
                        val bookCall = access.getBookById(bookData.key.toString())
                        bookCall.enqueue(object : Callback<JsonObject> {
                            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                                val data = response.body()
                                val result = data?.let { access.processBookData(it) }
                                if (result != null) {
                                    books.add(result)
                                    notifyDataSetChanged()
                                }
                            }
                            override fun onFailure(call: Call<JsonObject>, t: Throwable) { Log.d("api", "failure   $t")}
                        })
                        max--
                    }
                }
            }
            notifyDataSetChanged()
        }

        internal fun populateFromShelf(shelf:String){
            books.clear()
            database.child("userInfo/$userID/shelves/$shelf").get().addOnSuccessListener {shelf->
                shelf.children.forEach{bookData ->
                    val bookCall = access.getBookById(bookData.key.toString())
                    bookCall.enqueue(object : Callback<JsonObject> {
                        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                            val data = response.body()
                            val result = data?.let { access.processBookData(it) }
                            if (result != null) {
                                books.add(result)
                                notifyDataSetChanged()
                            }
                        }
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) { Log.d("api", "failure   $t") }
                    })
                }
            }
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
            context?.let {
                Glide.with(it)
                    .load(books[position].thumbnail)
                    .apply(RequestOptions().override(80, 120))
                    .into(holder.view.findViewById(R.id.cover_image))
            }
            holder.itemView.setOnClickListener {
                holder.view.findNavController().navigate(R.id.action_listFragment_to_bookFragment,
                    bundleOf("book" to books[position]))
            }
        }
        inner class BookViewHolder(val view: View) : RecyclerView.ViewHolder(view)



    }
}