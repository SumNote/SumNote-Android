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
                query = "데이터의 구조와 설계를 나타내는 것은?",
                answerList = arrayListOf("① 레코드", "② 필드", "③ 스키마", "④ 데이터베이스"),
                answerNum = 3,
                explanation = "스키마는 데이터베이스의 구조와 설계를 나타냅니다."
            )
            ,
            Quiz(
                query = "데이터를 조회하는 SQL 명령은?",
                answerList = arrayListOf("① INSERT", "② DELETE", "③ UPDATE", "④ SELECT"),
                answerNum = 4,
                explanation = "SELECT는 데이터를 조회하는 SQL 명령입니다."
            ),
            Quiz(
                query = "데이터 중복을 최소화하는 설계 방법은?",
                answerList = arrayListOf("① 인덱스", "② 정규화", "③ 뷰", "④ 무결성"),
                answerNum = 2,
                explanation = "정규화는 데이터 중복을 최소화하는 설계 방법입니다."
            ),
            Quiz(
                query = "여러 연산의 묶음으로, 모두 성공하거나 모두 실패하는 것은?",
                answerList = arrayListOf("① 인증", "② 인가", "③ 트랜잭션", "④ 암호화"),
                answerNum = 3,
                explanation = "트랜잭션은 여러 연산의 묶음으로, 모두 성공하거나 모두 실패합니다."
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