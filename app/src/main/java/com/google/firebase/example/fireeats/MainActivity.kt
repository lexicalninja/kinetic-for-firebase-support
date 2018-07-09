package com.google.firebase.example.fireeats

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            startActivity(Intent(this, Main2Activity::class.java))
        } else {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE) {
            val resp = IdpResponse.fromResultIntent(data)
            when(resultCode){
                Activity.RESULT_OK -> startActivity(Intent(this, Main2Activity::class.java))
                else -> Log.d(this.localClassName, resp?.error.toString())
            }
        }
    }
}
