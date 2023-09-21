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
import com.example.sumnote.ui.DTO.User
import com.example.sumnote.ui.Note.AllNoteRecyclerViewAdapter
import com.example.sumnote.ui.Note.NoteItem
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kakao.sdk.user.UserApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllQuizFragment : Fragment() {


    private var _binding: FragmentAllQuizBinding? = null
    private val binding get() = _binding!!

    private var quizList = ArrayList<QuizListItem>()
    private lateinit var allQuizRecyclerViewAdapter: AllQuizRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllQuizBinding.inflate(inflater, container, false)

        //사용자 정보 얻어오기
        getUser()

        allQuizRecyclerViewAdapter = AllQuizRecyclerViewAdapter(quizList,LayoutInflater.from(this.context),
            object : AllQuizRecyclerViewAdapter.OnItemClickListener{
                override fun onAllQuizItemClick(position: Int) {
                    // 퀴즈 아이템 클릭시 동작
                    // 클릭한 문제집 아이디 가져오기
                    val clickedQuizId = quizList[position].id
                    val quizTitle = quizList[position].quiz_doc_title

                    // 번들을 생성하고 클릭한 퀴즈 정보 입력
                    val bundle = Bundle()
                    bundle.putInt("quizId", clickedQuizId)
                    bundle.putString("quiz_doc_title", quizTitle)
                    Log.d("QuizDoc CLICKED", "test : $clickedQuizId")
                    findNavController().navigate(R.id.action_allQuizFragment_to_quizViewerFragment,bundle)
                }
            })

        val allQuizRecyclerView = binding.allQuizListRecyclerView
        allQuizRecyclerView.layoutManager = LinearLayoutManager(this.context,
            LinearLayoutManager.VERTICAL,false)
        allQuizRecyclerView.adapter = allQuizRecyclerViewAdapter

        //뒤로가기 버튼
        val btmBack = binding.imgBtnBack
        btmBack.setOnClickListener{
            findNavController().navigateUp()
        }

        return binding.root
    }


    private fun getUser() {

        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                var userInfo = User()
                userInfo.name = user.kakaoAccount?.profile?.nickname.toString()
                userInfo.email = user.kakaoAccount?.email.toString()

                initQuizList(userInfo) //퀴즈 얻어오기
            }
        }

    }

    data class QuizItemResult(
        @SerializedName("quizList") val quizList: List<QuizListItem>
    )
    //사용자 퀴즈 목록 얻어오기
    private fun initQuizList(user : User){

        Log.d("getUser() TEST", user.name + " and " + user.email)

        val call = RetrofitBuilder.api.getQuizList(user.email.toString())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        Log.d("#Spring-Quiz Success:", jsonString)

                        val gson = Gson()
                        val result = gson.fromJson(jsonString, QuizItemResult::class.java)

                        val quizList = result.quizList
                        for(quiz in quizList){
                            Log.d("#Spring-Quiz",quiz.quiz_doc_title)
                            addQuizList(quiz)
                        }
                    } else {
                        // 응답 본문이 null인 경우 처리
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

    private fun addQuizList(quizListItem: QuizListItem){

        // 중복 체크: 이미 리스트에 같은 ID의 노트가 있는지 확인
        val isDuplicate = quizList.any { it.id == quizListItem.id }

        if (!isDuplicate) {
            if (quizList.size < 10) {
                // 10개 미만일 때만 요소 추가
                quizList.add(0, quizListItem)
            }

            // 리사이클러뷰 업데이트
            allQuizRecyclerViewAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}