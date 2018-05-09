package com.fakecorp.firebaserndm
import com.google.firebase.Timestamp

data class Thought constructor(val username: String, val timestamp: Timestamp, val thoughtTxt: String,
                               val numLikes: Int, val numComments: Int, val documentId: String)
