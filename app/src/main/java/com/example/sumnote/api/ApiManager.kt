package com.example.sumnote.api

import com.example.sumnote.ui.kakaoLogin.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiManager {
    @Multipart
    @POST("upload")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>


    //안드로이드 스튜디오 캠 카메라 화질 이슈로 ocr이 제대로 되지 않기 때문에, 테스트 용으로 작성함
    @Multipart
    @POST("image-to-text-test")
    fun uploadImageTest(@Part image: MultipartBody.Part): Call<ResponseBody>

    // login
    @POST("android")
    fun getLoginResponse(@Body user: User): Call<String>


}