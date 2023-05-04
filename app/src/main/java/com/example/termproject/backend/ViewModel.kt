package com.example.termproject.backend

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewModel (application: Application) : AndroidViewModel(application) {

    val database = Firebase.database.reference
    val userID = Firebase.auth.currentUser?.uid

    val shelfList = listOf<String>()

    fun getShelves(){

    }

}