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
import androidx.core.text.HtmlCompat
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookFragment : Fragment() {


    private lateinit var binding: FragmentBookBinding
    private var book: BookItem? = null
    private val userID = Firebase.auth.currentUser?.uid
    private lateinit var database: DatabaseReference
    private val access = DataAccess()
    private var shelfList:MutableList<String> = mutableListOf("No Shelf", "Currently Reading", "To Read", "Read")
    private var currentShelf:String? = null


    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //initialization
        binding = FragmentBookBinding.inflate(layoutInflater)
        val view = binding.root
        database = Firebase.database.reference
        book = this.arguments?.getParcelable("book")
        //initialize views
        binding.title.text = book?.title
        binding.author.text = book?.author
        binding.bookRating.rating = book?.rating ?: 0f

        val styledText = HtmlCompat.fromHtml(
            book?.description!!,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            null,
            null
        )
        binding.description.text = styledText

        binding.bookRating.rating = book?.rating!!
        context?.let {
            Glide.with(it)
                .load(book?.thumbnail)
                .apply(RequestOptions().override(128, 128))
                .into(binding.coverImage) // Make sure you have an ImageView with the ID 'poster' in your layout
        }
        //initialize recyclers
        //tags
        val tagRecycler = binding.tagList
        val tagAdapter = BookTagsAdapter()
        tagRecycler.adapter = tagAdapter
        tagRecycler.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        tagAdapter.setTags(book?.bookID.toString())
        //reviews
        val reviewRecycler = binding.reviewList
        val reviewAdapter = ReviewListAdapter()
        reviewRecycler.adapter = reviewAdapter
        reviewRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)




        val homeBtn = binding.home
        homeBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_bookFragment_to_listFragment)
        }

            GlobalScope.launch{
                database.child("userInfo/$userID/shelves").get().addOnSuccessListener {
                    it.children.forEach {shelf ->
                        if(!shelfList.contains(shelf.key.toString())) {
                            shelfList.add(shelf.key.toString())
                            activity?.runOnUiThread {
                                setDropdown()
                            }
                        }
                    }
                }

            }

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


        binding.addTagButton.setOnClickListener{
           tagAdapter.addTag(binding.addTagText.text.toString(), book!!.bookID)
            binding.addTagText.text.clear()
        }


        reviewAdapter.setReviews(book?.bookID.toString(), userID.toString())
        val userRating = binding.userRating
        val userReviewMessage = binding.userReviewMessage
        val submitReview = binding.submitReview
        var editing = true
        //checks if the user submitted a review in the past and if so populates the relevant UI elements
        database.child("userInfo/$userID/books/${book?.bookID}").get().addOnSuccessListener {
            if(it.hasChild("review")){
                userRating.rating = it.child("review/rating").value.toString().toFloat()
                userRating.setIsIndicator(true)
                userReviewMessage.setText(it.child("review/message").value.toString())
                userReviewMessage.isEnabled = false
                submitReview.text = "Edit"
                editing = false
            }
        }
        // submit button, if editing submits a review to the database, if not enables editing
        submitReview.setOnClickListener{
            if(editing){
                val rating = userRating.rating
                val message = userReviewMessage.text.toString()
                access.submitReview(rating, message, userID.toString(), book?.bookID.toString())
                editing = false
                submitReview.text = "Edit"
                userRating.setIsIndicator(true)
                userReviewMessage.isEnabled = false
            }else{
                editing = true
                submitReview.text = "Submit"
                userRating.setIsIndicator(false)
                userReviewMessage.isEnabled = true
            }
        }

        val reviewFilter = binding.reviewFilter
        //checks review filter spinner and filters review list on changes
        reviewFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                reviewAdapter.filterReviews(p2)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        return view
    }

    fun setDropdown(){
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, shelfList)
        binding.shelf.adapter = adapter

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
                    val rand = (0..11).random()
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

        internal fun removeTag(tag: TagItem){
            tags.remove(tag)
            access.removeTagData(tag, book?.bookID.toString(), userID.toString())
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
            holder.view.setOnClickListener{
                removeTag(tags[position])
            }
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
        private var reviews = mutableListOf<ReviewItem>()
        private var reviewsFiltered = listOf<ReviewItem>()

        //pulls reviews down from the databse
        internal fun setReviews(bookID:String, userID:String) {
            reviews.clear()
            val reviewRef = database.child("books/$bookID/reviews")
            reviewRef.get().addOnSuccessListener {
                it.children.forEach() { review ->
                    if(review.key != userID) {
                        val reviewName = review.child("name").value.toString()
                        val reviewMessage = review.child("message").value.toString()
                        val reviewRating = review.child("rating").value.toString().toFloat()

                        reviews.add(ReviewItem(reviewName, reviewMessage, reviewRating))
                        reviewsFiltered = reviews
                        activity?.runOnUiThread {
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        //filters reviews based on the spinner
        @SuppressLint("NotifyDataSetChanged")
        internal fun filterReviews(stars:Int){
            reviewsFiltered = if(stars == 0) {
                reviews
            }else{
                reviews.filter { it.rating.toInt() == stars }
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

        }

        inner class ReviewViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    }
}

