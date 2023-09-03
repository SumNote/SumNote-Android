package com.example.sumnote.ui.MyNote

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyNoteBinding
import com.example.sumnote.ui.Note.NoteItem
import com.example.sumnote.ui.Note.NoteRecyclerViewAdapter
import com.example.sumnote.ui.Quiz.QuizListItem
import com.example.sumnote.ui.Quiz.QuizRecyclerViewAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyNoteFragment : Fragment(){

    private var _binding: FragmentMyNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyNoteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //뷰 생성 시점
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onHiddenChanged(false) //카메라 프래그먼트에서 가렸던 바텀 뷰 다시 보이게 하기

        //리사이클러뷰 관련 코드 작성(노트)
        //id : note_list_recycler_view

        //리사이클러뷰에 사용할 아이템 리스트(테스트용)
        var noteList = ArrayList<NoteItem>()
        //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
        for(i in 0 until 10){
            noteList.add(NoteItem(i, "Note $i","2023.08.30 pm 16:53"))
        }

        val noteRecyclerViewAdapter = NoteRecyclerViewAdapter(noteList, object: NoteRecyclerViewAdapter.OnItemClickListener {
            override fun onNoteItemClick(position: Int) {
                // 노트 아이템 클릭시 동작
                findNavController().navigate(R.id.action_navigation_my_note_to_noteViewerFragment)
            }
        })
        val noteRecyclerView = binding.noteListRecyclerView //리사이클러뷰를 붙여줄 레이아웃 위치 가져오기
        noteRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false) //좌우로 보여주기
        noteRecyclerView.adapter = noteRecyclerViewAdapter

        //전체 보기
        val goAllNote = binding.txtGoAllNote
        goAllNote.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_my_note_to_allNoteFragment)
        }



        //리사이클러뷰 관련 코드 작성(퀴즈)
        //id : quiz_list_recycler_view

        //리사이클러뷰에 사용할 아이템 리스트(테스트용)
        var quizList = ArrayList<QuizListItem>()
        //data class QuizItem constructor(var id:Int, var date:Int, var month:Int)
        for(i in 0 until 10){
            quizList.add(QuizListItem(i, 14+i,"Aguest"))
        }

        val quizRecyclerViewAdapter = QuizRecyclerViewAdapter(quizList, LayoutInflater.from(this.context), object: QuizRecyclerViewAdapter.OnItemClickListener {
            override fun onQuizItemClick(position: Int) {
                // 퀴즈 아이템 클릭시 동작
                findNavController().navigate(R.id.action_navigation_my_note_to_quizViewerFragment)
            }
        })
        val quizRecyclerView = binding.quizListRecyclerView //리사이클러뷰를 붙여줄 레이아웃 위치 가져오기
        quizRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)//위아래로 보여주기
        quizRecyclerView.adapter = quizRecyclerViewAdapter


    }


    override fun onHiddenChanged(hidden: Boolean) {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)

        if (hidden) {
            bottomNavigationView?.visibility = View.GONE
        } else {
            bottomNavigationView?.visibility = View.VISIBLE
        }
    }
}

