package com.example.sumnote.ui.Quiz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sumnote.databinding.FragmentQuizViewerBinding

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
                quest = "What is the capital of France?",
                answerList = arrayListOf("Paris", "London", "Rome", "Berlin"),
                answerNum = 1,
                explanation = "The capital of France is Paris."
            ),
            Quiz(
                quest = "What is the largest planet in the solar system?",
                answerList = arrayListOf("Earth", "Mars", "Jupiter", "Saturn"),
                answerNum = 3,
                explanation = "The largest planet in the solar system is Jupiter."
            ),
            Quiz(
                quest = "What is the square root of 81?",
                answerList = arrayListOf("7", "8", "9", "10"),
                answerNum = 3,
                explanation = "The square root of 81 is 9."
            )
        )

        //뷰 페이저에 붙일 어댑터 생성
        val adapter = QuizPagerAdapter(this, quizzes)
        quizViewPager.adapter = adapter //어댑터 붙이기

        val progressBar = binding.progressBar
        progressBar.max = quizzes.size

        //현재 뷰 페이지만큼 프로그래스바에 반영하기
        quizViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                progressBar.progress = position + 1
            }
        })

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
    private val questions: List<Quiz>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun createFragment(position: Int): Fragment {
        return QuizFragment.newInstance(questions[position])
    }
}