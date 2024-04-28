package com.example.sumnote.ui.DTO

data class CreateNoteRequest(
    val note: Note,
    val notePages: List<NotePage>
)