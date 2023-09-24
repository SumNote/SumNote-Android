package com.example.sumnote.ui.Quiz

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sumnote.databinding.FragmentQuizViewerBinding
import com.example.sumnote.ui.Note.NotePagerAdapter
import com.example.sumnote.ui.Note.NoteViewerFragment
import com.example.sumnote.ui.Note.Page
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizViewerFragment : Fragment() {
    private var _binding: FragmentQuizViewerBinding? = null
    private val binding get() = _binding!!

    private var clickedQuizId: Int = -1
    private lateinit var quizViewAdapter: QuizPagerAdapter
    private lateinit var quizzes: MutableList<Quiz>

    //커밋용
    //프로그래스 바
    lateinit var progressBar : ProgressBar
    //txt_current_question_num
    lateinit var currQuizNum : TextView
    lateinit var totalQuizNum : TextView
    //퀴즈 제목
    lateinit var quizViewerTitle : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizViewerBinding.inflate(inflater, container, false)

        //퀴즈들을 보여주기 위한 뷰 페이저 가져오기
        var quizViewPager = binding.quizViewPager

        quizzes = mutableListOf()
        //뷰 페이저에 붙일 어댑터 생성
        quizViewAdapter = QuizPagerAdapter(this, quizzes)
        quizViewPager.adapter = quizViewAdapter //어댑터 붙이기

        progressBar = binding.progressBar
        currQuizNum = binding.txtCurrentQuestionNum
        totalQuizNum = binding.txtQuestionNum
        quizViewerTitle = binding.quizViewerTitle

        arguments?.let {
            clickedQuizId = it.getInt("quizId")
            var quizTitle = it.getString("quiz_doc_title")
            Log.d("QuizViewr", "test : $clickedQuizId")
            Log.d("QuizViewr", "test : $quizTitle")
            quizViewerTitle.text = quizTitle //퀴즈 제목 받아오기

            //얻어온 정보를 바탕으로 서버에 퀴즈 데이터 요청 + 퀴즈 클래스 생성
            if (clickedQuizId != -1) {
                //클릭한 문제집에 대한 퀴즈 정보를 서버에 요청
                Log.d("QuizViewr", "#1")
                detailQuiz(clickedQuizId)

                currQuizNum.text = "1"
                //현재 뷰 페이지만큼 프로그래스바에 반영하기
                quizViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        progressBar.progress = position + 1
                        currQuizNum.text = (position + 1).toString()
                    }
                })
            }
        }

        //뒤로가기 버튼에 대한 동작처리
        binding.imgBtnBack.setOnClickListener{
            findNavController().navigateUp() //뒤로 가기
        }


        return binding.root
    }

    //서버로부터 클릭한 노트에 대한 페이지 요청
    private fun detailQuiz(clickedQuizId: Int) {
        val call = RetrofitBuilder.api.detailQuiz(clickedQuizId)
        call.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        val json = JSONObject(jsonString)

                        // 로그 출력 형식 통일
                        Log.d("#QUIZVIWER DETAIL Success:", jsonString)

                        val regexPattern = Regex("\\[([\\s\\S]*?)\\]")  // 수정된 정규식 패턴

                        val questions = regexPattern.findAll(json.getString("question")).map { it.groupValues[1] }.toList()
                        Log.d("#QUIZVIWER DETAIL questions:", questions.toString())

                        val selections = regexPattern.findAll(json.getString("selections")).map { it.groupValues[1] }.toList()
                        Log.d("#QUIZVIWER selections:", selections.toString())

                        val answers = regexPattern.findAll(json.getString("answer")).map { it.groupValues[1].toInt() }.toList()
                        Log.d("#QUIZVIWER answers:", answers.toString())

                        val commentary = regexPattern.findAll(json.getString("commentary")).map { it.groupValues[1] }.toList()
                        Log.d("#QUIZVIWER commentary:", commentary.toString())

                        if (questions.size == answers.size && answers.size == commentary.size) {
                            quizzes.clear()
                            for (index in questions.indices) {
                                val start = index * 4
                                val end = (index + 1) * 4
                                val answerList = ArrayList(selections.subList(start, end))

                                val quiz = Quiz(
                                    query = questions[index],
                                    answerList = answerList,
                                    answerNum = answers[index],
                                    explanation = commentary[index]
                                )
                                Log.d("#QUIZVIWER query",quiz.query)
                                Log.d("#QUIZVIWER answerList",quiz.answerList.toString())
                                Log.d("#QUIZVIWER answerNum",quiz.answerNum.toString())
                                Log.d("#QUIZVIWER explanation",quiz.explanation)
                                quizzes.add(quiz)
                            }

                            Log.d("#DETAIL RESULT SIZE",quizzes.size.toString())
                            progressBar.max = quizzes.size
                            totalQuizNum.text = "/" + quizzes.size.toString() //총 문제 수 지정
                            Log.d("ProgressBarSize", quizzes.size.toString())
                            quizViewAdapter.notifyDataSetChanged()
                        }
                        else {
                            // 데이터 길이 불일치 로그 처리
                            Log.e("ParsingError", "Questions, answers, and commentary size mismatch!")
                        }

                    } else {
                        // 응답 본문이 null인 경우 처리
                        Log.e("#Error:", "Response body is null.")
                    }
                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("#SPRING SERVER:", "FAILURE")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신에 실패한 경우
                Log.d("CONNECTION FAILURE #SPRING SERVER: ", t.localizedMessage)
            }
        })
    }

}

// QuestionPagerAdapter 클래스
class QuizPagerAdapter(
    fragmentActivity: QuizViewerFragment,
    private val quizzes: MutableList<Quiz>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return quizzes.size
    }

    override fun createFragment(position: Int): Fragment {
        return QuizFragment.newInstance(quizzes[position])
    }
}