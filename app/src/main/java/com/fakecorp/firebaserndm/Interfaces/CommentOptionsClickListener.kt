package com.fakecorp.firebaserndm.Interfaces

import com.fakecorp.firebaserndm.Model.Comment

interface CommentOptionsClickListener {
    fun optionsMenuClicked(comment: Comment)
}