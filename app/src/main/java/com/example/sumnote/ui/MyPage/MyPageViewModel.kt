package com.example.sumnote.ui.MyPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


//ViewModel은 View와 Model 사이의 매개체 역할
class MyPageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is My Workbook Fragment"
    }
    val text: LiveData<String> = _text

}