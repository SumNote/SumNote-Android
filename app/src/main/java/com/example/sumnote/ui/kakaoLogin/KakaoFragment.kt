package com.example.sumnote.ui.kakaoLogin

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.example.sumnote.MyApplication
import com.example.sumnote.R
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel.Companion.TAG
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KakaoFragment : Fragment() {

    companion object {
        fun newInstance() = KakaoFragment()
    }

    private lateinit var viewModel: KakaoViewModel
    private var appUser: User? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("최강", "#1")
        return inflater.inflate(R.layout.fragment_kakao, container, false)
    }

    @SuppressLint("MissingInflatedId")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(requireActivity().application)).get(KakaoViewModel::class.java)
        // TODO: Use the ViewModel강

        val btnKakaoLogin = view?.findViewById<Button>(R.id.btn_kakao_login)
        val btnKakaoLogout = view?.findViewById<Button>(R.id.btn_kakao_logout)
        val tvLoginStatus = view?.findViewById<TextView>(R.id.tv_login_status)

        btnKakaoLogin?.setOnClickListener {
            viewModel.kakaoLogin()
        }

        btnKakaoLogout?.setOnClickListener {
            viewModel.kakaoLogout()
        }

        viewModel.isLoggedIn.asLiveData().observe(viewLifecycleOwner) { isLoggedIn ->
            val loginStatusInfoTitle = if (isLoggedIn) "로그인 상태" else "로그아웃 상태"
            tvLoginStatus?.text = loginStatusInfoTitle
        }

        val test = view?.findViewById<Button>(R.id.buttonInfo)
        test?.setOnClickListener {
            getInfo()
        }

        Log.d("viewModel Test", "userInfo : " + appUser?.name + ", " + appUser?.email)

        viewModel.kakaoUser.value = appUser

        Log.d("viewModel Test2", "userInfo : " + viewModel.kakaoUser.toString())
        Log.d("viewModel Test2", "userInfo : " + appUser?.name + ", " + appUser?.email)

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
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                        "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")


                appUser?.name = user.kakaoAccount?.profile?.nickname.toString()
                appUser?.email = user.kakaoAccount?.email.toString()
                appUser?.imageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl.toString()

                Log.d("BUTTON CLICKED", "id2: " + appUser?.name + ", pw2: " + appUser?.email + ", Image url : " + appUser?.imageUrl)


                Login(appUser ?: User())
            }
        }
    }

    // 스프링과 통신
    fun Login(user: User){

        val call = RetrofitBuilder.api.getLoginResponse(user)
        Log.d("Login", "정보를 받았습니다 ${call}")
        call.enqueue(object : Callback<String> { // 비동기 방식 통신 메소드
            override fun onResponse( // 통신에 성공한 경우
                call: Call<String>,
                response: Response<String>
            ) {
                if(response.isSuccessful()){ // 응답 잘 받은 경우
                    Log.d("RESPONSE: ", response.body().toString())

                }else{
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