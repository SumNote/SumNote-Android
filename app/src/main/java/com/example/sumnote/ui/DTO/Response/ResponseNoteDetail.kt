package com.example.sumnote.ui.DTO.Response

import com.example.sumnote.ui.DTO.Note

data class ResponseNoteDetail (
    val note: Note,
    val notePages: List<ResNotePage>
)