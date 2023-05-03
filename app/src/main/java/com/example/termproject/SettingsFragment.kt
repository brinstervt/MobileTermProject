package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.contains
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.termproject.DTOs.BookItem
import com.example.termproject.backend.DataAccess
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val access = DataAccess()
    private val userID = Firebase.auth.currentUser?.uid
    private var shelfList:List<String> = listOf("All Books", "Currently Reading", "To Read", "Read")



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        database = Firebase.database.reference
        auth = Firebase.auth

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {
            auth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        val shelves = view.findViewById<RecyclerView>(R.id.shelf_list)
        val shelfAdapter = ShelfListAdapter()
        shelves.adapter = shelfAdapter
        shelves.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        GlobalScope.launch{
            database.child("userInfo/$userID/shelves").get().addOnSuccessListener {
                it.children.forEach {shelf ->
                    if(!shelfList.contains(shelf.key.toString())) {
                        activity?.runOnUiThread {
                            shelfAdapter.addShelf(shelf.key.toString())
                        }
                    }
                }
            }
        }
    }


    inner class ShelfListAdapter :
        RecyclerView.Adapter<ShelfListAdapter.ShelfViewHolder>(){

        private var shelves = mutableListOf<String>()

        fun addShelf(name:String) {
            shelves.add(name)
            notifyDataSetChanged()
        }
        fun deleteShelf(name:String, index:Int){
            access.removeShelf(name, userID.toString())
            shelves.removeAt(index)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return shelves.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.shelf_card, parent, false)
            return ShelfViewHolder(v)
        }

        override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
            holder.view.findViewById<TextView>(R.id.name).text = shelves[position]
            holder.view.findViewById<ImageButton>(R.id.delete).setOnClickListener {
                deleteShelf(shelves[position], position)
            }
        }
        inner class ShelfViewHolder(val view: View) : RecyclerView.ViewHolder(view)



    }

}