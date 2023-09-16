package com.example.sumnote.ui.DTO

data class CreateQuizRequest(val email : String, val sum_id : Int, val question : String, val selections : String, val answer : String, val commentary : String)