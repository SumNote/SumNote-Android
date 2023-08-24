package com.example.sumnote.ui.MyPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyPageBinding
import com.example.sumnote.LoginActivity
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private lateinit var kakaoViewModel: KakaoViewModel
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        // ViewModel 인스턴스 생성 후 프로퍼티에 할당
        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(requireActivity().application))[KakaoViewModel::class.java]

        val nickname = binding.nickname
        nickname.text = "test"

        kakaoViewModel.kakaoUser.observe(viewLifecycleOwner, Observer { userInfo ->
            // 옵저버가 활성화되면 호출되는 코드
            // userInfo를 사용하여 UI 업데이트 등을 수행할 수 있습니다.
            val myName = userInfo.name
            Log.d("userNameTest", "${myName}입니다!!")
            nickname.text = myName
        })

        val logoutBtn = binding.logout
        logoutBtn.setOnClickListener {
            kakaoViewModel.kakaoLogout()
            // 로그아웃 성공 시 실행되는 콜백
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Optional: 현재 프래그먼트를 종료하고 LoginActivity만 보여주는 것
        }

        var switchModeChange = binding.switchModeChange

        //이곳에 스위치의 변화를 감지하여 테마 변경하는 코드 작성할것

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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}