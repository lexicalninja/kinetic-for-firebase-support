package com.kinetic.fit.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kinetic.fit.ui.root.SynchronizeActivity_

private const val TAG = "LoginDispatchActivity"

class LoginDispathActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var currentUser: FirebaseUser? = mAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runDispatch()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.setResult(resultCode)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            this.runDispatch()
        } else {
            this.finish()
        }
    }

    private fun runDispatch() {
        val targetIntent: Intent
        if (currentUser != null) {
            targetIntent = getSynchronizeIntent()
            this.startActivityForResult(targetIntent, 1)
        } else {
            targetIntent = getLoginIntent()
            this.startActivityForResult(targetIntent, 0)
        }
    }

    private fun getSynchronizeIntent(): Intent {
        Crashlytics.setUserEmail(currentUser?.email)
        Crashlytics.setUserName(currentUser?.email)
        val i = Intent(this, SynchronizeActivity_::class.java)
        if (intent.extras != null) {
            i.putExtras(intent.extras)
        }
        return i
    }

    private fun getLoginIntent(): Intent {
        val i = Intent(this, LoginActivity::class.java)
        if (intent.extras != null) {
            i.putExtras(intent.extras)
        }
        return i
    }
}