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
        binding = FragmentListBinding.inflate(layoutInflater)
        val view = binding.root

        database = Firebase.database.reference

        val searchBtn = view.findViewById<ImageButton>(R.id.search)
        val shelfSelect = view.findViewById<TextInputLayout>(R.id.shelf_select)

        searchBtn.setOnClickListener{
            view.findNavController().navigate(R.id.action_listFragment_to_searchFragment)
        }



        val call = access.getBooks("Great Gatsby")
        call.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val data = response.body()

//                Log.d("api", data.toString())
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                Log.d("api", "failure   $t")
            }
        })


        val settingsButton=view.findViewById<ImageButton>(R.id.settings_button)
        settingsButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_listFragment_to_settingsFragment)
        }

        val bookList = view.findViewById<RecyclerView>(R.id.book_list)
        val bookAdapter = BookListAdapter()
        bookList.adapter = bookAdapter
        bookList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        setupExposedDropdownMenu(shelfSelect, bookAdapter)
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



//        adapter.populateFromShelf("Read")
        bookAdapter.populateAllBooks()

        val adpaterOnClick = OnItemClickListener { adapterView, view, i, l ->
            Log.d("clicking", "entered")
            val shelf = (adapterView.get(i) as MaterialTextView).text.toString()
            Log.d("clicker", shelf)
            if (shelf == "All Books") {
                bookAdapter.populateAllBooks()
            } else {
                bookAdapter.populateFromShelf(shelf)
            }
        }

        binding.addShelfButton.setOnClickListener {
            val name = binding.addShelfText.text.toString()
            if (name.isNotBlank() && !shelfList.contains(name)){
                access.addShelf(name, userID.toString())
                binding.addShelfText.text.clear()
                shelfList.add(name)
                setupExposedDropdownMenu(shelfSelect, bookAdapter)
            }
        }

//        OnItemClickListener { adapterView, view, i, l ->
//
//        }

//        shelfSelect.setOnFocusChangeListener { view, b ->
//            Log.d("focus change", "success")
//
//            if(!b){
//                val text = shelfSelect.editText?.text
//                Log.d("spinner text", text.toString())
//            }
//        }



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
            Log.d("clicking", "entered")
            val shelf = (adapterView[position] as MaterialTextView).text.toString()
            Log.d("clicker", shelf)
            if (shelf == "All Books") {
                bookAdapter.populateAllBooks()
            } else {
                bookAdapter.populateFromShelf(shelf)
            }
        }
    }



    inner class BookListAdapter :
        RecyclerView.Adapter<BookListAdapter.BookViewHolder>(){

        //a list of the movie items to load into the RecyclerView
        private var books = mutableListOf<BookItem>()

        @SuppressLint("NotifyDataSetChanged")
        internal fun setBooks(booksList: List<BookItem>) {
            books = booksList as MutableList<BookItem>
            notifyDataSetChanged()
        }

        internal fun populateAllBooks(){
            books.clear()
            database.child("userInfo/$userID/books").get().addOnSuccessListener{
                var max = 20
                it.children.forEach { bookData ->
                    if(max > 0) {
                        val bookCall = access.getBookById(bookData.key.toString())
                        bookCall.enqueue(object : Callback<JsonObject> {
                            override fun onResponse(
                                call: Call<JsonObject>,
                                response: Response<JsonObject>
                            ) {
                                val data = response.body()
                                val result = data?.let { access.processBookData(it) }
                                if (result != null) {
                                    books.add(result)
                                    notifyDataSetChanged()
                                }
                            }
                            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                Log.d("api", "failure   $t")
                            }
                        })
                        max--
                    }
                }
            }

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
                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Log.d("api", "failure   $t")
                        }
                    })
                }
            }
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
//            holder.view.findViewById<ImageView>(R.id.cover_image)(books[position].thumbnail)
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