package com.example.termproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.termproject.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth


        binding.registerButton.setOnClickListener{
            val email:String = binding.email.text.toString()
            val password:String = binding.password.text.toString()
            val name:String = binding.name.text.toString()

            if(email.isEmpty()){
                Toast.makeText(this, "Email Invalid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                Toast.makeText(this ,"Password Invalid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(name.isEmpty()){
            Toast.makeText(this ,"Please Enter a Name", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        addUserToDatabase(name, email, auth.currentUser?.uid!!)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(baseContext, "Authentication failure",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.loginRedirect.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("userInfo")
        dbRef.child("$uid/uid").setValue(uid)
        dbRef.child("$uid/name").setValue(name)
        dbRef.child("$uid/email").setValue(email )
    }
}