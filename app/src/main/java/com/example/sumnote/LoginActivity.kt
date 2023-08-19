package com.example.sumnote

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.sumnote.databinding.ActivityLoginBinding
import com.example.sumnote.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {

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

        val intent = Intent(this, MainActivity::class.java)
        //로그인 버튼 클릭시 : 카카오 로그인 수행하고 MainActivity로 넘어가기
        binding.btnLogin.apply {
            setOnClickListener{
                Log.d("btnClick : ","btn_login_clicked")
                //이곳에 카카오 로그인 관련 로직 작성 필요

                startActivity(intent)
            }
        }

        // 상태바 숨기기
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}