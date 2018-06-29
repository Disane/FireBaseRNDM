package com.fakecorp.firebaserndm.Adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fakecorp.firebaserndm.Model.Comment
import com.fakecorp.firebaserndm.R
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter (private val comments: ArrayList<Comment>): RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindComment(comments[position])
    }

    override fun getItemCount(): Int {
        return comments.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_list_view, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView){
        private val userName = itemView?.findViewById<TextView>(R.id.commentListUsername)
        private val timeStamp = itemView?.findViewById<TextView>(R.id.commentListTimestamp)
        private val commentTxt = itemView?.findViewById<TextView>(R.id.commentListCommentTxt)

        fun bindComment(comment: Comment)
        {
            userName?.text = comment.username
            commentTxt?.text = comment.commentTxt

            val dateFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            val dateData = comment.timestamp.toDate();
            val dateString = dateFormatter.format(dateData)
            timeStamp?.text = dateString

        }
    }
}