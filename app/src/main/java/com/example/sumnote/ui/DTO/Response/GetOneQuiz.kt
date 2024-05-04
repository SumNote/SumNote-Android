package com.example.sumnote.ui.DTO.Response

data class GetOneQuiz (
    val quizId : String,
    val quiz : List<QuizPage>
) {
    data class QuizPage (
        val quizPageId : String,
        val question : String,
        val selection : List<Selection>,
        val answer : String,
        val commentary : String,
    )

    data class Selection(
        val selection : String
    )
}