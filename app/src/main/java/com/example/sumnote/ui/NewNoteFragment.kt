package com.example.sumnote.ui

import android.content.ContentValues
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentNewNoteBinding
import com.example.sumnote.ui.DTO.CreateNoteRequest
import com.example.sumnote.ui.Note.NoteItem
import com.example.sumnote.ui.DTO.Summary
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.example.sumnote.ui.DTO.User
import com.kakao.sdk.user.UserApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class NewNoteFragment : Fragment() {

    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

    lateinit var textTitle: String // ocr을 통해 얻어온 교과서의 텍스트들
    lateinit var textBook: String // ocr을 통해 얻어온 교과서의 텍스트들

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        val view = binding.root

        //번들을 통해 전닯 받은 값 화면에 뿌리기 => 추후 스프링에 전송하여 요약된 결과값 얻는 코드 작성 필요
        arguments?.let {

            textTitle = it.getString("title").toString()
            var title = binding.textView
            title.text = textTitle

            textBook = it.getString("textBook").toString()
            Log.d("newnote", textBook)
            var summaryNote = binding.textSummaryNote
            summaryNote.text = textBook

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val note = binding.note // layout2 LinearLayout 가져오기

        //현재 보유중인 노트 리스트
        var colorArray: Array<String> = arrayOf("데이터베이스", "알고리즘", "운영체제")


        //저장하기 버튼 클릭시
        binding.btnSaveNote.apply {
            setOnClickListener{
                Log.d("newNote","note saved!")
                val bitmap = viewToBitmap(note) // 프래그먼트의 뷰 전체를 Bitmap으로 변환
                saveNoteImageToMediaStore(bitmap) // Bitmap을 저장

                var selectedNoteTitle : String
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("저장할 노트를 선택해주세요")
                    .setItems(colorArray,
                        DialogInterface.OnClickListener { dialog, which ->
                            // 여기서 인자 'which'는 배열의 position을 의미
                            selectedNoteTitle = colorArray[which]
                            //선택된 노트 확인
                            Log.d("selectedNote",selectedNoteTitle)

                            findNavController().navigate(R.id.action_newNoteFragment_to_navigation_my_note)
                        })
                // 다이얼로그 띄우기
                builder.show()



//                //서버로 노트 저장하는 요청
//                arguments?.let {
//
//                    textTitle = it.getString("title").toString()
//                    var title = binding.textView
//                    title.text = textTitle
//
//                    textBook = it.getString("textBook").toString()
//                    Log.d("newnote", textBook)
//                    var summaryNote = binding.textSummaryNote
//                    summaryNote.text = textBook
//
//                    val summary = Summary()
//                    summary.title = textTitle
//                    summary.content = textBook
//
//                    makeNote(summary)
//
//                }
            }
        }


        //뒤로가기 버튼 클릭시 스택에서 제거 => 카메라 프래그먼트로 이동
        val btnReturnCamera = binding.btnReturnCamera
        btnReturnCamera.setOnClickListener {
            findNavController().popBackStack() //현재 프래그먼트 스택에서 제거
        }


    }

    //뷰 -> 비트맵 전환 : 이미지 저장을 위해
    private fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveNoteImageToMediaStore(bitmap: Bitmap) {
        val displayName = "note_image_" + SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date()) + ".png"

        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "MyNote"
                )
            }
        }

        val contentResolver = requireContext().contentResolver
        var imageUri: Uri? = null

        try {
            imageUri = contentResolver.insert(imageCollection, contentValues)
            imageUri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    Toast.makeText(requireContext(), "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun makeNote(summary: Summary){
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                var userInfo = User()
                userInfo.name = user.kakaoAccount?.profile?.nickname.toString()
                userInfo.email = user.kakaoAccount?.email.toString()

                Log.d("NOTELIST TEST : ", "name : " + userInfo.name + ", email" + userInfo.email)
                serverNote(userInfo, summary)
            }
        }

    }
    }
    private fun serverNote(user : User, summary: Summary) {

        val request = CreateNoteRequest(user, summary)
        val call = RetrofitBuilder.api.createNote(request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        Log.d("#MAKE_NOTE: ", jsonString)



                    } else {
                        // 응답 본문이 null인 경우 처리
                    }
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


