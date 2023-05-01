package com.example.termproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.DTOs.BookItem
import com.example.termproject.backend.DataAccess
import com.example.termproject.databinding.FragmentListBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListFragment : Fragment() {

    private lateinit var binding : FragmentListBinding
    private lateinit var database: DatabaseReference
    private val userID = Firebase.auth.currentUser?.uid
    private val access = DataAccess()

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

        val bookList = view.findViewById<RecyclerView>(R.id.book_list)
        val adapter = BookListAdapter()
        bookList.adapter = adapter
        bookList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

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



        setupExposedDropdownMenu(shelfSelect)

        return view
    }

    private fun setupExposedDropdownMenu(shelfSelect:TextInputLayout) {
        val items = resources.getStringArray(R.array.dropdown)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, items)

        val autoCompleteTextView = shelfSelect.editText as? AutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)

        // Set the first item as the default selected item
        autoCompleteTextView?.setText(adapter.getItem(0), false)

        autoCompleteTextView?.setOnItemClickListener { _, _, position, _ ->
//            val selectedItem = adapter.getItem(position)
            // Handle the selected item here
        }
    }

    inner class BookListAdapter :
        RecyclerView.Adapter<BookListAdapter.BookViewHolder>(){

        //a list of the movie items to load into the RecyclerView
        private var books = emptyList<BookItem>()

        @SuppressLint("NotifyDataSetChanged")
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
//            holder.view.findViewById<ImageView>(R.id.cover_image).setImageDrawable(books[position].imageDrawable)
//            context?.let {
//                Glide.with(it)
//                    .load(resources.getString(R.string.picture_base_url) + movies[position].poster_path)
//                    .apply(RequestOptions().override(128, 128))
//                    .into(holder.view.findViewById(R.id.poster))
//            }



            holder.itemView.setOnClickListener {
                holder.view.findNavController().navigate(R.id.action_listFragment_to_bookFragment)
            }
        }

        inner class BookViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    }
}