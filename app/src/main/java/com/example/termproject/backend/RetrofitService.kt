package com.example.termproject.backend

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {


        @GET("volumes")
        fun getBooks(
            @Query("q") query:String,
            @Query("projection") projection:String = "full",
            @Query("printType") printType:String = "books",
            @Query("maxResults") maxResults:Int = 10,
            @Query("key") apiKey:String = "AIzaSyDnAL3qsC3X0ZjKAP8NKRMHir7LMrNjiuo",
        ): Call<JsonObject>


    companion object {
        fun create(baseUrl: String): RetrofitService {
            val retrofit =
                Retrofit.Builder().addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                        GsonConverterFactory.create(
                            GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
                        )
                    )
//                .addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(baseUrl)
                    .build()

            return retrofit.create(RetrofitService::class.java)
        }
    }

}