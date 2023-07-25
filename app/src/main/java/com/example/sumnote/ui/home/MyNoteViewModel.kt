package com.example.sumnote.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyNoteViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is My Note Fragment"
    }
    val text: LiveData<String> = _text
}