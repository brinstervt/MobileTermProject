package com.example.termproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.termproject.DTOs.BookItem
import com.example.termproject.DTOs.TagItem
import com.example.termproject.backend.DataAccess
import com.example.termproject.databinding.FragmentSearchBinding
import com.google.firebase.database.DatabaseReference
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchFragment : Fragment() {

    private lateinit var binding:FragmentSearchBinding
    private lateinit var database: DatabaseReference
    private val access = DataAccess()

    @SuppressLint("UseCompatLoadingForDrawables")
    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //initializing variables
        binding = FragmentSearchBinding.inflate(layoutInflater)
        val view = binding.root
        //initializing views
        val searchBar = view.findViewById<SearchView>(R.id.searchbar)
        val homeBtn = view.findViewById<ImageButton>(R.id.home)
        val searchType = view.findViewById<Spinner>(R.id.search_type)
        val subjectSpin = view.findViewById<Spinner>(R.id.subject)
        //initializing recyclers
        val resultList = view.findViewById<RecyclerView>(R.id.result_list)
        val resultAdapter = ResultsAdapter()
        resultList.adapter = resultAdapter
        resultList.layoutManager = GridLayoutManager(context, 2)

//        val filterList = view.findViewById<RecyclerView>(R.id.filter_list)
//        val filterAdapter = FilterAdapter()
//        filterList.adapter = filterAdapter

        homeBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_searchFragment_to_listFragment)
        }


        searchBar.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                val selectedSearchType = searchType.selectedItem.toString()
                val selectedSubject = subjectSpin.selectedItem.toString()
                var query = searchType(selectedSearchType) + ":$p0"
                if(selectedSubject != "Filter by Subject"){
                    query = "$query+subject:$selectedSubject"
                }
                Log.d("query", query)
                val call = access.getBooks(query)
                call.enqueue(object: Callback<JsonObject>{
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val data = response.body()
//                        Log.d("full data", data.toString())
                        val booksList = data?.let { access.processBookListData(it) }
//                        Log.d("response", booksList.toString())
                        if (booksList != null) {
                            resultAdapter.setResults(booksList)
                        }
                    }
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.d("search api", "failure   $t")
                    }
                })
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean{return true}

        })



        return view
    }

    private fun searchType(value:String):String{
        when(value) {
            "Title" -> return "+intitle"
            "Author" -> return "+inauthor"
            else -> return ""
        }
    }


    inner class ResultsAdapter :
        RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>(){

        private var results = listOf<BookItem>()

        @SuppressLint("NotifyDataSetChanged")
        internal fun setResults(list:List<BookItem>) {
            results  = list
//            Log.d("results", results.size.toString())
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return results.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_result_card, parent, false)
            return ResultViewHolder(v)
        }

        override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
            holder.view.findViewById<TextView>(R.id.title).text = results[position].title
            holder.view.findViewById<TextView>(R.id.author).text = results[position].author
            holder.view.findViewById<RatingBar>(R.id.ratingBar).rating = results[position].rating
            holder.view.findViewById<TextView>(R.id.rating_number).text = "(${results[position].ratingCount})"
            context?.let {
                Glide.with(it)
                    .load(results[position].thumbnail)
                    .apply(RequestOptions().override(80, 120))
                    .into(holder.view.findViewById(R.id.image))
            }
            holder.itemView.setOnClickListener {
                holder.view.findNavController().navigate(
                    R.id.action_searchFragment_to_bookFragment,
                    bundleOf("book" to results[position])
                )
            }
        }

        inner class ResultViewHolder(val view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            override fun onClick(view: View?) {
                //fill in later with ability to edit tags
            }
        }
    }

    inner class FilterAdapter :
        RecyclerView.Adapter<FilterAdapter.FilterViewHolder>(){

        private var filters = listOf<TagItem>()

        @SuppressLint("NotifyDataSetChanged")
        internal fun setFilters(filtersList:List<TagItem>) {
            filters  = filtersList
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return filters.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.tag_pill, parent, false)
            return FilterViewHolder(v)
        }

        override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
            holder.view.findViewById<TextView>(R.id.tag).text = filters[position].tag
            (holder.view as CardView).setCardBackgroundColor(filters[position].color)

        }

        inner class FilterViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    }

}