package com.example.sumnote

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.sumnote.databinding.ActivityLoginBinding
import com.example.sumnote.databinding.ActivityMainBinding
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel.Companion.TAG
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.example.sumnote.ui.kakaoLogin.User
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var kakaoViewModel: KakaoViewModel
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // Optional: Add a listener to splash screen
//        splashScreen.setOnExitAnimationListener { splashScreenProvider ->
//            val fadeOut = ObjectAnimator.ofFloat(splashScreenProvider.view, View.ALPHA, 0f)
//            fadeOut.duration = 500L
//            fadeOut.start()
//        }
        // ViewModelProvider를 통해 ViewModel 인스턴스 생성
        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(application)).get(KakaoViewModel::class.java)

        val intent = Intent(this, MainActivity::class.java)
        //로그인 버튼 클릭시 : 카카오 로그인 수행하고 MainActivity로 넘어가기
        binding.btnLogin.apply {
            setOnClickListener{
                Log.d("btnClick : ","btn_login_clicked")
                //이곳에 카카오 로그인 관련 로직 작성 필요
                kakaoViewModel.kakaoLogin()
            }
        }
        // 로그인 완료까지 대기하는 코드 작성 (예: LiveData, Flow, 코루틴의 delay 등)
        Log.d("test", "activity start")
        kakaoViewModel.isLoggedIn.asLiveData().observe(this) { isLoggedIn ->
            // 로그인이 완료되면 getInfo()와 startActivity(intent) 호출
            if (isLoggedIn) {
                getInfo()
                startActivity(intent)
            }
        }

        // 상태바 숨기기
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);



    }
    fun getInfo(){
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                Log.i(TAG, "사용자 정보 요청 성공" +
                        "\n회원번호: ${user.id}" +
                        "\n이메일: ${user.kakaoAccount?.email}" +
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" )

                val userInfo = User()
                userInfo.name = user.kakaoAccount?.profile?.nickname.toString()
                userInfo.email = user.kakaoAccount?.email.toString()
                kakaoViewModel.kakaoUser.postValue(userInfo)


                Log.d("BUTTON CLICKED", "id: " + userInfo.name + ", pw: " + userInfo.email)

                Login(userInfo)
            }
        }
    }

    // Spring 서버와 통신하는 코드
    fun Login(user: User) {

        val call = RetrofitBuilder.api.getLoginResponse(user)
        call.enqueue(object : Callback<String> { // 비동기 방식 통신 메소드
            override fun onResponse( // 통신에 성공한 경우
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful()) { // 응답 잘 받은 경우
                    Log.d("RESPONSE: ", response.body().toString())

                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("RESPONSE", "FAILURE")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // 통신에 실패한 경우
                Log.d("CONNECTION FAILURE: ", t.localizedMessage)
            }
        })
    }
}