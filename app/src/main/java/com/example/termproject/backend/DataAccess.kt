package com.example.termproject.backend

import android.util.Log
import com.example.termproject.DTOs.BookImages
import com.example.termproject.DTOs.BookItem
import com.example.termproject.DTOs.TagItem
import com.example.termproject.DTOs.UserItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DataAccess {

    ////////////////////////////////////////API Section////////////////////////////////////////////////////////


    private val database = Firebase.database.reference
    fun getBooks(query:String): Call<JsonObject> {
        val baseUrl = "https://www.googleapis.com/books/v1/"

        return RetrofitService.create(baseUrl)
            .getBooks(query = query)
    }

    fun getBookById(id:String): Call<JsonObject> {
        val baseUrl = "https://www.googleapis.com/books/v1/"

        return RetrofitService.create(baseUrl)
            .getBookByID(id)
    }


    ////////////////////////////////////////Book Section////////////////////////////////////////////////////////

    fun processBookListData(data:JsonObject):List<BookItem>{
        val dataList = data.getAsJsonArray("items")
        val resultList = mutableListOf<BookItem>()
        for (i in 0 until dataList.size()) {
            val bookData = dataList.get(i)?.asJsonObject
            if(bookData != null) {
                resultList.add(processBookData(bookData))
            }
        }
        return resultList
    }

    fun processBookData(data:JsonObject):BookItem{
//        Log.d("bookdata", data.toString())
        val volumeInfo = data.getAsJsonObject("volumeInfo")
        val imageLinks = volumeInfo.getAsJsonObject("imageLinks")
        var rating = 0F
        val ratingData = volumeInfo.get("averageRating")
        if (ratingData != null){
            rating = ratingData.asFloat
        }
        var category = ""
        val categoryData = volumeInfo.get("mainCategory")
        if (categoryData != null){
            category = categoryData.asString
        }
        var ratingCount = 0
        val ratingCountData = volumeInfo.get("ratingsCount")
        if (ratingCountData != null){
            ratingCount = ratingCountData.asInt
        }
        var pageCount = 0
        val pageCountData = volumeInfo.get("pageCount")
        if (pageCountData != null){
            pageCount = pageCountData.asInt
        }
        var thumbnail = ""
        val thumbNailData = imageLinks.get("thumbnail")
        if (thumbNailData != null){
            thumbnail = thumbNailData.asString
        }
        var description = ""
        val descriptionData = volumeInfo.get("description")
        if (descriptionData != null){
            description = descriptionData.asString
        }
//        Log.d("categories", category)
        return BookItem(
            bookID = data.get("id").asString,
            description = description,
            title = volumeInfo.get("title").asString,
            author = volumeInfo.getAsJsonArray("authors").get(0).asString,
            publicationDate = volumeInfo.get("publishedDate").asString,
            pageCount = pageCount,
            category = category,
            ratingCount = ratingCount,
            rating = rating,
            thumbnail = thumbnail,
        )
    }

    private fun processBookImages(data:JsonObject):BookImages{
        Log.d("imagedata", data.toString())
        return BookImages(

            data.get("smallThunmbnail").asString,
            data.get("thumbnail").asString,
            data.get("small").asString,
            data.get("medium").asString,
            data.get("large").asString,
            data.get("extraLarge").asString,
        )
    }

    ////////////////////////////////////////Shelf Section////////////////////////////////////////////////////////


    // takes two shelves and adjusts the database for the given book and user
    fun changeShelf(bookID:String, userID:String, newShelf:String?, oldShelf:String?){
//        Log.d("change shelf", "$newShelf,  $oldShelf")
        GlobalScope.launch{
            val shelfRef = database.child("userInfo/$userID/shelves")
            if(newShelf != null) shelfRef.child("$newShelf/$bookID").setValue(1)
            if(oldShelf != null) shelfRef.child("$oldShelf/$bookID").removeValue()
            database.child("userInfo/$userID/books/$bookID/shelf").setValue(newShelf)
        }
    }

    //finds the shelf associated with a book
    suspend fun getShelf(bookID: String, userID: String):String {
        return suspendCoroutine { continuation ->
            database.child("userInfo/$userID/books").get().addOnSuccessListener {
                it.children.forEach {book ->
                    if(book.key == bookID) continuation.resume(book.child("shelf").value.toString())
                }
            }
        }
    }
    ////////////////////////////////////////Tag Section////////////////////////////////////////////////////////
    suspend fun tagExists(tagText:String, bookID: String, userID: String):Boolean{
        return suspendCoroutine { continuation ->
            database.child("userInfo/$userID/books/$bookID/tags").get().addOnSuccessListener {
                continuation.resume(it.hasChild(tagText))
            }
        }
    }

    fun addTagData(tag:TagItem, bookID:String, userID:String){
        Log.d("tag data", "entered")
        val tagRef = database.child("userInfo/$userID/tags")
        val bookRef = database.child("userInfo/$userID/books/$bookID/tags")
        bookRef.get().addOnSuccessListener { bookTag ->
            bookRef.child(tag.tag).setValue(tag.color)
            tagRef.get().addOnSuccessListener {tagTag ->
                if(tagTag.hasChild(tag.tag)){
                    Log.d("tag value", tagTag.child(tag.tag).toString())
                    val currentCount = tagTag.child(tag.tag).value.toString().toInt()
                    tagRef.child(tag.tag).setValue(currentCount + 1)
                }else{
                    tagRef.child(tag.tag).setValue(1)
                }
            }
        }

    }

    fun removeTagData(tag:TagItem, bookID:String, userID: String){
        val tagRef = database.child("userInfo/$userID/tags/${tag.tag}")
        val bookRef = database.child("userInfo/$userID/books/$bookID/tags")
        tagRef.get().addOnSuccessListener {
            val tagCount = it.value.toString().toInt() - 1
            if(tagCount == 0) tagRef.removeValue() else tagRef.setValue(tagCount)
        }
        bookRef.removeValue()
    }

    ////////////////////////////////////////Reviews Section////////////////////////////////////////////////////////

    suspend fun getUserData(userID:String):UserItem{
        return suspendCoroutine { continuation ->
            val userRef = database.child("userInfo/$userID")
            val uid = userRef.child("uid").toString()
            val email = userRef.child("email").toString()
            val name = userRef.child("name").toString()

            continuation.resume(UserItem(uid, email, name))
        }
    }



}