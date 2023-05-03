package com.example.termproject.DTOs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShelfList(val shelfList:List<String>):Parcelable