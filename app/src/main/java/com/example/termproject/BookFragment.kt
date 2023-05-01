package com.example.termproject


import android.annotation.SuppressLint
import android.graphics.Color.parseColor
import android.media.Rating
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.termproject.DTOs.BookItem
import com.example.termproject.DTOs.ReviewItem
import com.example.termproject.DTOs.TagItem
import com.example.termproject.backend.DataAccess
import com.example.termproject.databinding.FragmentBookBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookFragment : Fragment() {


    private lateinit var binding: FragmentBookBinding
    private var book: BookItem? = null
    private val userID = Firebase.auth.currentUser?.uid
    private lateinit var database: DatabaseReference
    private val access = DataAccess()


    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentBookBinding.inflate(layoutInflater)
        val view = binding.root

        book = this.arguments?.getParcelable("book")
        database = Firebase.database.reference


        val tagRecycler = view.findViewById(R.id.tag_list) as RecyclerView
        val tagAdapter = BookTagsAdapter()
        tagRecycler.adapter = tagAdapter
        tagRecycler.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        tagAdapter.setTags(book?.bookID.toString())

        val reviewRecycler = view.findViewById(R.id.review_list) as RecyclerView
        val reviewAdapter = ReviewListAdapter()
        reviewRecycler.adapter = reviewAdapter
        reviewRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.title.text = book?.title
        binding.author.text = book?.author
        binding.bookRating.rating = book?.rating ?: 0f
        binding.description.text = book?.description
        binding.bookRating.rating = book?.rating!!

        context?.let {
            Glide.with(it)
                .load(book?.thumbnail)
                .apply(RequestOptions().override(128, 128))
                .into(binding.coverImage) // Make sure you have an ImageView with the ID 'poster' in your layout
        }

        var currentShelf:String? = null
        //gets the current shelf of the selected book and applies to the spinner
        GlobalScope.launch{
            val shelf = access.getShelf(book?.bookID.toString(), userID.toString())
            val adapter = binding.shelf.adapter
            for (i in 0 until adapter.count){
                if(adapter.getItem(i).toString() == shelf){
                    activity?.runOnUiThread {
                        binding.shelf.setSelection(i)
                    }
                    currentShelf = shelf
                    break
                }
            }
        }
        //if the spinner is changed adjust the books shelf in the database accordingly
        binding.shelf.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var newShelf:String? = null
                if(p2 != 0)  newShelf = binding.shelf.adapter.getItem(p2).toString()
                if (newShelf != currentShelf){
                    access.changeShelf(book?.bookID!!, userID.toString(), newShelf, currentShelf)
                    currentShelf = newShelf
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }


        binding.addTagButton.setOnClickListener{
           tagAdapter.addTag(binding.addTagText.text.toString(), book!!.bookID)
            binding.addTagText.text.clear()
        }



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

        private var tags = mutableListOf<TagItem>()

        @SuppressLint("NotifyDataSetChanged")
        internal fun setTags(bookID:String) {
            tags.clear()
            val dbRef = database.child("userInfo/$userID/books/$bookID/tags").get().addOnSuccessListener {
                it.children.forEach {tag ->
                    tags.add(TagItem(tag.key.toString(), tag.value.toString().toInt()))
                    notifyDataSetChanged()
                }
            }
        }

        internal fun addTag(tagText:String ,bookID:String){
            GlobalScope.launch {
                if(tagText.isBlank()){
                    activity?.runOnUiThread {
                        val toast = Toast.makeText(activity, "Tag Requires Text", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }
                else if(access.tagExists(tagText, bookID, userID.toString())) {
                    activity?.runOnUiThread {
                        val toast = Toast.makeText(activity, "Tag Already Exists For This Book", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }else{
                    val rand = (0..5).random()
                    val color = parseColor(resources.getStringArray(R.array.tag_colors)[rand])
                    val tag = TagItem(tagText, color)
                    tags.add(tag)
                    access.addTagData(tag, bookID, userID!!)
                    activity?.runOnUiThread {
                        notifyDataSetChanged()
                    }
                }
            }
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

