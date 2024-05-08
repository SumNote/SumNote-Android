package com.example.sumnote.ui.Note

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sumnote.MainActivity
import com.example.sumnote.R
import com.example.sumnote.api.RetrofitBuilderFastApi
import com.example.sumnote.databinding.FragmentNoteViewerBinding
import com.example.sumnote.ui.DTO.CreateQuizRequest
import com.example.sumnote.ui.DTO.Response.ResQuizDetail
import com.example.sumnote.ui.DTO.Response.ResponseNoteDetail
import com.example.sumnote.ui.Dialog.ChangeNoteTitleDialog
import com.example.sumnote.ui.Dialog.CircleProgressDialog
import com.example.sumnote.ui.Dialog.FailDialog
import com.example.sumnote.ui.Dialog.SuccessDialog
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import kotlin.properties.Delegates


class NoteViewerFragment : Fragment() {

    private var _binding: FragmentNoteViewerBinding? = null
    private val binding get() = _binding!!
    private lateinit var pages: MutableList<Page>
    private lateinit var noteViewAdapter: NotePagerAdapter
    private var clickedNoteId: Int = -1

    private lateinit var kakaoViewModel: KakaoViewModel

    private val loadingDialog = CircleProgressDialog()
    private val successDialog = SuccessDialog()
    private val failDialog = FailDialog()

    // 장고에 보내줄 텍스트
    private lateinit var toQuiz: String

    lateinit var pageTitle: String

    // NoteViewerFragment 클래스 내에 멤버 변수 추가
    private var currentPageIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            pageTitle = it.getString("notetitle").toString()

            Log.d("noteClicked #2", "$pageTitle")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteViewerBinding.inflate(inflater, container, false)
        kakaoViewModel = ViewModelProvider(
            this,
            KakaoOauthViewModelFactory(requireActivity().application)
        )[KakaoViewModel::class.java]

        //번들에서 값 얻어오기 => 클릭한 노트 알아내기
        arguments?.let {

            clickedNoteId = it.getInt("noteId") // 선택한 노트가 없을 경우 -1
            var clickedNoteTitle = it.getString("sum_doc_title") // 선택한 노트가 없을 경우 -1
            var noteTitle = binding.txtNoteViewrTitle
            noteTitle.text = clickedNoteTitle
            pageTitle = clickedNoteTitle!!

            Log.d("#NOTE ID", "note ID : $clickedNoteId")
            if (clickedNoteId != -1) {
                //클릭한 노트에 대한 페이지 정보를 서버에 요청
                detailNote(clickedNoteId)
            }
        }

