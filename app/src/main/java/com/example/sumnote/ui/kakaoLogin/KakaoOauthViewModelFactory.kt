package com.example.sumnote.ui.kakaoLogin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application

class KakaoOauthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KakaoViewModel::class.java)) {
            return KakaoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}