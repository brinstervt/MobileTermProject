package com.example.termproject.backend

import com.google.gson.JsonObject
import retrofit2.Call

class DataAccess {

    fun getBooks(query:String): Call<JsonObject> {
        val baseUrl = "https://www.googleapis.com/books/v1/"

        return RetrofitService.create(baseUrl)
            .getBooks(query = query)
    }
}