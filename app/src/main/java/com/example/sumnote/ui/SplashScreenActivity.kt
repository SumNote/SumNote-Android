package com.example.sumnote.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.sumnote.LoginActivity
import com.example.sumnote.MainActivity
import com.example.sumnote.R

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen) //스플래시 스크린으로 보여줄 화면 설정

        // 일정 시간동안만 해당 엑티비티에서 머물고, 그 이후 엑티비티 전환(Handler사용)
        Handler(Looper.getMainLooper()).postDelayed({
            // 일정 시간(dailyVillis)이 지나면 MainActivity로 이동
            //val intent= Intent( this, MainActivity::class.java)
            // 일정 시간(dailyVillis)이 지나면 LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // 이전 키를 눌렀을 때 스플래시 스크린 화면으로 이동을 방지하기 위해
            // 이동한 다음 사용안함으로 finish 처리
            finish() //해당 엑티비티 종료

        }, 700) // 시간 0.7초 이후 실행 => 0.7초간 스플래시 스크린 보여줌
    }
}