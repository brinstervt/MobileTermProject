package com.example.termproject.DTOs

data class BookItem (
    val title:String,
    val author:String,
    val publicationDate:String,
    val rating:Float,
    val imageUrl:String = "",
    val imageInt:Int = 0,
    )
