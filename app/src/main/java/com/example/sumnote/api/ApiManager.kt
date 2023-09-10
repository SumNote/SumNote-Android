package com.example.sumnote.api

import com.example.sumnote.ui.DTO.CreateNoteRequest
import com.example.sumnote.ui.DTO.Summary
import com.example.sumnote.ui.DTO.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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
    fun getSumNotes(@Query("name") name: String, @Query("email") email: String): Call<ResponseBody>

    // 노트 만들기
    @POST("create-sum-note")
    fun createNote(@Body request: CreateNoteRequest): Call<ResponseBody>


}