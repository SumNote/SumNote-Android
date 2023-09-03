package com.example.sumnote.ui.Quiz

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentQuizBinding

//data class Question(val quest: String, val answerList: ArrayList<String>, val answerNum : Int, val explanation : String)
//quest : 질문
//answerList : 정답 리스트(1번부터 4번까지)
//answerNum : 정답 번호(사용자 선택과 비교)

data class Quiz(
    val query: String,
    val answerList: ArrayList<String>,
    val answerNum: Int,
    val explanation: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: arrayListOf(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(query)
        parcel.writeList(answerList)
        parcel.writeInt(answerNum)
        parcel.writeString(explanation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Quiz> {
        override fun createFromParcel(parcel: Parcel): Quiz {
            return Quiz(parcel)
        }

        override fun newArray(size: Int): Array<Quiz?> {
            return arrayOfNulls(size)
        }
    }
}

class QuizFragment : Fragment() {

    private lateinit var quiz: Quiz //프래그먼트 생성시점에 객체 할당

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    //번들로 부터 quiz클래스 얻어오기(해당 페이지에 맞는)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            quiz = it.getParcelable(ARG_QUESTION)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)

        //받아온 퀴즈 정보 화면에 보여주기
        var query = binding.txtQuery
        query.text = quiz.query

        val radioGroup = binding.radioGroup
        val radioButton1 = binding.radioButton1
        val radioButton2 = binding.radioButton2
        val radioButton3 = binding.radioButton3
        val radioButton4 = binding.radioButton4

        radioButton1.text = quiz.answerList[0]
        radioButton2.text = quiz.answerList[1]
        radioButton3.text = quiz.answerList[2]
        radioButton4.text = quiz.answerList[3]

        var explanation = binding.txtExplanation
        explanation.text = quiz.explanation


        val listener = { buttonView: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                val selectedAnswer = when(buttonView.id) {
                    R.id.radioButton1 -> 0
                    R.id.radioButton2 -> 1
                    R.id.radioButton3 -> 2
                    R.id.radioButton4 -> 3
                    else -> -1
                }
                Log.d("quiz","$selectedAnswer, ${quiz.answerNum}")
                if (selectedAnswer+1 == quiz.answerNum) {
                    buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    Toast.makeText(context, "정답입니다!", Toast.LENGTH_SHORT).show()
                } else {
                    buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    when(quiz.answerNum) {
                        1 -> radioButton1.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                        2 -> radioButton2.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                        3 -> radioButton3.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                        4 -> radioButton4.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    }
                    Toast.makeText(context, "틀렸습니다!", Toast.LENGTH_SHORT).show()
                }
                explanation.visibility = View.VISIBLE
                explanation.text = quiz.explanation
                radioGroup.clearCheck()
                radioButton1.isEnabled = false
                radioButton2.isEnabled = false
                radioButton3.isEnabled = false
                radioButton4.isEnabled = false
            }
        }

        radioButton1.setOnCheckedChangeListener(listener)
        radioButton2.setOnCheckedChangeListener(listener)
        radioButton3.setOnCheckedChangeListener(listener)
        radioButton4.setOnCheckedChangeListener(listener)

        return binding.root
    }

    companion object {
        private const val ARG_QUESTION = "question"
        //번들로 객체를 전달하기 위해 question객체를 Parcelable객체로 변환
        fun newInstance(question: Quiz) = QuizFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_QUESTION, question)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}