package com.example.sumnote.ui.Dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.sumnote.MainActivity
import com.example.sumnote.databinding.InputNoteNameDialogBinding
import com.example.sumnote.ui.DTO.ChangeNoteTitleRequest
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangeNoteTitleDialog(clickedNoteId : Int) : DialogFragment() {
    interface OnDialogResultListener {
        fun onDialogResult(result: Any)
    }

    private var listener: OnDialogResultListener? = null
    private var clickedNoteId : Int
    private lateinit var docTitle : String

    //생성자를 통해 유저의 노트아이템 리스트 얻어옴
    init {
        this.clickedNoteId = clickedNoteId
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
            updateTitle()
        }

        return binding.root
    }

    private fun updateTitle(){

        val request = ChangeNoteTitleRequest(docTitle)

        val token = MainActivity.prefs.getString("token", "")
        val call = RetrofitBuilder.api.updateNoteTitle(token, clickedNoteId, request)
        Log.d("#ChangeNoteTitleDialog clickedNoteId: ", "${clickedNoteId}")
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Log.d("#ChangeNoteTitleDialog changeTitle: ", "SUCCESS")

                    onWorkComplete(docTitle)
                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("#ChangeNoteTitleDialog changeTitle:", "FAILURE")
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신에 실패한 경우
                Log.d("#ChangeNoteTitleDialog FAIL: ", t.localizedMessage)
            }
        })

    }

    // 인터페이스 리스너 설정
    fun setOnDialogResultListener(listener: OnDialogResultListener) {
        this.listener = listener
    }

    // 다이얼로그에서 작업 완료 후 호출
    private fun onWorkComplete(result: String) {
        listener?.onDialogResult(result)
        dismiss()
    }
}

