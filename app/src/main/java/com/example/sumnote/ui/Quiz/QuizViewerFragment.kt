package com.example.sumnote.ui.Quiz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sumnote.databinding.FragmentQuizViewerBinding
import com.example.sumnote.ui.Note.NoteViewerFragment

class QuizViewerFragment : Fragment() {
    private var _binding: FragmentQuizViewerBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizViewerBinding.inflate(inflater, container, false)

        //퀴즈들을 보여주기 위한 뷰 페이저 가져오기
        var quizViewPager = binding.quizViewPager

        //테스트용 더미 데이터 생성 => 여기서 서버로부터 정보 받아와 파싱하는 코드 작성 필요
        val quizzes = listOf(
            Quiz(
                query = "What is the capital of France?",
                answerList = arrayListOf("① Paris", "② London", "③ Rome", "④ Berlin"),
                answerNum = 1,
                explanation = "The capital of France is Paris."
            ),
            Quiz(
                query = "What is the largest planet in the solar system?",
                answerList = arrayListOf("① Earth", "② Mars", "③ Jupiter", "④ Saturn"),
                answerNum = 3,
                explanation = "The largest planet in the solar system is Jupiter."
            ),
            Quiz(
                query = "What is the square root of 81?",
                answerList = arrayListOf("① 7", "② 8", "③ 9", "④ 10"),
                answerNum = 3,
                explanation = "The square root of 81 is 9."
            ),
            Quiz(
                query = "What is the capital of France?",
                answerList = arrayListOf("① Paris", "② London", "③ Rome", "④ Berlin"),
                answerNum = 1,
                explanation = "The capital of France is Paris."
            )

        )

        //뷰 페이저에 붙일 어댑터 생성
        val adapter = QuizPagerAdapter(this, quizzes)
        quizViewPager.adapter = adapter //어댑터 붙이기

        val progressBar = binding.progressBar
        progressBar.max = quizzes.size
        //txt_current_question_num
        var currQuizNum = binding.txtCurrentQuestionNum
        currQuizNum.text = "1"
        //현재 뷰 페이지만큼 프로그래스바에 반영하기
        quizViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                progressBar.progress = position + 1
                currQuizNum.text = (position + 1).toString()
            }
        })


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

// QuestionPagerAdapter 클래스
class QuizPagerAdapter(
    fragmentActivity: QuizViewerFragment,
    private val quizzes: List<Quiz>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return quizzes.size
    }

    override fun createFragment(position: Int): Fragment {
        return QuizFragment.newInstance(quizzes[position])
    }
}