package com.example.sumnote.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilderFastApi {
    var api: ApiManager
    init{
        val gson = GsonBuilder().setLenient().create()
        // 클래스 멤버로 OkHttpClient와 Retrofit 인스턴스를 선언하여 재사용
        val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build()
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("http://220.76.49.32:8000/")// 개발 머신의 로컬 호스트
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        api = retrofit.create(ApiManager::class.java)
    }
}