package com.fakecorp.firebaserndm

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var TAG : String? = LoginActivity::class.java.name

    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
    }

    fun loginLoginClicked(view: View) {
        // this is where we perform login
        val email = loginEmailTxt.text.toString()
        val password = loginPasswordTxt.text.toString()

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Could not sign in user ${exception.localizedMessage}")
                }
    }

    fun loginCreateClicked(view: View){
        // segue to the CreateUserActivity
        val createIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createIntent)
    }
}
