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
            .baseUrl("http://10.0.2.2:8080/")// 개발 머신의 로컬 호스트
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        api = retrofit.create(ApiManager::class.java)
    }
}