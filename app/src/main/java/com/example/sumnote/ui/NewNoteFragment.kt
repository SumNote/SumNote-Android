package com.example.sumnote.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sumnote.databinding.FragmentNewNoteBinding


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
}