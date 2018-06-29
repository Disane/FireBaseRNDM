package com.fakecorp.firebaserndm.Activities

import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.fakecorp.firebaserndm.Adapters.CommentsAdapter
import com.fakecorp.firebaserndm.Interfaces.CommentOptionsClickListener
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
import kotlin.jvm.internal.FunctionReference

class CommentsActivity : AppCompatActivity(), CommentOptionsClickListener {

    private var TAG : String? = CommentsActivity::class.java.name
    lateinit var thoughtDocumentId: String
    lateinit var commentsAdapter: CommentsAdapter
    val comments = arrayListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        thoughtDocumentId = intent.getStringExtra(DOCUMENT_KEY)

        commentsAdapter = CommentsAdapter(comments, this)
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
                                val documentId = document.id
                                val userId = data[USER_ID] as String

                                val newComment = Comment(name, timeStamp, commentTxt, documentId, userId)
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

    override fun optionsMenuClicked(comment: Comment) {
        // This is where w present alert dialog
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.options_menu, null)
        val deleteBtn = dialogView.findViewById<Button>(R.id.optionDeleteBtn)
        val editBtn = dialogView.findViewById<Button>(R.id.optionEditBtn)

        builder.setView(dialogView)
                .setNegativeButton("Cancel"){_, _ -> }
        val ad = builder.show()

        deleteBtn.setOnClickListener{
            // delete the comment
            val commentRef = FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thoughtDocumentId)
                    .collection(COMMENTS_REF).document(comment.documentId)
            val thoughtRef = FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thoughtDocumentId)
            /*thoughtRef.delete()
                    .addOnSuccessListener {
                        ad.dismiss()
                    }
                    .addOnFailureListener{exception ->
                        Log.e(TAG, "Could not delete comment ${exception.localizedMessage}")
                    }*/
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val thought = transaction.get(thoughtRef)
                var numComments : Long? = (thought.getLong(NUM_COMMENTS) as Long) - 1
                transaction.update(thoughtRef, NUM_COMMENTS, numComments)
                transaction.delete(commentRef)
            }
            .addOnSuccessListener {
                ad.dismiss()
            }
            .addOnFailureListener{ exception ->
                Log.e(TAG, "Could not delete comment ${exception.localizedMessage}")
            }
        }

        editBtn.setOnClickListener{
            // edit the comment
            val updateIntent = Intent(this, UpdateCommentActivity::class.java)
            updateIntent.putExtra(THOUGHT_DOC_ID_EXTRA, thoughtDocumentId)
            updateIntent.putExtra(COMMENT_DOC_ID_EXTRA, comment.documentId)
            updateIntent.putExtra(COMMENT_TXT_EXTRA, comment.commentTxt)
            ad.dismiss()
            startActivity(updateIntent)
        }
    }

    fun addCommentClicked(view: View) {
        val commentTxt = enterCommentTxt.text.toString()
        val thoughtRef = FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thoughtDocumentId)

        // fetch document
        FirebaseFirestore.getInstance().runTransaction { transaction ->

            val thought = transaction.get(thoughtRef)
            var numComments : Long? = (thought.getLong(NUM_COMMENTS) as Long) + 1
            transaction.update(thoughtRef, NUM_COMMENTS, numComments)

            val newCommentRef = FirebaseFirestore.getInstance().collection(THOUGHTS_REF)
                    .document(thoughtDocumentId).collection(COMMENTS_REF).document()

            val data = HashMap<String, Any>()
            data[COMMENT_TXT] = commentTxt
            data[TIMESTAMP] =  FieldValue.serverTimestamp()
            data[USERNAME] = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            data[USER_ID] =  FirebaseAuth.getInstance().currentUser?.uid as String

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
