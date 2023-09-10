package com.example.sumnote.ui.kakaoLogin

import com.example.sumnote.api.ApiManager
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    var api: ApiManager
    init{
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://223.130.132.249:8080/") // 요청 보내는 API 서버 url. /로 끝나야 함함
            .addConverterFactory(GsonConverterFactory.create(gson)) // Gson을 역직렬화
            .build()
        api = retrofit.create(ApiManager::class.java)
    }
}