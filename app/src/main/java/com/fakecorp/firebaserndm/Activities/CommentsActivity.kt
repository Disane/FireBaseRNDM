package com.fakecorp.firebaserndm.Activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.fakecorp.firebaserndm.Adapters.CommentsAdapter
import com.fakecorp.firebaserndm.Model.Comment
import com.fakecorp.firebaserndm.R
import com.fakecorp.firebaserndm.Utilities.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_add_thought.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {

    private var TAG : String? = CommentsActivity::class.java.name
    lateinit var thoughtDocumentId: String
    lateinit var commentsAdapter: CommentsAdapter
    val comments = arrayListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        thoughtDocumentId = intent.getStringExtra(DOCUMENT_KEY)

        commentsAdapter = CommentsAdapter(comments)
        commentsListView.adapter = commentsAdapter
        val layoutManager = LinearLayoutManager(this)
        commentsListView.layoutManager = layoutManager

        FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thoughtDocumentId)
                .collection(COMMENTS_REF)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener{ snapshot, exception ->
                    if(exception != null) {
                        Log.e(TAG, "Could not retrieve comments ${exception.localizedMessage}")
                    }

                    if(snapshot != null)
                    {
                        comments.clear()

                        for(document in snapshot.documents)
                        {
                            val data = document.data
                            if(data!![USERNAME] != null &&
                                data[TIMESTAMP] != null &&
                                data[COMMENT_TXT] != null) {
                                val name = data!![USERNAME] as String
                                val timeStamp = data[TIMESTAMP] as Timestamp
                                val commentTxt = data[COMMENT_TXT] as String

                                val newComment = Comment(name, timeStamp, commentTxt)
                                comments.add(newComment)
                            }
                            else {
                                Log.e(TAG, "Could not retrieve comment data because one of its elements was null!")
                            }

                        }

                        commentsAdapter.notifyDataSetChanged()
                    }
                }
    }

    fun addCommentClicked(view: View) {
        val commentTxt = enterCommentTxt.text.toString()
        val thoughtRef = FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thoughtDocumentId)

        // fetch document
        FirebaseFirestore.getInstance().runTransaction { transaction ->

            val thought = transaction.get(thoughtRef)
            var numComments : Long? = (thought.getLong(NUM_COMMENTS) as Long) + 1 // TODO: Add plus one here!
            transaction.update(thoughtRef, NUM_COMMENTS, numComments)

            val newCommentRef = FirebaseFirestore.getInstance().collection(THOUGHTS_REF)
                    .document(thoughtDocumentId).collection(COMMENTS_REF).document()

            val data = HashMap<String, Any>()
            data[COMMENT_TXT] = commentTxt
            data[TIMESTAMP] =  FieldValue.serverTimestamp()
            data[USERNAME] = FirebaseAuth.getInstance().currentUser?.displayName.toString()

            transaction.set(newCommentRef, data)
        }
                .addOnSuccessListener {
                    enterCommentTxt.setText("")
                    hideKeyboard()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Could not add comment ${exception.localizedMessage}")
                }
    }

    private fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
