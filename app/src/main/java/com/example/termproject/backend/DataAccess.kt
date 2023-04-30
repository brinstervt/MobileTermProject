package com.example.termproject.backend

import android.util.Log
import com.example.termproject.DTOs.BookImages
import com.example.termproject.DTOs.BookItem
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call

class DataAccess {

    fun getBooks(query:String): Call<JsonObject> {
        val baseUrl = "https://www.googleapis.com/books/v1/"

        return RetrofitService.create(baseUrl)
            .getBooks(query = query)
    }


    fun processBookListData(data:JsonObject):List<BookItem>{
        val dataList = data.getAsJsonArray("items")
        val resultList = mutableListOf<BookItem>()
        for (i in 0 until data.size()) {
            val bookData = dataList.get(i)?.asJsonObject
            if(bookData != null) {
                resultList.add(processBookData(bookData))
            }
        }
        return resultList
    }

    fun processBookData(data:JsonObject):BookItem{
        Log.d("bookdata", data.toString())
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
        return BookItem(
            description = volumeInfo.get("description").asString,
            title = volumeInfo.get("title").asString,
            author = volumeInfo.getAsJsonArray("authors").get(0).asString,
            publicationDate = volumeInfo.get("publishedDate").asString,
            pageCount = volumeInfo.get("pageCount").asInt,
            category = category,
            ratingCount = ratingCount,
            rating = rating,
            thumbnail = imageLinks.get("thumbnail").asString,
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

}