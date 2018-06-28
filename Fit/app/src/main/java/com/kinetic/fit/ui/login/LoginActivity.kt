package com.kinetic.fit.ui.login

import android.app.Activity
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.kinetic.fit.R
import com.kinetic.fit.data.DataSync
import com.kinetic.fit.data.DataSync_
import com.kinetic.fit.ui.FitActivity
import com.kinetic.fit.util.ViewStyling
import kotlinx.android.synthetic.main.activity_login.*


const val MIN_PASSWORD_LENGTH: Int = 8

class LoginActivity : FitActivity() {
    private var mProgressDialog: ProgressDialog? = null
    private var passwordEmailSent = false
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var mDataSyncBinder: DataSync.DataSyncBinder? = null
    private val mDataSyncConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mDataSyncBinder = service as DataSync.DataSyncBinder
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mDataSyncBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        createAccountTextLink.setOnClickListener { createAccountViewSetup() }
        forgotPasswordTextLink.setOnClickListener { retrievePasswordViewSetup() }
        loginTextLink.setOnClickListener { loginViewSetup() }
        emailEditText.setOnFocusChangeListener { v, hasFocus -> editTextHasFocus(v, hasFocus) }
        emailEditText.setOnClickListener { getFocus(it) }
        passwordEditText.setOnFocusChangeListener { v, hasFocus -> editTextHasFocus(v, hasFocus) }
        passwordEditText.setOnClickListener { getFocus(it) }
        createAccountNameEditText.setOnFocusChangeListener { v, hasFocus -> editTextHasFocus(v, hasFocus) }
        createAccountNameEditText.setOnClickListener { getFocus(it) }
        loginButton.setOnClickListener { login() }
        signUpButton.setOnClickListener { signup() }
        retrievePasswordButton.setOnClickListener { resetPassword() }
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        if (mDataSyncBinder != null) {
            unbindService(mDataSyncConnection)
        }
        super.onDestroy()
    }

    private fun loginViewSetup() {
        emailEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        passwordEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        passwordEditText.visibility = View.VISIBLE
        createAccountNameEditText.visibility = View.GONE
        loginTextLink.visibility = View.GONE
        forgotPasswordTextLink.visibility = View.VISIBLE
        createAccountTextLink.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        retrievePasswordButton.visibility = View.GONE
        signUpButton.visibility = View.GONE
        emailEditText.clearFocus()
        emailEditText.requestFocus()
    }

    private fun login() {
        val username = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        when {
            username.isEmpty() -> showNoEmailToast()
            password.isEmpty() -> showNoPasswordToast()
            else -> {
                loading()
                mDataSyncBinder?.authenticate(username, password) { code ->
                    if (!isDestroyed) {
                        when (code) {
                            200 -> successfulLogin()
                            101 -> invalidLogIn()
                            else -> showUnknownErrorToast(code)
                        }
                    }
                }
            }
        }

    }

    private fun retrievePasswordViewSetup() {
        emailEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        passwordEditText.visibility = View.GONE
        createAccountNameEditText.visibility = View.GONE
        forgotPasswordTextLink.visibility = View.GONE
        createAccountTextLink.visibility = View.VISIBLE
        loginTextLink.visibility = View.VISIBLE
        loginButton.visibility = View.GONE
        retrievePasswordButton.visibility = View.VISIBLE
        signUpButton.visibility = View.GONE
        emailEditText.invalidate()
        emailEditText.requestFocus()
    }

    private fun resetPassword() {
        if (!passwordEmailSent) {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                showNoEmailToast()
            } else {
                loading()
                mDataSyncBinder?.resetPassword(email) { code ->
                    if (!isDestroyed) {
                        doneLoading()
                        when (code) {
                            200 -> {
                                passwordEmailSent = true
                                resetPasswordEmailSent()
                            }
                            125, 205 -> showInvalidEmailToast()
                            else -> showUnknownErrorToast(code)
                        }
                    }
                }
            }
        }
    }

    private fun createAccountViewSetup() {
        emailEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        passwordEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        createAccountNameEditText.imeOptions = EditorInfo.IME_ACTION_GO
        passwordEditText.visibility = View.VISIBLE
        createAccountNameEditText.visibility = View.VISIBLE
        createAccountTextLink.visibility = View.GONE
        forgotPasswordTextLink.visibility = View.VISIBLE
        loginTextLink.visibility = View.VISIBLE
        loginButton.visibility = View.GONE
        retrievePasswordButton.visibility = View.GONE
        signUpButton.visibility = View.VISIBLE
        emailEditText.invalidate()
        emailEditText.requestFocus()
    }

    private fun signup() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        var name = createAccountNameEditText.text.toString()
        when {
            email.isEmpty() -> showNoEmailToast()
            password.isEmpty() -> showNoPasswordToast()
            password.length < MIN_PASSWORD_LENGTH -> passwordTooShort()
            else -> {
                if (name.isEmpty()) {
                    name = getString(R.string.kinetic_user)
                }
                loading()
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
                    if (it.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        successfulLogin()
                    } else {
                        // If sign in fails, display a message to the user.
                        when (it.exception) {
                            is FirebaseAuthWeakPasswordException -> weakPassword()
                            is FirebaseAuthInvalidCredentialsException -> malformedPassword()
                            is FirebaseAuthUserCollisionException -> alreadySignedUp()
                            else -> showUnknownErrorToast(-1)
                        }
                    }
                }
            }
        }
    }


    private fun editTextHasFocus(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun getFocus(view: View) {
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun sendRequest() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(if (null == currentFocus)
                    null
                else
                    currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        login()
    }

    private fun showNoEmailToast() {
        Toast.makeText(this, R.string.login_email_error_toast_message, Toast.LENGTH_LONG).show()
    }

    private fun showNoPasswordToast() {
        ViewStyling.getCustomToast(this, layoutInflater,
                getString(R.string.login_password_error_toast_message)).show()
    }

    private fun showUnknownErrorToast(code: Int) {
        Toast.makeText(this, getString(R.string.signup_failed_unkown_reason, code), Toast.LENGTH_LONG).show()
    }

    private fun showInvalidEmailToast() {
        ViewStyling.getCustomToast(this, layoutInflater,
                getString(R.string.signup_invalid_email)).show()
    }

    private fun passwordTooShort() {
        Toast.makeText(this, getString(R.string.login_password_too_short, MIN_PASSWORD_LENGTH), Toast.LENGTH_LONG).show()
    }

    fun loading() {
        mProgressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading_wait_login))
    }

    private fun doneLoading() {
        mProgressDialog?.dismiss()
    }

    private fun successfulLogin() {
        val i = Intent()
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    private fun alreadySignedUp() {
        ViewStyling.getCustomToast(this, layoutInflater, getString(R.string.signup_email_already_registered)).show()
    }

    private fun invalidLogIn() {
        ViewStyling.getCustomToast(this, layoutInflater,
                getString(R.string.login_invalid_login)).show()
    }

    private fun resetPasswordEmailSent() {
        ViewStyling.getCustomToast(this, layoutInflater, getString(R.string.forgot_password_email_sent)).show()
    }

    private fun weakPassword() {
        ViewStyling.getCustomToast(this, layoutInflater, getString(R.string.weak_password)).show()
    }

    private fun malformedPassword() {
        ViewStyling.getCustomToast(this, layoutInflater, getString(R.string.malformed_email)).show()
    }
}

