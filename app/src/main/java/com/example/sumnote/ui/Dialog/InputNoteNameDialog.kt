package com.example.sumnote.ui.Dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.sumnote.R
import com.example.sumnote.databinding.InputNoteNameDialogBinding
import com.example.sumnote.databinding.ProgressDialogBinding
import com.example.sumnote.ui.DTO.CreateNoteRequest
import com.example.sumnote.ui.Note.NoteItem
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.kakao.sdk.user.UserApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InputNoteNameDialog(note : UpdateNoteRequest) : DialogFragment() {
    private var note : UpdateNoteRequest
    private lateinit var docTitle : String

    //생성자를 통해 유저의 노트아이템 리스트 얻어옴
    init {
        this.note = note
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = InputNoteNameDialogBinding.inflate(inflater, container, false)

        dialog?.setCanceledOnTouchOutside(false) // 주변 터치 방지

        val saveBtn = binding.submitButton
        saveBtn.setOnClickListener {
            docTitle = binding.editText.text.toString()
            if (docTitle == "")
                docTitle = "New Note"
            makeNote(docTitle)
        }

        return binding.root
    }

    private fun makeNote(docTitle : String) {
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                var email = user.kakaoAccount?.email.toString()
                serverNote(email)
            }
        }

    }

    private fun serverNote(email: String) {

        val request = CreateNoteRequest(email, docTitle, note.addTitle, note.addContent)
        val call = RetrofitBuilder.api.createNote(request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("#MAKE_NOTE: ", "SUCCESS")
                    dialog?.dismiss()

                    findNavController().navigate(R.id.action_newNoteFragment_to_navigation_my_note)

                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("#MAKE_NOTE:", "FAILURE")
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신에 실패한 경우
                Log.d("#MAKE_NOTE FAIL: ", t.localizedMessage)
            }
        })
    }

}