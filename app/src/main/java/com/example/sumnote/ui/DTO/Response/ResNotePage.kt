package com.example.sumnote.ui.DTO.Response

data class ResNotePage(
    val id : Int,
    val title: String,
    val content: String,
    val isQuizExist : Boolean,
)