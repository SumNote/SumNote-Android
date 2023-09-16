package com.example.sumnote.ui.MyNote

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyNoteBinding
import com.example.sumnote.ui.Note.NoteItem
import com.example.sumnote.ui.Note.NoteRecyclerViewAdapter
import com.example.sumnote.ui.Quiz.QuizListItem
import com.example.sumnote.ui.Quiz.QuizRecyclerViewAdapter
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.example.sumnote.ui.DTO.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kakao.sdk.user.UserApiClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class MyNoteFragment : Fragment(){

    private var _binding: FragmentMyNoteBinding? = null
    private lateinit var kakaoViewModel: KakaoViewModel
    private val binding get() = _binding!!

    private var noteList = ArrayList<NoteItem>()
    private var quizList = ArrayList<QuizListItem>()
    private lateinit var noteRecyclerViewAdapter: NoteRecyclerViewAdapter
    private lateinit var quizRecyclerViewAdapter: QuizRecyclerViewAdapter

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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onHiddenChanged(false) //카메라 프래그먼트에서 가렸던 바텀 뷰 다시 보이게 하기

        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(requireActivity().application))[KakaoViewModel::class.java]

        getUser() //로그인 한 유저에 대한 노트 및 퀴즈 리스트 받아오기

        noteRecyclerViewAdapter = NoteRecyclerViewAdapter(noteList, object: NoteRecyclerViewAdapter.OnItemClickListener {
            override fun onNoteItemClick(position: Int) {

                // 클릭한 노트 아이디 가져오기
                val clickedNoteId = noteList[position].id
                val noteTitle = noteList[position].sum_doc_title

                // 번들을 생성하고 클릭한 노트 아이디를 추가
                val bundle = Bundle()
                bundle.putInt("noteId", clickedNoteId)
                bundle.putString("sum_doc_title", noteTitle)
//                bundle.putInt("position", position) //번호 넘기기
                Log.d("NOTE CLICKED", "test : $clickedNoteId")

                // 노트 아이템 클릭시 동작 => 번들을 통해 현재 클릭한 노트가 어떤 노트인지 전달
                findNavController().navigate(R.id.action_navigation_my_note_to_noteViewerFragment, bundle)

            }
        })

        val noteRecyclerView = binding.noteListRecyclerView //리사이클러뷰를 붙여줄 레이아웃 위치 가져오기
        noteRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false) //좌우로 보여주기
        noteRecyclerView.adapter = noteRecyclerViewAdapter

        //노트 전체 보기
        val goAllNote = binding.txtGoAllNote
        goAllNote.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_my_note_to_allNoteFragment)
        }
        
        quizRecyclerViewAdapter = QuizRecyclerViewAdapter(quizList, LayoutInflater.from(this.context), object: QuizRecyclerViewAdapter.OnItemClickListener {
            override fun onQuizItemClick(position: Int) {
                // 퀴즈 아이템 클릭시 동작
                // 클릭한 문제집 아이디 가져오기
                val clickedQuizId = quizList[position].id
                val quizTitle = quizList[position].quiz_doc_title

                // 번들을 생성하고 클릭한 퀴즈 정보 입력
                val bundle = Bundle()
                bundle.putInt("quizId", clickedQuizId)
                bundle.putString("quiz_doc_title", quizTitle)
                Log.d("QuizDoc CLICKED", "test : $clickedQuizId")
                //번들과 함께 뷰 이동
                findNavController().navigate(R.id.action_navigation_my_note_to_quizViewerFragment,bundle)
            }
        })
        val quizRecyclerView = binding.quizListRecyclerView //리사이클러뷰를 붙여줄 레이아웃 위치 가져오기
        quizRecyclerView.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)//위아래로 보여주기
        quizRecyclerView.adapter = quizRecyclerViewAdapter

        //퀴즈 전체 보기
        val goAllQuiz = binding.txtGoAllQuiz
        goAllQuiz.setOnClickListener{
            Log.d("debug!","#1")
            findNavController().navigate(R.id.action_navigation_my_note_to_allQuizFragment)
        }


        //사용자 정보 보기
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {

                // 프로필 사진
                val profile = binding.usrProfile
                val imageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl
                // 이미지 로딩 라이브러리 (Glide, Picasso 등)를 사용하여 URL 이미지 설정 시:
                Glide.with(this)
                    .load(imageUrl)
                    .into(profile)
            }
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)

        if (hidden) {
            bottomNavigationView?.visibility = View.GONE
        } else {
            bottomNavigationView?.visibility = View.VISIBLE
        }
    }

    // Response 데이터 클래스를 정의합니다.
    data class Result(
        @SerializedName("noteList") val noteList: List<NoteItem>
    )

    //서버로부터 노트 목록 받아오기
    private fun initNoteList(user : User){

        Log.d("getUser() TEST", user.name + " and " + user.email)

        val call = RetrofitBuilder.api.getSumNotes(user.email.toString())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        Log.d("#SPRING Success:", jsonString)

                        val gson = Gson()
                        val result = gson.fromJson(jsonString, Result::class.java)

                        // 'noteList'에 포함된 노트 목록에 접근합니다.
                        val notes = result.noteList
                        for (note in notes) {
                            println("ID: ${note.id}")
                            println("Title: ${note.sum_doc_title}")
//                            println("Content: ${note.generatedDate}")
                            println("Created At: ${note.created_at}")
                            Log.d("GET NOTELIST" , "ID : ${note.id} title : ${note.sum_doc_title} created_at : ${note.created_at}")

                            val myNote = NoteItem(note.id, note.sum_doc_title, note.created_at)
                            addNoteList(myNote)
                        }


                    } else {
                        // 응답 본문이 null인 경우 처리
                        Log.d("#SPRING Success:", "NO RESPONSE")
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


    private fun getUser() {

        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                var userInfo = User()
                userInfo.name = user.kakaoAccount?.profile?.nickname.toString()
                userInfo.email = user.kakaoAccount?.email.toString()

                Log.d("NOTELIST TEST : ", "name : " + userInfo.name + ", email" + userInfo.email)
                initNoteList(userInfo) //노트 얻어오기
                initQuizList(userInfo) //퀴즈 얻어오기
            }
        }

    }

    private fun addNoteList(note : NoteItem){

        // 중복 체크: 이미 리스트에 같은 ID의 노트가 있는지 확인
        val isDuplicate = noteList.any { it.id == note.id }

        if (!isDuplicate) {
            if (noteList.size < 10) {
                // 10개 미만일 때만 요소 추가
                noteList.add(0, note)
            }

            // RecyclerView 어댑터를 업데이트
            noteRecyclerViewAdapter.notifyDataSetChanged()
        }
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
            quizRecyclerViewAdapter.notifyDataSetChanged()
        }
    }

}

