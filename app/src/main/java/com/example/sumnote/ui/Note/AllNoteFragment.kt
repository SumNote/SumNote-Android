package com.example.sumnote.ui.Note

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentAllNoteBinding


class AllNoteFragment : Fragment() {


    private var _binding: FragmentAllNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllNoteBinding.inflate(inflater, container, false)


        //테스트용 더미 데이터 생성
        var noteList = ArrayList<NoteItem>()
        //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
        for(i in 0 until 10){
            noteList.add(NoteItem(i, "Note $i","2023.08.30 pm 16:53"))
        }

        //모든 노트 보기 리사이클러뷰 적용
        val allNoteRecyclerViewAdapter = AllNoteRecyclerViewAdapter(noteList, LayoutInflater.from(this.context),
            object : AllNoteRecyclerViewAdapter.OnItemClickListener{
                override fun onAllNoteItemClick(position: Int){
                    //position을 같이 넣어야 함을 잊지말것
                    findNavController().navigate(R.id.action_allNoteFragment_to_noteViewerFragment)
                    Log.d("checked","$position")
                }
        })

        val allNoteRecyclerView = binding.allNoteListRecyclerView //리사이클러뷰를 붙여줄 레이아웃 위치 가져오기
        allNoteRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        allNoteRecyclerView.adapter = allNoteRecyclerViewAdapter


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}