package com.example.sumnote.ui.Note

import android.util.Log
import androidx.lifecycle.ViewModel

class NoteViewModel : ViewModel() {
    var itemList = ArrayList<NoteItem>() // 데이터 리스트

    // 데이터를 추가, 수정 또는 삭제하는 메서드를 정의할 수 있습니다.
    fun addItem(noteItem: NoteItem) {
        itemList.add(noteItem)
    }

    fun addItem(noteItems: ArrayList<NoteItem>) {
        Log.d("NoteViewModel", "test #1")
        itemList = noteItems
    }
}