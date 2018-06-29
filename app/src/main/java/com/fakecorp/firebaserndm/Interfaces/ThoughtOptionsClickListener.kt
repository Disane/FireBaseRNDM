package com.fakecorp.firebaserndm.Interfaces

import com.fakecorp.firebaserndm.Model.Thought

interface ThoughtOptionsClickListener {
    fun thoughtOptionsMenuClicked(thought: Thought)
}