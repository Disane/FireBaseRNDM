package com.fakecorp.firebaserndm.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fakecorp.firebaserndm.Model.Thought
import com.fakecorp.firebaserndm.Utilities.NUM_LIKES
import com.fakecorp.firebaserndm.R
import com.fakecorp.firebaserndm.Utilities.THOUGHTS_REF
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ThoughtsAdapter(private val thoughts: ArrayList<Thought>, val itemClick: (Thought) -> Unit): RecyclerView.Adapter<ThoughtsAdapter.ViewHolder>()
{
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindThought(thoughts[position])
    }

    override fun getItemCount(): Int {
       return thoughts.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.thought_list_view, parent, false)
        return ViewHolder(view, itemClick)
    }

    inner class ViewHolder(itemView: View?, private val itemClick: (Thought) -> Unit) : RecyclerView.ViewHolder(itemView){
        private val userName = itemView?.findViewById<TextView>(R.id.listViewUserName)
        private val timeStamp = itemView?.findViewById<TextView>(R.id.listViewTimeStamp)
        private val thoughtTxt = itemView?.findViewById<TextView>(R.id.listViewThoughtTxt)
        private val numLikes = itemView?.findViewById<TextView>(R.id.listViewNumLikesLbl)
        private val likesImage = itemView?.findViewById<ImageView>(R.id.listViewLikesImg)
        private val numComments = itemView?.findViewById<TextView>(R.id.listViewNumCommentsLbl)

        fun bindThought(thought: Thought)
        {
            userName?.text = thought.username
            thoughtTxt?.text = thought.thoughtTxt
            numLikes?.text = thought.numLikes.toString()
            numComments?.text = thought.numComments.toString()

            val dateFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val dateData = thought.timestamp.toDate();
            val dateString = dateFormatter.format(dateData)
            timeStamp?.text = dateString
            itemView.setOnClickListener{ itemClick(thought) }

            likesImage?.setOnClickListener{
                FirebaseFirestore.getInstance().collection(THOUGHTS_REF).document(thought.documentId)
                        .update(NUM_LIKES, thought.numLikes + 1)
            }
        }
    }
}