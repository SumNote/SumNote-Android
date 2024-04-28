package com.example.sumnote.ui.DTO.Request

import com.example.sumnote.ui.DTO.Note
import com.example.sumnote.ui.DTO.NotePage

data class CreateNoteRequest(
    val note: Note,
    val notePages: List<NotePage>
)