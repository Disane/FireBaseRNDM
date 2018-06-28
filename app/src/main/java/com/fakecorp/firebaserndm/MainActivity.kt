package com.fakecorp.firebaserndm

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private val TAG: String? = MainActivity::class.java.name
    // TODO: refactor this to use KOTLIN ENUM Classes
    // https://kotlinlang.org/docs/reference/enum-classes.html
    var selectedCategory = FUNNY
    lateinit var thoughtsAdapter: ThoughtsAdapter
    private val thoughts = arrayListOf<Thought>()
    lateinit var thoughtsListener: ListenerRegistration

    lateinit var firestore: FirebaseFirestore
    lateinit var settings: FirebaseFirestoreSettings

    lateinit var auth: FirebaseAuth


    lateinit var thoughtsCollectionRef: CollectionReference

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

        auth = FirebaseAuth.getInstance()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItem = menu?.getItem(0)
        if(auth.currentUser == null) {
            // logged out state
            menuItem?.title = "Login"
        }
        else {
            // logged in
            menuItem?.title = "Logout"
        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun updateUI(){
        if(auth.currentUser == null){
            Toast.makeText(this,"You are now signed out!", Toast.LENGTH_LONG).show()
            // disable all buttons
            mainCrazyBtn.isEnabled = false
            mainPopularBtn.isEnabled = false
            mainFunnyBtn.isEnabled = false
            mainSeriousBtn.isEnabled = false
            fab.isEnabled = false
            // clear the list of thoughts
            thoughts.clear()
            // notify the recyclerView that the list has been cleared
            thoughtsAdapter.notifyDataSetChanged()
        }
        else{
            Toast.makeText(this,"You are now signed in!", Toast.LENGTH_LONG).show()
            // disable all buttons
            mainCrazyBtn.isEnabled = true
            mainPopularBtn.isEnabled = true
            mainFunnyBtn.isEnabled = true
            mainSeriousBtn.isEnabled = true
            fab.isEnabled = true
            setListener()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.action_login) {
            if(auth.currentUser == null) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
            } else {
                auth.signOut()
                updateUI()
            }
            return true
        }
        return false
    }

    fun setListener()
    {
        if (selectedCategory == POPULAR){
            thoughtsListener = thoughtsCollectionRef
                    .orderBy(NUM_LIKES, Query.Direction.DESCENDING)
                    .addSnapshotListener(this) { snapshot, exception ->
                        if (exception != null) {
                            Log.e(TAG, "Could not retrieve documents: $exception")
                        }
                        if (snapshot != null) {
                           parseData(snapshot)
                        }
                    }
        }
        else {
            thoughtsListener = thoughtsCollectionRef
                    .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                    .whereEqualTo(CATEGORY, selectedCategory)
                    .addSnapshotListener(this) { snapshot, exception ->
                        if (exception != null) {
                            Log.e(TAG, "Could not retrieve documents: $exception")
                        }
                        if (snapshot != null) {
                           parseData(snapshot)
                        }
                    }
        }
    }

    fun parseData(snapshot: QuerySnapshot)
    {
        // clear before updating the list
        thoughts.clear()

        // update list of thoughts
        for (document in snapshot.documents) {
            val data = document.data
            if (data!![USERNAME] != null &&
                data[TIMESTAMP] != null &&
                data[THOUGHT_TXT] != null &&
                data[NUM_LIKES] != null &&
                data[NUM_COMMENTS] != null) {
                val name = data!![USERNAME] as String
                val timestamp = data[TIMESTAMP] as Timestamp
                val thoughtTxt = data[THOUGHT_TXT] as String
                val numLikes = data[NUM_LIKES] as Long
                val numComments = data[NUM_COMMENTS] as Long
                val documentId = document.id

                val newThought = Thought(name, timestamp, thoughtTxt, numLikes.toInt(), numComments.toInt(), documentId)
                thoughts.add(newThought)
            }
            else {
                Log.e(TAG, "Could not retrieve data because one of its elements was null!")
            }
        }
        // reload the recycler view once the data has been loaded into the list of thoughts
        thoughtsAdapter.notifyDataSetChanged()
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

        // reset observer
        thoughtsListener.remove()
        setListener()
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

        thoughtsListener.remove()
        setListener()
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

        thoughtsListener.remove()
        setListener()
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

        thoughtsListener.remove()
        setListener()
    }

}
