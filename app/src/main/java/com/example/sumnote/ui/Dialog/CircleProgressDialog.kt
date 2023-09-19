package com.example.sumnote.ui.Dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.sumnote.R
import com.example.sumnote.databinding.ProgressDialogBinding

class CircleProgressDialog: DialogFragment() {

    private lateinit var loadingText : TextView
    // TextView를 업데이트하기 위한 함수
    fun updateTextView(newText: String) {
        loadingText.text = newText
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ProgressDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 투명 배경
        dialog?.setCanceledOnTouchOutside(false) // 주변 터치 방지


        // Dialog 내의 TextView를 찾아 변수에 할당
        loadingText = binding.loadingText

        return binding.root
    }
}