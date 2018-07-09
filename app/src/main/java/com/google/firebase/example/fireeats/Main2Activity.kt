package com.google.firebase.example.fireeats

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Main2Activity : AppCompatActivity() {

    private val mFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    override fun onResume() {
        super.onResume()
//        val query = mFirestore.collection("restaurants")
//                .orderBy("avgRating", Query.Direction.DESCENDING).limit(40).get()
//                .addOnCompleteListener({
//                    if (it.isSuccessful) {
//                        val querySnap = it.result
//                        if (!querySnap.isEmpty) {
//                            Log.d(this.localClassName, "DocumentSnapshot data: " + querySnap.documents)
//                            for (d in querySnap.documents) {
//                                Log.d(this.localClassName, d.data.toString())
//                                Log.d(this.localClassName, d.get("ratings").toString())
//                            }
//                        } else {
//                            Log.d(this.localClassName, "No such document")
//                        }
//                    } else {
//                        Log.d(this.localClassName, "get failed with ", it.exception)
//                    }
//                })
//        Log.d(this.localClassName, "query: $query")

        val query = mFirestore.collection("restaurants")
                .document("yMCTAHqQkZ2khSzDu78l").collection("ratings")
                .get().addOnCompleteListener({
                    if (it.isSuccessful) {
                        val snap = it.result
                        if (!snap.isEmpty) {
                            for (d in snap.documents) {
                                Log.d(this.localClassName, "rating: ${d.data.toString()}")
                            }
                        }
                    } else {
                        Log.d(this.localClassName, "No such document")
                    }
                })
    }

}
