package com.example.sumnote.ui.Note

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentAllNoteBinding
import com.example.sumnote.ui.DTO.User
import com.example.sumnote.ui.MyNote.MyNoteFragment
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.google.gson.Gson
import com.kakao.sdk.user.UserApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime


class AllNoteFragment : Fragment() {

    private var _binding: FragmentAllNoteBinding? = null
    private val binding get() = _binding!!

    private var noteList = ArrayList<NoteItem>()
    private lateinit var allNoteRecyclerViewAdapter: AllNoteRecyclerViewAdapter
    private lateinit var kakaoViewModel: KakaoViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllNoteBinding.inflate(inflater, container, false)
        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(requireActivity().application))[KakaoViewModel::class.java]

        //사용자 정보 얻어오기
        getUser()

        //모든 노트 보기 리사이클러뷰 적용
        allNoteRecyclerViewAdapter = AllNoteRecyclerViewAdapter(noteList, LayoutInflater.from(this.context),
            object : AllNoteRecyclerViewAdapter.OnItemClickListener{
                override fun onAllNoteItemClick(position: Int){
                    //position을 같이 넣어야 함을 잊지말것
                    // 클릭한 노트 아이디 가져오기
                    val clickedNoteId = noteList[position].id
                    val noteTitle = noteList[position].sum_doc_title

                    // 번들을 생성하고 클릭한 노트 아이디를 추가
                    val bundle = Bundle()
                    bundle.putInt("noteId", clickedNoteId)
                    bundle.putString("sum_doc_title", noteTitle)
                    Log.d("NOTE CLICKED", "test : $clickedNoteId")

                    // 노트 아이템 클릭시 동작

                    findNavController().navigate(R.id.action_allNoteFragment_to_noteViewerFragment, bundle)
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
                        val result = gson.fromJson(jsonString, MyNoteFragment.Result::class.java)

                        // 'noteList'에 포함된 노트 목록에 접근합니다.
                        val noteList = result.noteList
                        for (note in noteList) {
                            println("ID: ${note.id}")
                            println("Title: ${note.sum_doc_title}")
//                            println("Content: ${note.generatedDate}")
                            println("Created At: ${note.created_at}")
                            Log.d("GET NOTELIST" , "ID : ${note.id} title : ${note.sum_doc_title} created_at : ${note.created_at}")

                            val myNote = NoteItem(note.id, note.sum_doc_title,note.created_at)
                            addNoteList(myNote)
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
                initNoteList(userInfo)
            }
        }

    }

    private fun addNoteList(note : NoteItem){

        // 중복 체크: 이미 리스트에 같은 ID의 노트가 있는지 확인
        val isDuplicate = noteList.any { it.id == note.id }

        if (!isDuplicate) {
            noteList.add(0, note)

            // RecyclerView 어댑터를 업데이트
            allNoteRecyclerViewAdapter.notifyDataSetChanged()
        }
    }


}