        // ViewPager2에 페이지 변경 리스너를 추가합니다.
        binding.noteViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 페이지가 변경될 때 현재 페이지의 인덱스를 업데이트합니다.
                currentPageIndex = position
            }
        })

        //페이지들을 보여주기 위한 뷰 페이저
        var noteViewPager = binding.noteViewPager
        pages = mutableListOf()

        //뷰 페이저에 붙일 어댑터 생성
        noteViewAdapter = NotePagerAdapter(this, pages)
        noteViewPager.adapter = noteViewAdapter //어댑터 붙이기


        //뒤로가기 버튼
        val btmBack = binding.imgBtnBack
        btmBack.setOnClickListener {
            findNavController().navigateUp()
        }


        val menuButton = binding.menuVertical
        menuButton.setOnClickListener {
            // 팝업 메뉴를 생성하고 표시
            val popupMenu = PopupMenu(requireContext(), menuButton)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.note_viewer_menu, popupMenu.menu)

            // 팝업 메뉴 아이템에 클릭 리스너를 추가
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // 퀴즈 생성
                    R.id.create_quiz -> {
                        // 메뉴 아이템 1을 클릭했을 때 수행할 동작을 정의
                        if (currentPageIndex < pages.size) {
                            val currentPage = pages[currentPageIndex]
                            // currentPage에는 현재 페이지의 정보가 포함
                            // 이 정보를 사용하여 페이지 내용 가져오기
                            val pageTitle = currentPage.pageTitle
                            val pageSummary = currentPage.summary
                            Log.d("GET CURR PAGE", "title : ${pageTitle} sum : ${pageSummary}")
                            toQuiz = "[${pageTitle}]\n${pageSummary}"
                        }
                        serverToGetPro()
                        true
                    }

                    // 제목 수정
                    R.id.update_title -> {
                        val changeNoteTitleDialog = ChangeNoteTitleDialog(clickedNoteId)

                        // 다이얼로그가 생성될 때 리스너 설정
                        changeNoteTitleDialog.setOnDialogResultListener(object :
                            ChangeNoteTitleDialog.OnDialogResultListener {
                            override fun onDialogResult(result: Any) {
                                // 다이얼로그에서 작업 완료 후 이 메서드가 호출됩니다.
                                // 여기서 프래그먼트의 내용을 갱신할 수 있습니다.
                                Log.d("dialog result", "result : $result")
                                binding.txtNoteViewrTitle.text = result.toString()


                                val bundle = Bundle()
                                bundle.putString("dialogText", "성공적으로 변경되었습니다!")
                                successDialog.arguments = bundle

                                CoroutineScope(Dispatchers.Main).launch {
                                    successDialog.show(
                                        requireActivity().supportFragmentManager,
                                        successDialog.tag
                                    )
                                    withContext(Dispatchers.Default) { delay(1500) }
                                    successDialog.dismiss()
                                }

                            }
                        })

                        changeNoteTitleDialog.show(
                            requireActivity().supportFragmentManager,
                            changeNoteTitleDialog.tag
                        )
                        Log.d("#MENU", "update title : ${clickedNoteId}")

                        true
                    }

                    // 노트 삭제
                    R.id.delete_note -> {
                        deleteNote()
                        Log.d("#MENU", "DELETE ${clickedNoteId}")
                        // 성공 dialog 띄우기
                        CoroutineScope(Dispatchers.Main).launch {
                            val dialogBundle = Bundle()
                            dialogBundle.putString("dialogText", "노트가 삭제되었습니다!")
                            successDialog.arguments = dialogBundle
                            successDialog.show(
                                requireActivity().supportFragmentManager,
                                successDialog.tag
                            )

                            // 지정된 딜레이 이후에 UI 조작 수행
                            delay(1500)

                            // UI 조작은 메인 스레드에서 실행
                            withContext(Dispatchers.Main) {
                                successDialog.dismiss()

                            }
                        }
                        findNavController().popBackStack()


                        true
                    }

                    else -> false
                }
            }

            // 팝업 메뉴를 표시합니다.
            popupMenu.show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun deleteNote() {

        val token = MainActivity.prefs.getString("token", "")
        val call = RetrofitBuilder.api.deleteNote(token, clickedNoteId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("#NoteViewerFragment :", "DELETE SUCCESS")

                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("#NoteViewerFragment :", "DELETE SUCCESS But Res")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("#SPRING SERVER:", "CONNECTION FAILURE")
            }
        })
    }

    // 노트 조회 Response 용
    data class NoteDetailResult(
        @SerializedName("data") val noteList: ResponseNoteDetail
    )

    //서버로부터 클릭한 노트에 대한 페이지 요청
    private fun detailNote(clickedNoteId: Int) {

        val token = MainActivity.prefs.getString("token", "")
        val call = RetrofitBuilder.api.detailNote(token, clickedNoteId)
        call.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()

                        val gson = Gson()
                        val result = gson.fromJson(jsonString, NoteDetailResult::class.java)

                        // 'noteList'에 포함된 노트 목록에 접근합니다.
                        val notes = result.noteList

                        val docTitle = notes.note.title //클릭한 노트의 제목

                        // 노트들 각각 페이지에 넣어줌

                        val noteDetails = notes.notePages
                        var sb = StringBuilder()

                        for (n in noteDetails) {
                            val page = Page(
                                pageTitle = n.title,
                                summary = n.content
                            )

                            sb.append(n.content).append("\n")

                            Log.d(
                                "#NoteViewerFragment createdPage : ",
                                "페이지 제목 : ${n.title} 요약정보 ${n.content}"
                            )
                            pages.add(page)
                        }
                        noteViewAdapter.notifyDataSetChanged()

                        // 퀴즈를 생성할 때 보내줄 contents
                        toQuiz = "[${docTitle}]\n${sb}"


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

    data class QuizDetailResult(
        @SerializedName("data") val quizList: List<ResQuizDetail>
    )
    private fun serverToGetPro() {

        Log.d("GetPro", "#1")

        // 요약된 노트 내용을 바탕으로 문제 생성
        // 다이얼로그 표시

        val bundle = Bundle()
        bundle.putString("dialogText", "문제를 생성하는 중입니다...")
        Log.d("GetPro", "#2")
        loadingDialog.arguments = bundle
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        Log.d("GetPro", "#1")
        Log.d("TO QUIZ TEST", "${toQuiz}")

        // API 호출
        val call = RetrofitBuilderFastApi.api.generateProblem(toQuiz)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.isSuccessful) {
                    // 서버로 이미지 전송 성공시
                    Log.d("#NoteViewerFragment", "send success")

                    // 서버 응답에 대한 추가 처리 코드 작성
                    val responseBody = response.body()

                    if (responseBody != null) {
                        // JSON 응답 파싱
                        val jsonString = responseBody.string()

                        val gson = Gson()
                        val result = gson.fromJson(jsonString, QuizDetailResult::class.java)

                        val quizList = result.quizList


                        val createQuizRequests = mutableListOf<ResQuizDetail>()
                        if (quizList != null) {
                            for (quiz in quizList) {
                                Log.d(
                                    "FastApi get Quiz",
                                    quiz.question + quiz.answer + quiz.commentary
                                )
                                val selections = mutableListOf<String>()
                                for (s in quiz.selection) {
                                    selections.add(s)
                                    Log.d("FastApi get Quiz", s)
                                }

                                val quizDetail = ResQuizDetail(
                                    question = quiz.question,
                                    selection = selections,
                                    answer = quiz.answer,
                                    commentary = quiz.commentary
                                )
                                createQuizRequests.add(quizDetail)
                            }

                            val createQuiz = CreateQuizRequest(
                                clickedNoteId.toLong(),
                                pageTitle,
                                createQuizRequests
                            )

                            // 성공 dialog 띄우기
                            CoroutineScope(Dispatchers.Main).launch {
                                loadingDialog.dismiss()
                                val bundle = Bundle()
                                bundle.putString("dialogText", "문제가 성공적으로 생성되었습니다!")
                                successDialog.arguments = bundle
                                successDialog.show(
                                    requireActivity().supportFragmentManager,
                                    successDialog.tag
                                )
                                withContext(Dispatchers.Default) { delay(1500) }
                                successDialog.dismiss()
                                makeQuiz(createQuiz)
                            }
                        }
                    } else {
                        Log.e("DjangoServer", "Error parsing JSON")
                        loadingDialog.dismiss()
                        showFailDialog()
                    }
                } else {
                    Log.e("DjangoServer", "Error response")
                    loadingDialog.dismiss()
                    showFailDialog()
                }


            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                Log.e("#NoteViewerFragment", "Image upload error: ${t.message}")
                loadingDialog.dismiss()
                showFailDialog()
            }
        })

    }

    private fun makeQuiz(request: CreateQuizRequest) {

        Log.d("Spring makeQuiz" , "noteId : " + request.noteId)
        Log.d("Spring makeQuiz" , "title : " + request.title)

        for (quiz in request.quiz) {
            Log.d("Spring makeQuiz" , "question : " + quiz.question)
            Log.d("Spring makeQuiz" , "commentary : " + quiz.commentary)
            Log.d("Spring makeQuiz" , "answer : " + quiz.answer)

            for (s in quiz.selection) {
                Log.d("Spring makeQuiz" , "selection : $s")
            }
        }

        val token = MainActivity.prefs.getString("token", "")
        val call = RetrofitBuilder.api.createQuiz(token, request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Log.d("#MAKE_QUIZ: ", "SUCCESS")

//                    findNavController().navigate(R.id.action_newNoteFragment_to_navigation_my_note)
                    findNavController().popBackStack()
                } else {
                    // 통신 성공 but 응답 실패
                    Log.d("#MAKE_QUIZ:", "FAILURE :" + response.code())
                    showFailDialog()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신에 실패한 경우
                Log.d("#MAKE_QUIZ FAIL: ", t.localizedMessage)
                showFailDialog()
            }
        })
    }

    private fun showFailDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            val dialogBundle = Bundle()
            dialogBundle.putString("dialogText", "문제 생성에 실패하였습니다.")
            failDialog.arguments = dialogBundle
            failDialog.show(requireActivity().supportFragmentManager, failDialog.tag)

            // 지정된 딜레이 이후에 UI 조작 수행
            delay(1500)

            // UI 조작은 메인 스레드에서 실행되어야 합니다.
            withContext(Dispatchers.Main) {
                failDialog.dismiss()

            }
        }
    }
}

// NotePagerAdapter 클래스
class NotePagerAdapter(
    fragmentActivity: NoteViewerFragment,
    private val notes: List<Page>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun createFragment(position: Int): Fragment {
        return NoteFragment.newInstance(notes[position])
    }
}