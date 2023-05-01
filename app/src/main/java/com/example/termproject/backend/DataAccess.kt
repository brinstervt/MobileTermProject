package com.example.termproject.backend

import android.util.Log
import com.example.termproject.DTOs.BookImages
import com.example.termproject.DTOs.BookItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DataAccess {

    private val database = Firebase.database.reference
    fun getBooks(query:String): Call<JsonObject> {
        val baseUrl = "https://www.googleapis.com/books/v1/"

        return RetrofitService.create(baseUrl)
            .getBooks(query = query)
    }


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
        Log.d("categories", category)
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

    fun changeShelf(bookID:String, uid:String, newShelf:String?, oldShelf:String?){
        Log.d("change shelf", "$newShelf,  $oldShelf")
        GlobalScope.launch{
            val dbRef = database.child("userInfo/$uid/shelves")
            if(newShelf != null) dbRef.child("$newShelf/$bookID").setValue(1)
            if(oldShelf != null) dbRef.child("$oldShelf/$bookID").removeValue()
        }
    }

    suspend fun getShelf(bookID:String, uid:String):String {
        return suspendCoroutine { continuation ->
            val dbRef = database.child("userInfo/$uid/shelves")
            dbRef.get().addOnSuccessListener {
                Log.d("shelves", it.toString())
                it.children.forEach { shelf ->
                    Log.d("shelf", shelf.toString())
                    shelf.children.forEach { book ->
                        if (book.key == bookID) {
                            continuation.resume(shelf.key.toString())
                        }
                    }
                }
            }
        }
    }

}