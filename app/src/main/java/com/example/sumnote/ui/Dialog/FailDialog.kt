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
import com.example.sumnote.databinding.FailDialogBinding
import com.example.sumnote.databinding.ProgressDialogBinding
import com.example.sumnote.databinding.SuccessDialogBinding

class FailDialog: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FailDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 투명 배경
        dialog?.setCanceledOnTouchOutside(false) // 주변 터치 방지

        // TextView 찾기
        val dialogTextView = binding.loadingText

        // 다른 프래그먼트로부터 전달받은 텍스트 설정
        val arguments = arguments
        if (arguments != null) {
            val newText = arguments.getString("dialogText", "실패하였습니다!")
            dialogTextView.text = newText
        }

        return binding.root
    }
}