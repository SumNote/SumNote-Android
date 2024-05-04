package com.example.sumnote.ui.DTO.Response


data class ResQuizDetail (
    val question : String,
    val selection : List<String>,
    val answer : String,
    val commentary : String,
)