package com.example.termproject.DTOs

import android.graphics.drawable.Drawable

data class BookItem (
    val title:String,
    val author:String,
    val publicationDate:String,
    val rating:Float,
    val imageUrl:String = "",
    val imageDrawable: Drawable? = null,
    )
