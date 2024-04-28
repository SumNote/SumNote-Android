package com.example.sumnote.api

import android.util.Log
import com.example.sumnote.MainActivity
import com.example.sumnote.ui.DTO.ChangeNoteTitleRequest
import com.example.sumnote.ui.DTO.CreateNoteRequest
import com.example.sumnote.ui.DTO.CreateQuizRequest
import com.example.sumnote.ui.DTO.UpdateQuizRequest
import com.example.sumnote.ui.DTO.User
import com.example.sumnote.ui.Dialog.UpdateNoteRequest
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

interface ApiManager {

    /**
     * FastAPI Server API
     * */

    @Multipart
    @POST("image-to-text")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>

    // pdf 파일 바탕으로 노트 생성
    @Multipart
    @POST("pdf-to-text")
    fun uploadPdf(@Part pdf: MultipartBody.Part): Call<ResponseBody>

    // 요약 내용 body에 담아서 보내기
    @POST("gen-problem")
    fun generateProblem(@Body summary : String): Call<ResponseBody>

    // Test
    @Multipart
    @POST("image-to-text-test")
    fun uploadImageTest(@Part image: MultipartBody.Part): Call<ResponseBody>


    /**
     * Spring Server API
     * */

    // 카카오 로그인
    @POST("api/member/login")
    fun getLoginResponse(@Body user: User): Call<ResponseBody>

    // 노트 리스트 가져오기
    @GET("api/sum-note")
    fun getSumNotes(@Header("Authorization") token: String, @Query("type") type: String): Call<ResponseBody>
//    fun getSumNotes(@Query("type") type: String): Call<ResponseBody>

    // 노트 만들기
    @POST("api/sum-note")
    fun createNote(@Header("Authorization") token: String, @Body request: CreateNoteRequest): Call<ResponseBody>

    // 노트 조회하기
    @GET("api/sum-note/{id}")
    fun detailNote(@Path("id") id : Int): Call<ResponseBody>

    // 노트 내용 추가하기
    @PUT("api/sum-note/content/{id}")
    fun updateNote(@Path("id") id : Int, @Body request: UpdateNoteRequest): Call<ResponseBody>

    // 노트 제목 수정하기
    @PUT("api/sum-note/title/{id}")
    fun updateNoteTitle(@Path("id") id : Int, @Body request: ChangeNoteTitleRequest): Call<ResponseBody>

    // 노트 삭제하기
    @DELETE("api/sum-note/{id}")
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


object SpringRetrofit {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object FastAPIRetrofit {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}