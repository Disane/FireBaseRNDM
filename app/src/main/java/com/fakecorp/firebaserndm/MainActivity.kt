package com.fakecorp.firebaserndm

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG: String? = MainActivity::class.java.name
    var selectedCategory = FUNNY
    lateinit var thoughtsAdapter: ThoughtsAdapter
    private val thoughts = arrayListOf<Thought>()

    lateinit var firestore : FirebaseFirestore
    lateinit var settings : FirebaseFirestoreSettings


    lateinit var thoughtsCollectionRef : CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        firestore = FirebaseFirestore.getInstance()
        settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        firestore.firestoreSettings = settings
        thoughtsCollectionRef = firestore.collection(THOUGHTS_REF)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            val addThoughtActivity = Intent(this, AddThoughtActivity::class.java)
            startActivity(addThoughtActivity)
        }

        thoughtsAdapter = ThoughtsAdapter(thoughts)
        thoughtListView.adapter = thoughtsAdapter
        val layouManager = LinearLayoutManager(this)
        thoughtListView.layoutManager = layouManager

        thoughtsCollectionRef.get().addOnSuccessListener { snapshot ->
                    for (document in snapshot.documents){
                        val data = document.data
                        val name = data!![USERNAME] as String
                        val timestamp = data[TIMESTAMP] as Timestamp
                        val thoughtTxt = data[THOUGHT_TXT] as String
                        val numLikes = data[NUM_LIKES] as Long
                        val numComments = data[NUM_COMMENTS] as Long
                        val documentId = document.id

                        val newThought = Thought(name, timestamp, thoughtTxt, numLikes.toInt(), numComments.toInt(), documentId)
                        thoughts.add(newThought)
                    }
            // reload the recycler view once the data has been loaded into the list of thoughts
            thoughtsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Could not add post: $exception")
            }
    }

    fun mainFunnyClicked(view: View){
        if (selectedCategory == FUNNY)
        {
            mainFunnyBtn.isChecked = true
            return
        }
        mainSeriousBtn.isChecked = false
        mainCrazyBtn.isChecked = false
        mainPopularBtn.isChecked = false
        selectedCategory = FUNNY

    }

    fun mainSeriousClicked(view: View){
        if (selectedCategory == SERIOUS)
        {
            mainSeriousBtn.isChecked = true
            return
        }
        mainFunnyBtn.isChecked = false
        mainCrazyBtn.isChecked = false
        mainPopularBtn.isChecked = false
        selectedCategory = SERIOUS
    }

    fun mainCrazyClicked(view: View){
        if (selectedCategory == CRAZY)
        {
            mainCrazyBtn.isChecked = true
            return
        }
        mainFunnyBtn.isChecked = false
        mainSeriousBtn.isChecked = false
        mainPopularBtn.isChecked = false
        selectedCategory = CRAZY
    }

    fun mainPopularClicked(view: View)
    {
        if(selectedCategory == POPULAR) {
            mainPopularBtn.isChecked = true
            return
        }
        mainFunnyBtn.isChecked = false
        mainSeriousBtn.isChecked = false
        mainCrazyBtn.isChecked = false
        selectedCategory = POPULAR
    }

}
