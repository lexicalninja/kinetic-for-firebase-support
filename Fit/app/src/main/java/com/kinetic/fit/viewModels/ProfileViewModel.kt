package com.kinetic.fit.viewModels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.DocumentSnapshot
//import com.google.firebase.firestore.FirebaseFirestore


class ProfileViewModel : ViewModel() {
    private var user: MutableLiveData<Map<String, Any>> = MutableLiveData()
    private var name: MutableLiveData<String> = MutableLiveData()
//    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    init {
//        db.collection("/profiles").document(mAuth.currentUser?.uid!!).addSnapshotListener{snapshot, e ->
//            if(e != null) {
//                Log.d("ProfileViewModel", e.toString())
//                return@addSnapshotListener
//            } else if (snapshot != null && snapshot.exists()) {
//                Log.d("ProfileViewModel", "Current data: " + snapshot.data)
//                user.postValue(snapshot.data)
//            } else {
//                Log.d("ProfileViewModel", "Current data: null");
//            }
//        }
    }

    fun getName(): MutableLiveData<String> {
        return name
    }

    fun getUser():MutableLiveData<Map<String, Any>> {
        return user
    }
}