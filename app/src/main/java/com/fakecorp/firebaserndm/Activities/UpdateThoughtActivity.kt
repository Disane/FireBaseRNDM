package com.fakecorp.firebaserndm.Activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.fakecorp.firebaserndm.R
import com.fakecorp.firebaserndm.Utilities.THOUGHTS_REF
import com.fakecorp.firebaserndm.Utilities.THOUGHT_DOC_ID_EXTRA
import com.fakecorp.firebaserndm.Utilities.THOUGHT_TXT
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_update_thought.*

class UpdateThoughtActivity : AppCompatActivity() {

    private val TAG : String? = UpdateCommentActivity::class.java.name
    lateinit var thoughtDocId : String
    lateinit var thoughtTxt: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_thought)

        thoughtDocId = intent.getStringExtra(THOUGHT_DOC_ID_EXTRA)
        thoughtTxt = intent.getStringExtra(THOUGHT_TXT)

        updateThoughtTxt.setText(thoughtTxt)
    }

    fun updateThoughtClicked(view: View){
        FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thoughtDocId)
                .update(THOUGHT_TXT, updateThoughtTxt.text.toString())
                .addOnSuccessListener {
                    hideKeyboard()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Could not update thought text ${exception.localizedMessage}")
                }
    }

    private fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
