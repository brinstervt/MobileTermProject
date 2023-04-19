package com.example.termproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.termproject.DTOs.BookItem
import com.example.termproject.DTOs.TagItem
import com.example.termproject.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private lateinit var binding:FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        val view = binding.root


        val filterList = view.findViewById<RecyclerView>(R.id.filter_list)
        val filterAdapter = FilterAdapter()
        filterList.adapter = filterAdapter

        val tag1 = TagItem("fiction", resources.getColor(R.color.purple_200))
        val tag2 = TagItem("classic", resources.getColor(R.color.red))
        val tag3 = TagItem("popular", resources.getColor(R.color.teal_700))
        val tagList = listOf(tag1, tag2, tag3)
        filterAdapter.setFilters(tagList)

        val resultList = view.findViewById<RecyclerView>(R.id.result_list)
        val resultAdapter = ResultsAdapter()
        resultList.adapter = resultAdapter
        resultList.layoutManager = GridLayoutManager(context, 2)



        val book1 = BookItem("The Great Gatsby", "F. Scott Fitzgerald", "1925", 3.5.toFloat())
        val book2 = BookItem("The Catcher in the Rye", "J.D. Salinger", "1951", 4.0.toFloat())
        val book3 = BookItem("Brave New World", "Aldous Huxley", "1932", 3.0.toFloat())
        val book4 = BookItem("1984", "George Orwell", "1949", 5.0.toFloat())
        val book5 = BookItem("Jane Eyre", "Charlotte Bronte", "1847", 3.5.toFloat())
        val book6 = BookItem("Pride and Prejudice", "Jane Austen", "1813", 4.5.toFloat())


        val bookItemList:List<BookItem> = listOf(book1, book2, book3, book4, book5, book6)

        resultAdapter.setResults(bookItemList)




        return view
    }

    inner class ResultsAdapter :
        RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>(){

        private var results = listOf<BookItem>()

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