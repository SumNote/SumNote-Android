package com.example.sumnote.api

import com.example.sumnote.ui.DTO.ChangeNoteTitleRequest
import com.example.sumnote.ui.DTO.CreateNoteRequest
import com.example.sumnote.ui.DTO.CreateQuizRequest
import com.example.sumnote.ui.DTO.UpdateQuizRequest
import com.example.sumnote.ui.DTO.User
import com.example.sumnote.ui.Dialog.UpdateNoteRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiManager {
    @Multipart
    @POST("image-to-text")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>

    // 요약 내용 body에 담아서 보내기
    @POST("gen-problem")
    fun generateProblem(@Body summary : String): Call<ResponseBody>

    //안드로이드 스튜디오 캠 카메라 화질 이슈로 ocr이 제대로 되지 않기 때문에, 테스트 용으로 작성함
    @Multipart
    @POST("image-to-text-test")
    fun uploadImageTest(@Part image: MultipartBody.Part): Call<ResponseBody>

    // login
    @POST("login")
    fun getLoginResponse(@Body user: User): Call<ResponseBody>

    // 노트 리스트 가져오기
    @GET("sum-notes")
    fun getSumNotes(@Query("email") email: String): Call<ResponseBody>

    // 노트 만들기
    @POST("sum-note")
    fun createNote(@Body request: CreateNoteRequest): Call<ResponseBody>

    // 노트 조회하기
    @GET("sum-note/{id}")
    fun detailNote(@Path("id") id : Int): Call<ResponseBody>

    // 노트 내용 추가하기
    @PUT("sum-note/content/{id}")
    fun updateNote(@Path("id") id : Int, @Body request: UpdateNoteRequest): Call<ResponseBody>

    // 노트 제목 수정하기
    @PUT("sum-note/title/{id}")
    fun updateNoteTitle(@Path("id") id : Int, @Body request: ChangeNoteTitleRequest): Call<ResponseBody>

    // 노트 삭제하기
    @DELETE("sum-note/{id}")
    fun deleteNote(@Path("id") id : Int): Call<ResponseBody>


    // 퀴즈 만들기
    @POST("quiz")
    fun createQuiz(@Body request: CreateQuizRequest): Call<ResponseBody>

    //문제집 리스트 가져오기
    @GET("quizzes")
    fun getQuizList(@Query("email") email: String) : Call<ResponseBody>

    //선택한 문제집에 대한 퀴즈 가져오기
    @GET("quiz/{id}")
    fun detailQuiz(@Path("id") id : Int): Call<ResponseBody>

    // 퀴즈 내용 추가하기
    @PUT("quiz/content/{id}")
    fun updateQuiz(@Path("id") id : Int, @Body request2: UpdateQuizRequest): Call<ResponseBody>
}