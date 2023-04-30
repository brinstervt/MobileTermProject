package com.example.termproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.DTOs.BookItem
import com.example.termproject.DTOs.TagItem
import com.example.termproject.backend.DataAccess
import com.example.termproject.databinding.FragmentSearchBinding
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchFragment : Fragment() {

    private lateinit var binding:FragmentSearchBinding
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
        //initializing recyclers
        val filterList = view.findViewById<RecyclerView>(R.id.filter_list)
        val filterAdapter = FilterAdapter()
        filterList.adapter = filterAdapter

        val resultList = view.findViewById<RecyclerView>(R.id.result_list)
        val resultAdapter = ResultsAdapter()
        resultList.adapter = resultAdapter
        resultList.layoutManager = GridLayoutManager(context, 2)


        val tag1 = TagItem("fiction", resources.getColor(R.color.purple_200))
        val tag2 = TagItem("classic", resources.getColor(R.color.red))
        val tag3 = TagItem("popular", resources.getColor(R.color.teal_700))
        val tagList = listOf(tag1, tag2, tag3)
        filterAdapter.setFilters(tagList)





//        val book1 = BookItem("The Great Gatsby", "F. Scott Fitzgerald", "1925", 3.5.toFloat(), imageDrawable = resources.getDrawable(R.drawable.great_gatsby))
//        val book2 = BookItem("The Catcher in the Rye", "J.D. Salinger", "1951", 4.0.toFloat(), imageDrawable = resources.getDrawable(R.drawable.catcher_in_the_rye))
//        val book3 = BookItem("Brave New World", "Aldous Huxley", "1932", 3.0.toFloat(), imageDrawable = resources.getDrawable(R.drawable.brave_new_world))
//        val book4 = BookItem("1984", "George Orwell", "1949", 5.0.toFloat(), imageDrawable = resources.getDrawable(R.drawable._1984))
//        val book5 = BookItem("Jane Eyre", "Charlotte Bronte", "1847", 3.5.toFloat(), imageDrawable = resources.getDrawable(R.drawable.jane_erye))
//        val book6 = BookItem("Pride and Prejudice", "Jane Austen", "1813", 4.5.toFloat(), imageDrawable = resources.getDrawable(R.drawable.pride_and_prejudice))
//
//
//        val bookItemList:List<BookItem> = listOf(book1, book2, book3, book4, book5, book6)
//
//        resultAdapter.setResults(bookItemList)

        homeBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_searchFragment_to_listFragment)
        }


        searchBar.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.d("search", p0.toString())
                val call = access.getBooks(p0.toString())
                call.enqueue(object: Callback<JsonObject>{
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        val data = response.body()
                        val booksList = data?.let { access.processBookListData(it) }
                        Log.d("response", booksList.toString())
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

    inner class ResultsAdapter :
        RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>(){

        private var results = listOf<BookItem>()

        @SuppressLint("NotifyDataSetChanged")
        internal fun setResults(tagsList:List<BookItem>) {
            results  = tagsList
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
//            if(results[position].imageDrawable != null){
//                holder.view.findViewById<ImageView>(R.id.image).setImageDrawable(results[position].imageDrawable)
//            }
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