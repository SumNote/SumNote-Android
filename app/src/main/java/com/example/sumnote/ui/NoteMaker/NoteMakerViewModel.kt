package com.example.sumnote.ui.NoteMaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NoteMakerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Note Maker Fragment"
    }
    val text: LiveData<String> = _text
}