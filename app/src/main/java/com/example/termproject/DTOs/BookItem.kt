package com.example.termproject.DTOs

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookItem (
    val bookID:String,
    val title:String,
    val author:String,
    val publicationDate:String,
    val description:String,
    val pageCount:Int,
    val category:String,
    val rating:Float,
    val ratingCount:Int,
    val thumbnail:String,
//    val images:BookImages,
    ):Parcelable

@Parcelize
data class BookImages(
    val smallThumbnail:String,
    val thumbnail:String,
    val small:String,
    val medium:String,
    val large:String,
    val extraLarge:String,
):Parcelable
