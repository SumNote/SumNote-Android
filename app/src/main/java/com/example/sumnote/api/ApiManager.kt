package com.example.sumnote.api

import com.example.sumnote.ui.DTO.ChangeNoteTitleRequest
import com.example.sumnote.ui.DTO.CreateQuizRequest
import com.example.sumnote.ui.DTO.Request.CreateNoteRequest
import com.example.sumnote.ui.DTO.NotePage
import com.example.sumnote.ui.DTO.UpdateQuizRequest
import com.example.sumnote.ui.DTO.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
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
    fun detailNote(@Header("Authorization") token: String, @Path("id") id : Int): Call<ResponseBody>

    // 노트 내용 추가하기
    @PUT("api/sum-note/{id}/add")
    fun updateNote(@Header("Authorization") token: String, @Path("id") id : Int, @Body request: NotePage): Call<ResponseBody>

    // 노트 제목 수정하기
    @PUT("api/sum-note/{id}/title")
    fun updateNoteTitle(@Header("Authorization") token: String, @Path("id") id : Int, @Body request: ChangeNoteTitleRequest): Call<ResponseBody>

    // 노트 삭제하기
    @DELETE("api/sum-note/{id}")
    fun deleteNote(@Header("Authorization") token: String, @Path("id") id : Int): Call<ResponseBody>


    // 퀴즈 만들기
    @POST("api/quiz")
    fun createQuiz(@Header("Authorization") token: String, @Body request: CreateQuizRequest): Call<ResponseBody>

    //문제집 리스트 가져오기
    @GET("api/quiz")
    fun getQuizList(@Header("Authorization") token: String, @Query("type") type: String) : Call<ResponseBody>

    //선택한 문제집에 대한 퀴즈 가져오기
    @GET("api/quiz/{id}")
    fun detailQuiz(@Header("Authorization") token: String, @Path("id") id : Int): Call<ResponseBody>

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