package com.example.sumnote.ui.Note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentNoteViewerBinding

class NoteViewerFragment : Fragment() {

    private var _binding: FragmentNoteViewerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteViewerBinding.inflate(inflater, container, false)

        //노트들을 보여주기 위한 뷰 페이저
        var noteViewPager = binding.noteViewPager

        //테스트용 더미 데이터 생성 => 여기서 서버로부터 정보 받아와 파싱하는 코드 작성 필요
        val notes = listOf(
            Note(
                noteTitle = "France?",
                summary = "The capital of France is Paris."
            ),
            Note(
                noteTitle = "Largest planet",
                summary = "The largest planet in the solar system is Jupiter."
            ),
            Note(
                noteTitle = "Root of 81?",
                summary = "The square root of 81 is 9."
            ),
            Note(
                noteTitle = "France?",
                summary = "The capital of France is Paris."
            )

        )

        //뷰 페이저에 붙일 어댑터 생성
        val noteViewAdapter = NotePagerAdapter(this, notes)
        noteViewPager.adapter = noteViewAdapter //어댑터 붙이기


        //뒤로가기 버튼
        val btmBack = binding.imgBtnBack
        btmBack.setOnClickListener{
            findNavController().navigateUp()
        }




        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// NotePagerAdapter 클래스
class NotePagerAdapter(
    fragmentActivity: NoteViewerFragment,
    private val notes: List<Note>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun createFragment(position: Int): Fragment {
        return NoteFragment.newInstance(notes[position])
    }
}