package com.example.sumnote.ui.Dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.sumnote.MainActivity
import com.example.sumnote.databinding.InputNoteNameDialogBinding
import com.example.sumnote.ui.DTO.Request.CreateNoteRequest
import com.example.sumnote.ui.DTO.Note
import com.example.sumnote.ui.DTO.NotePage
import com.example.sumnote.api.RetrofitBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InputNoteNameDialog(note : NotePage) : DialogFragment() {
    private var note : NotePage
    private lateinit var docTitle : String
    private val successDialog = SuccessDialog()
    private val failDialog = FailDialog()

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
            serverNote()
        }

        return binding.root
    }

    // 토큰이 있기 때문에 필요없음
//    private fun makeNote() {
//        // 사용자 정보 요청 (기본)
//        UserApiClient.instance.me { user, error ->
//            if (error != null) {
//                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
//                val dialogBundle = Bundle()
//                dialogBundle.putString("dialogText", "잘못된 계정입니다.")
//                showFailDialog(dialogBundle)
//            } else if (user != null) {
//                var email = user.kakaoAccount?.email.toString()
//                serverNote(email)
//            }
//        }
//
//    }

    // 노트를 새롭게 저장하기
    private fun serverNote() {

        val token = MainActivity.prefs.getString("token", "")
        val request = CreateNoteRequest(Note(docTitle),  listOf(NotePage(note.title, note.content)))
        Log.d("#InputNoteNameDialog , MAKE_NOTE DATA:", "${docTitle}, ${note.title}, ${note.content},")

        val call = RetrofitBuilder.api.createNote(token, request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("#MAKE_NOTE: ", "SUCCESS")

                    CoroutineScope(Dispatchers.Main).launch {
                        dialog?.dismiss()
                        val dialogBundle = Bundle()
                        dialogBundle.putString("dialogText", "노트가 저장되었습니다!")
                        successDialog.arguments = dialogBundle
                        successDialog.show(requireActivity().supportFragmentManager, successDialog.tag)

                        // 지정된 딜레이 이후에 UI 조작 수행
                        delay(1500)

                        // UI 조작은 메인 스레드에서 실행되어야 합니다.
                        withContext(Dispatchers.Main) {
                            successDialog.dismiss()

                        }
                    }
                    findNavController().popBackStack()



                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("#MAKE_NOTE:", "FAILURE")
                    val dialogBundle = Bundle()
                    dialogBundle.putString("dialogText", "노트 저장에 실패하였습니다.")
                    showFailDialog(dialogBundle)
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신에 실패한 경우
                Log.d("#MAKE_NOTE FAIL: ", t.localizedMessage)
                val dialogBundle = Bundle()
                dialogBundle.putString("dialogText", "노트 저장에 실패하였습니다.")
                showFailDialog(dialogBundle)
            }
        })
    }

    private fun showFailDialog(dialogBundle : Bundle) {
        dialog?.dismiss()
        CoroutineScope(Dispatchers.Main).launch {
            failDialog.arguments = dialogBundle
            failDialog.show(requireActivity().supportFragmentManager, failDialog.tag)

            // 지정된 딜레이 이후에 UI 조작 수행
            delay(1500)

            // UI 조작은 메인 스레드에서 실행되어야 합니다.
            withContext(Dispatchers.Main) {
                failDialog.dismiss()

            }
        }
        findNavController().popBackStack()
    }

}