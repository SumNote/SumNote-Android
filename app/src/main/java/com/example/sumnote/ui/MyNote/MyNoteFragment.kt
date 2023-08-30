package com.example.sumnote.ui.MyNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyNoteBinding
import com.example.sumnote.ui.Note.NoteItem
import com.example.sumnote.ui.Note.NoteRecyclerViewAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyNoteFragment : Fragment() {

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

        //리사이클러뷰 관련 코드 작성
        //id : note_list_recycler_view

        //리사이클러뷰에 사용할 아이템 리스트(테스트용)
        var noteList = ArrayList<NoteItem>()
        //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
        for(i in 0 until 10){
            noteList.add(NoteItem(i, "Note $i","2023.08.30 pm 16:53"))

        }

        val adapter = NoteRecyclerViewAdapter(noteList, LayoutInflater.from(this.context))
        val recyclerView = binding.noteListRecyclerView //리사이클러뷰를 붙여줄 레이아웃 위치 가져오기
        recyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false) //선형적으로 표현할 경우
        recyclerView.adapter = adapter


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