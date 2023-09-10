package com.example.sumnote.ui.MyPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sumnote.databinding.FragmentMyPageBinding
import com.example.sumnote.LoginActivity
import com.example.sumnote.api.ApiManager
import com.example.sumnote.ui.Dialog.CircleProgressDialog
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MyPageFragment : Fragment() {
    lateinit var apiManager: ApiManager
    private val baseUrl = "http://10.0.2.2:8000/"

    private var _binding: FragmentMyPageBinding? = null
    private lateinit var kakaoViewModel: KakaoViewModel
    private val loadingDialog = CircleProgressDialog()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        // ViewModel 인스턴스 생성 후 프로퍼티에 할당
        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(requireActivity().application))[KakaoViewModel::class.java]

        binding.test.setOnClickListener {
//            showLoading()
            // 서버 테스트
            serverToGetPro()
        }

        // 프로필 세팅
        setInfo()

        val logoutBtn = binding.logout
        logoutBtn.setOnClickListener {
            kakaoViewModel.kakaoLogout()
            // 로그아웃 성공 시 실행되는 콜백
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Optional: 현재 프래그먼트를 종료하고 LoginActivity만 보여주는 것
        }

        var switchModeChange = binding.switchModeChange
        return binding.root
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        val button = view?.findViewById<Button>(R.id.goLogin)
//
//        button?.setOnClickListener {
//            val intent = Intent(context, LoginActivity::class.java)
//            startActivity(intent)
//
//        }
//
//
//    }

    // 프로필 세팅하기
    fun setInfo(){

        // ViewModel에서 가져온 사용자 이름을 TextView에 설정
//        kakaoViewModel.kakaoUser.observe(viewLifecycleOwner) { user ->
//            binding.nickname.text = user.name
//        }

        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                //이름
                val nickname = binding.nickname
                nickname.text = user.kakaoAccount?.profile?.nickname.toString()

                //이메일
                val email = binding.userEmail
                email.text = user.kakaoAccount?.email

                // 프로필 사진
                val profile = binding.usrProfile
                val imageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl
                // 이미지 로딩 라이브러리 (Glide, Picasso 등)를 사용하여 URL 이미지 설정 시:
                Glide.with(this)
                    .load(imageUrl)
                    .into(profile)
            }
        }
    }

    private fun serverToGetPro(){

        // timeout setting 해주기
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiManager = retrofit.create(ApiManager::class.java)

        // 요약된 노트 내용을 바탕으로 문제 생성
        val call = apiManager.generateProblem("")
        //val call = apiManager.uploadImage(imagePart)

        // 다이얼로그 표시
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 서버로 이미지 전송 성공시
                    Log.d("DjangoServer","send success")

                    // 서버 응답에 대한 추가 처리 코드 작성
                    val responseBody = response.body()?.string()

                    try {
                        // JSON 응답 파싱
                        val jsonObject = JSONObject(responseBody)
                        //String 값 받아오기  => 책의 모든 문자열(추후 ocr코드 개발되면 개선)
                        val question = jsonObject.getString("query")
                        val answerList = jsonObject.getJSONArray("answerList")
                        val answerNum = jsonObject.getString("answerNum")

                        Log.d("DjangoServer", "question's Text : $question")
                        Log.d("DjangoServer", "answer_list's Text : $answerList")
                        Log.d("DjangoServer", "answer_num's Text : $answerNum")



                    } catch (e: JSONException) {
                        Log.e("DjangoServer", "Error parsing JSON: ${e.message}")
                    }


                } else {
                    Log.e("DjangoServer", "Error response")
                }

                loadingDialog.dismiss()

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                Log.e("ImageUpload", "Image upload error: ${t.message}")
                loadingDialog.dismiss()
            }
        })
    }

    // 테스트 용
    private fun showLoading() {
        CoroutineScope(Dispatchers.Main).launch {
            loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)
            withContext(Dispatchers.Default) { delay(3000) }
            loadingDialog.dismiss()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}