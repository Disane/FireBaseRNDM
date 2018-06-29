package com.fakecorp.firebaserndm.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.fakecorp.firebaserndm.*
import com.fakecorp.firebaserndm.Utilities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_thought.*

class AddThoughtActivity : AppCompatActivity() {
    val TAG: String? = AddThoughtActivity::class.java.name

    // TODO: refactor this to use KOTLIN ENUM Classes
    // https://kotlinlang.org/docs/reference/enum-classes.html

    var selectedCategory = FUNNY

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_thought)
    }

    fun addPostClicked(view:View)
    {
        // this adds a post to Firestore!
        val data = HashMap<String, Any>()
        data[CATEGORY] = selectedCategory
        data[NUM_COMMENTS] = 0
        data[NUM_LIKES] = 0
        data[THOUGHT_TXT] = addThoughtTxt.text.toString()
        data[TIMESTAMP] = FieldValue.serverTimestamp()
        data[USERNAME] = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        FirebaseFirestore.getInstance().collection(THOUGHTS_REF)
                .add(data)
                .addOnSuccessListener{_ ->
                    finish()
                }
                .addOnFailureListener{exception ->
                    Log.e(TAG, "Could not add post: $exception")
                }
    }

    fun addFunnyClicked(view: View){
        if (selectedCategory == FUNNY)
        {
            addFunnyBtn.isChecked = true
            return
        }
        addSeriousBtn.isChecked = false
        addCrazyBtn.isChecked = false
        selectedCategory = FUNNY

    }

    fun addSeriousClicked(view: View){
        if (selectedCategory == SERIOUS)
        {
            addSeriousBtn.isChecked = true
            return
        }
        addFunnyBtn.isChecked = false
        addCrazyBtn.isChecked = false
        selectedCategory = SERIOUS
    }

    fun addCrazyClicked(view:View){
        if (selectedCategory == CRAZY)
        {
            addCrazyBtn.isChecked = true
            return
        }
        addFunnyBtn.isChecked = false
        addSeriousBtn.isChecked = false
        selectedCategory = CRAZY
    }

}
