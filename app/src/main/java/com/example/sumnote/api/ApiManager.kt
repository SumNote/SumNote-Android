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
    @POST("image-to-text")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>

    // login
    @POST("android")
    fun getLoginResponse(@Body user: User): Call<String>


}