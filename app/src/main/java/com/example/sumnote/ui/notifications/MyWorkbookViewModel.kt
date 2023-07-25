package com.example.sumnote.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyWorkbookViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is My Workbook Fragment"
    }
    val text: LiveData<String> = _text
}