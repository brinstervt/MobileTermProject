package com.example.termproject


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.termproject.DTOs.ReviewItem
import com.example.termproject.DTOs.TagItem
import com.example.termproject.databinding.FragmentBookBinding

class BookFragment : Fragment() {


    private lateinit var binding: FragmentBookBinding

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentBookBinding.inflate(layoutInflater)
        val view = binding.root


        val tagRecycler = view.findViewById(R.id.tag_list) as RecyclerView
        val tagAdapter = BookTagsAdapter()
        tagRecycler.adapter = tagAdapter
        tagRecycler.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)

        val reviewRecycler = view.findViewById(R.id.review_list) as RecyclerView
        val reviewAdapter = ReviewListAdapter()
        reviewRecycler.adapter = reviewAdapter
        reviewRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)



        binding.description.text = "The new novel by George Orwell is the major work towards which all his previous writing has pointed.Critics have hailed it as his most solid, most brilliant work.Though the story of Nineteen Eighty-Four takes place thirty-five years hence, it is in every sense timely.The scene is London, where there has been no new housing since 1950 and where the city-wide slums are called Victory Mansions.Science has abandoned Man for the State. As every citizen knows only too well, war is peace."

        val tag1 = TagItem("fiction", resources.getColor(R.color.purple_200))
        val tag2 = TagItem("classic", resources.getColor(R.color.red))
        val tag3 = TagItem("popular", resources.getColor(R.color.teal_700))
        val tagList = listOf(tag1, tag2, tag3)
        tagAdapter.setTags(tagList)

        val review1 = ReviewItem("John Smith", "great book I really liked it", 5.0f)
        val review2 = ReviewItem("Charlie Charleston", "I didn't like this book", 1.5f)
        val reviewList = listOf(review1, review2)
        reviewAdapter.setReviews(reviewList)


        val homeBtn = view.findViewById<ImageButton>(R.id.home)
        homeBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_bookFragment_to_listFragment)
        }

        val reviewFilter = view.findViewById<Spinner>(R.id.review_filter)

        reviewFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                reviewAdapter.filterReviews(p2)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

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

        @SuppressLint("NotifyDataSetChanged")
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


    inner class ReviewListAdapter :
        RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>(){

//        private var reviews = mutableListOf<ReviewItem>()
        private var reviews = listOf<ReviewItem>()
        private var reviewsFiltered = listOf<ReviewItem>()



//////////////////////////////// to be used when the database is up
//        internal fun setReviews(reviewData: DataSnapshot, userID:String) {
//            val children = reviewData.children
////            val length = reviewData.childrenCount
//
//            children.forEach() { review ->
//                if(review.key != userID && review.key != "reviewCount" && review.key != "reviewAverage") {
//                    val reviewName = review.child("name").value.toString()
//                    val reviewMessage = review.child("message").value.toString()
//                    val reviewRating = review.child("rating").value.toString().toFloat()
//
//                    reviews.add(ReviewItem(reviewName, reviewMessage, reviewRating))
//                }
//            }
//            reviewsFiltered = reviews
//            notifyDataSetChanged()
//        }


       //////////////////////////////// temporary for sandbox
       @SuppressLint("NotifyDataSetChanged")
       internal fun setReviews(reviewsList: List<ReviewItem>){
           reviews = reviewsList
           reviewsFiltered = reviews
           notifyDataSetChanged()
       }

        @SuppressLint("NotifyDataSetChanged")
        internal fun filterReviews(stars:Int){
            if(stars == 0) {
                reviewsFiltered = reviews
            }else{
                reviewsFiltered = reviews.filter { it.rating.toInt() == stars }
            }
            notifyDataSetChanged()
        }


        override fun getItemCount(): Int {
            if(reviewsFiltered.size <= 10)
                return reviewsFiltered.size
            return 10
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.review_card, parent, false)
            return ReviewViewHolder(v)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {

            holder.view.findViewById<TextView>(R.id.name).text = reviewsFiltered[position].userName
            holder.view.findViewById<TextView>(R.id.message).text = reviewsFiltered[position].message
            holder.view.findViewById<RatingBar>(R.id.rating).rating = reviewsFiltered[position].rating

//            holder.itemView.setOnClickListener {
//                holder.view.findNavController().navigate(
//                    R.id.action_searchResult_to_campgroundDetail,
//                    bundleOf("campgroundItem" to campgrounds[position])
//                )
//            }
        }

        inner class ReviewViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    }
}

