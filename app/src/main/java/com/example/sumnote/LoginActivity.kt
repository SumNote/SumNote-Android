package com.example.sumnote

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.sumnote.api.ApiManager
import com.example.sumnote.api.RetrofitBuilder
import com.example.sumnote.databinding.ActivityLoginBinding
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel.Companion.TAG
import com.example.sumnote.ui.DTO.User
import com.kakao.sdk.user.UserApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var kakaoViewModel: KakaoViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService : ApiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(application))[KakaoViewModel::class.java]
        apiService = RetrofitBuilder.api

        val intent = Intent(this, MainActivity::class.java)
        binding.btnLogin.apply {
            setOnClickListener{
                Log.d("#LoginActivity : ", "Login Btn Clicked")
                kakaoViewModel.kakaoLogin()
            }
        }

        kakaoViewModel.isLoggedIn.asLiveData().observe(this) { isLoggedIn ->
            if (isLoggedIn) { // 카카오 로그인 성공시 -> 스프링 서버로 로그인 요청
                loginRequest() // log check & call Login API
                startActivity(intent) // Call Application Main Activity
            }
        }

        // 상태바 숨기기
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private fun loginRequest(){
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                Log.i(TAG, "사용자 정보 요청 성공" +
                        "\n회원번호: ${user.id}" +
                        "\n이메일: ${user.kakaoAccount?.email}" +
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                        "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")

                val userInfo = User()
                userInfo.name = user.kakaoAccount?.profile?.nickname.toString()
                userInfo.email = user.kakaoAccount?.email.toString()

                springLogin(userInfo) // Spring Login Request
            }
        }
    }

    // 스프링 서버에 로그인 요청
    private fun springLogin(user: User) {

        Log.d("#LoginActivity : ", "call springLogin : ${user.email} ${user.name}")
        val call = apiService.getLoginResponse(user)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("#LoginActivity : ", "springLogin Success")
                    // 로그인 성공시 사용자 토큰 SharedPreference에 저장
                    response.headers()["Authorization"]?.let{ token ->
                        Log.d("#LoginActivity : ", "token is $token")
                        saveToken(token)
                    }
                } else {
                    // 통신에는 성공하였으나 응답 실패
                    Log.d("#LoginActivity :  ", "springLogin Response Fail")
                }
            }

            // 스프링 서버 통신 실패
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("#LoginActivity : ", "springLogin onFailure ${t.localizedMessage}")
            }
        })

    }

    // 공유 저장소에 사용자 토큰 저장 -> 추후 Spring 관련 모든 API 호출시 사용
    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("token", token).apply()
    }

    // 사용자 저장소에서 토큰 가져오는 예시 => RetrofitBuilder 클래스에 저장하는 방식 고려
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }


}