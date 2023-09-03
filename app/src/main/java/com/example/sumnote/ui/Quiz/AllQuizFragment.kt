package com.example.sumnote.ui.Quiz

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentAllQuizBinding


class AllQuizFragment : Fragment() {


    private var _binding: FragmentAllQuizBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllQuizBinding.inflate(inflater, container, false)


        Log.d("debug!","#2")

        //테스트용 더미 데이터 생성
        var quizList = ArrayList<QuizListItem>()
        //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
        for(i in 0 until 10){
            quizList.add(QuizListItem(i, i,"Aguest"))
        }

        Log.d("debug!","#3")
        val allQuizRecyclerViewAdapter = AllQuizRecyclerViewAdapter(quizList,LayoutInflater.from(this.context),
            object : AllQuizRecyclerViewAdapter.OnItemClickListener{
                override fun onAllQuizItemClick(position: Int) {
                    //position 넘겨줘야함
                    Log.d("debug!","#4")
                    findNavController().navigate(R.id.action_allQuizFragment_to_quizViewerFragment)
                }
            })

        val allQuizRecyclerView = binding.allQuizListRecyclerView
        allQuizRecyclerView.layoutManager = LinearLayoutManager(this.context,
            LinearLayoutManager.VERTICAL,false)
        allQuizRecyclerView.adapter = allQuizRecyclerViewAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}