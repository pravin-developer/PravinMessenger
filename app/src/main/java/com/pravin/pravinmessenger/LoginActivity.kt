package com.pravin.pravinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        button_login.setOnClickListener{
            val email=email_editTextL.text.toString()
            val password=Password_editTextL.text.toString()
            Log.d("Login","Attempt login with email:$email")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if (it.isSuccessful){

                        val intent = Intent(this,LatestMessageActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this, "failed to login", Toast.LENGTH_SHORT).show()
                    }
                }


        }
        register_textView.setOnClickListener{
            finish()
        }


    }
}