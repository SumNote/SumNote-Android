package com.example.sumnote.ui.DTO

import com.example.sumnote.ui.DTO.Response.ResQuizDetail

data class CreateQuizRequest(val noteId: Long, val title: String, val quiz: List<ResQuizDetail>)