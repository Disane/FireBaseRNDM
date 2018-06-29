package com.fakecorp.firebaserndm.Model

import com.google.firebase.Timestamp


class Comment constructor(val username: String,
                          val timestamp: Timestamp,
                          val commentTxt: String,
                          val documentId :String,
                          val userId: String)