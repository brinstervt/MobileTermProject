package com.example.termproject


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.termproject.databinding.FragmentBookBinding

class BookFragment : Fragment() {


    private lateinit var binding: FragmentBookBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBookBinding.inflate(layoutInflater)
        val view = binding.root


        val recyclerView = view.findViewById(R.id.tag_list) as RecyclerView
        val adapter = BookTagsAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)

        binding.description.text = "The new novel by George Orwell is the major work towards which all his previous writing has pointed.Critics have hailed it as his most solid, most brilliant work.Though the story of Nineteen Eighty-Four takes place thirty-five years hence, it is in every sense timely.The scene is London, where there has been no new housing since 1950 and where the city-wide slums are called Victory Mansions.Science has abandoned Man for the State. As every citizen knows only too well, war is peace."

        val tag1 = TagItem("fiction", resources.getColor(R.color.purple_200))
        val tag2 = TagItem("classic", resources.getColor(R.color.red))
        val tag3 = TagItem("popular", resources.getColor(R.color.teal_700))
        val tag4 = TagItem("fiction", resources.getColor(R.color.purple_200))
        val tag5 = TagItem("classic", resources.getColor(R.color.red))
        val tag6 = TagItem("popular", resources.getColor(R.color.teal_700))
        val tagList = listOf(tag1, tag2, tag3, tag4, tag5, tag6)
        adapter.setTags(tagList)

//        context?.let {
//                Glide.with(it)
//                    .load(resources.getString(R.string.picture_base_url) + movies[position].poster_path)
//                    .apply(RequestOptions().override(128, 128))
//                    .into(holder.view.findViewById(R.id.poster))
//            }


        return view
    }



    inner class BookTagsAdapter :
        RecyclerView.Adapter<BookTagsAdapter.TagViewHolder>(){

        private var tags = listOf<TagItem>()

        internal fun setTags(tagsList:List<TagItem>) {
            tags  = tagsList
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return tags.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.tag_pill, parent, false)
            return TagViewHolder(v)
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            holder.view.findViewById<TextView>(R.id.tag).text = tags[position].tag
            (holder.view as CardView).setCardBackgroundColor(tags[position].color)
        }

        inner class TagViewHolder(val view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            override fun onClick(view: View?) {
                //fill in later with ability to edit tags
            }
        }


    }
}

