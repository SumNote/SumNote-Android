package com.example.sumnote.ui

import android.content.ContentValues
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
import com.example.sumnote.databinding.FragmentNewNoteBinding
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NewNoteFragment : Fragment() {
    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

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

        binding.btnSaveNote.apply {
            setOnClickListener{
                Log.d("newNote","note saved!")
                val bitmap = viewToBitmap(note) // 프래그먼트의 뷰 전체를 Bitmap으로 변환
                saveNoteImageToMediaStore(bitmap) // Bitmap을 저장
            }
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

}