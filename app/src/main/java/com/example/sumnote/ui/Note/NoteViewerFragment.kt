package com.example.sumnote.ui.Note

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import com.example.sumnote.R
import com.example.sumnote.api.ApiManager
import com.example.sumnote.databinding.FragmentNoteViewerBinding
import com.example.sumnote.ui.DTO.CreateQuizRequest
import com.example.sumnote.ui.DTO.UpdateQuizRequest
import com.example.sumnote.ui.Dialog.ChangeNoteTitleDialog
import com.example.sumnote.ui.Dialog.CircleProgressDialog
import com.example.sumnote.ui.Dialog.FailDialog
import com.example.sumnote.ui.Dialog.SuccessDialog
import com.example.sumnote.ui.kakaoLogin.KakaoOauthViewModelFactory
import com.example.sumnote.ui.kakaoLogin.KakaoViewModel
import com.example.sumnote.ui.kakaoLogin.RetrofitBuilder
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class NoteViewerFragment : Fragment() {

    private var _binding: FragmentNoteViewerBinding? = null
    private val binding get() = _binding!!
    private lateinit var pages : MutableList<Page>
    private lateinit var noteViewAdapter : NotePagerAdapter
    private var clickedNoteId : Int = -1

    lateinit var apiManager: ApiManager
    private val baseUrl = "http://10.0.2.2:8000/"
//    private val baseUrl = "http://43.201.71.53:80/"

    private lateinit var kakaoViewModel: KakaoViewModel

    private val loadingDialog = CircleProgressDialog()
    private val successDialog = SuccessDialog()
    private val failDialog = FailDialog()

    // 장고에 보내줄 텍스트
    private lateinit var toQuiz : String
    // 해당 노트에 대해서 퀴즈가 존재하는지
    private var quizExist by Delegates.notNull<Boolean>()

    lateinit var pageTitle : String

    // NoteViewerFragment 클래스 내에 멤버 변수 추가
    private var currentPageIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            pageTitle = it.getString("notetitle").toString()

            Log.d("noteClicked #2","$pageTitle")
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteViewerBinding.inflate(inflater, container, false)
        kakaoViewModel = ViewModelProvider(this, KakaoOauthViewModelFactory(requireActivity().application))[KakaoViewModel::class.java]

        //번들에서 값 얻어오기 => 클릭한 노트 알아내기
        arguments?.let {

            clickedNoteId = it.getInt("noteId") // 선택한 노트가 없을 경우 -1
            var clickedNoteTitle = it.getString("sum_doc_title") // 선택한 노트가 없을 경우 -1
            var noteTitle = binding.txtNoteViewrTitle
            noteTitle.text = clickedNoteTitle

            Log.d("#NOTE ID", "note ID : $clickedNoteId")
            if (clickedNoteId != -1) {
                //클릭한 노트에 대한 페이지 정보를 서버에 요청
                detailNote(clickedNoteId)
            }
        }

        // ViewPager2에 페이지 변경 리스너를 추가합니다.
        binding.noteViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 페이지가 변경될 때 현재 페이지의 인덱스를 업데이트합니다.
                currentPageIndex = position
            }
        })

        //페이지들을 보여주기 위한 뷰 페이저
        var noteViewPager = binding.noteViewPager
        pages = mutableListOf()


        //테스트용 더미 데이터 생성 => 여기서 서버로부터 정보 받아와 파싱하는 코드 작성 필요

//        notes = listOf(
//            Note(
//                noteTitle = "데이터베이스와 기본 용어",
//                summary = "데이터베이스 : 구조화된 데이터의 집합체\n\n\n" +
//                        "스키마 : 데이터베이스의 구조와 설계\n\n\n" +
//                        "테이블 : 데이터의 행과 열 구조\n\n\n" +
//                        "레코드 : 테이블 내의 하나의 행\n\n\n" +
//                        "필드 : 데이터의 속성 또는 열\n\n\n" +
//                        "기본 키 : 레코드를 고유하게 식별하는 키\n\n\n" +
//                        "외래 키 : 다른 테이블의 기본 키를 참조하는 키\n\n\n"
//            ),
//            Note(
//                noteTitle = "질의어와 SQL",
//                summary = "질의어 : 데이터베이스 정보 요구를 위한 언어\n\n\n" +
//                        "SQL : 표준 데이터베이스 질의 언어\n\n\n" +
//                        "SELECT : 데이터를 조회하는 명령\n\n\n" +
//                        "INSERT : 데이터를 추가하는 명령\n\n\n" +
//                        "WHERE : 데이터 검색 조건을 설정하는 절\n\n\n" +
//                        "UPDATE : 데이터를 수정하는 명령\n\n\n" +
//                        "DELETE : 데이터를 삭제하는 명령\n\n\n"
//            ),
//            Note(
//                noteTitle = "데이터베이스 설계",
//                summary = "정규화 : 데이터 중복을 최소화하는 설계 방법\n\n\n" +
//                        "엔터티 : 정보의 기본 단위, 테이블로 표현\n\n\n" +
//                        "관계 : 테이블 간의 연결\n\n\n" +
//                        "인덱스 : 데이터 검색 속도 향상을 위한 구조\n\n\n" +
//                        "일대일, 일대다, 다대다 : 엔터티 간 관계 유형\n\n\n" +
//                        "뷰 : 하나 이상의 테이블로부터 파생된 가상 테이블\n\n\n" +
//                        "무결성 : 데이터의 정확성과 일관성을 유지하는 규칙\n\n"
//            ),
//            Note(
//                noteTitle = "트랜잭션과 보안",
//                summary = "트랜잭션 : 여러 연산의 묶음, 모두 성공 또는 실패\n\n\n" +
//                        "커밋 : 트랜잭션이 성공적으로 완료된 경우\n\n\n" +
//                        "롤백 : 오류로 인해 트랜잭션을 취소하고 복구하는 연산\n\n\n" +
//                        "인증 : 사용자 신원 확인 과정\n\n\n" +
//                        "인가 : 인증된 사용자에게 접근 권한 부여\n\n\n" +
//                        "암호화 : 데이터를 보호하기 위해 변환하는 과정\n\n\n" +
//                        "로그 : 데이터베이스 활동의 기록\n\n"
//            )
//
//        )

        //뷰 페이저에 붙일 어댑터 생성
        noteViewAdapter = NotePagerAdapter(this, pages)
        noteViewPager.adapter = noteViewAdapter //어댑터 붙이기


        //뒤로가기 버튼
        val btmBack = binding.imgBtnBack
        btmBack.setOnClickListener{
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
                            toQuiz =  "[${pageTitle}]\n${pageSummary}"
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
                                    successDialog.show(requireActivity().supportFragmentManager, successDialog.tag)
                                    withContext(Dispatchers.Default) { delay(1500) }
                                    successDialog.dismiss()
                                }

                            }
                        })

                        changeNoteTitleDialog.show(requireActivity().supportFragmentManager, changeNoteTitleDialog.tag)
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
                            successDialog.show(requireActivity().supportFragmentManager, successDialog.tag)

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

    private fun deleteNote(){
        val call = RetrofitBuilder.api.deleteNote(clickedNoteId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("#SPRING SERVER:", "DELETE SUCCESS")

                } else {
                    // 통신 성공 but 응답 실패

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("#SPRING SERVER:", "CONNECTION FAILURE")
            }
        })
    }

    //서버로부터 클릭한 노트에 대한 페이지 요청
    private fun detailNote(clickedNoteId : Int){

        val call = RetrofitBuilder.api.detailNote(clickedNoteId)
        call.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        val json = JSONObject(jsonString)

                        val docTitle = json.getString("sum_doc_title") //클릭한 노트의 제목

                        //노트에 대한 페이지들의 정보 => 파싱 필요 title : [][] .. content : [][]
                        val title = json.getString("title")
                        val content = json.getString("content")

                        quizExist = json.getBoolean("is_quiz_exist")

                        Log.d("#DETAIL Success:", jsonString)
                        Log.d("#DETAIL title:", title)
                        Log.d("#DETAIL content:", content)

                        //수정된 정규식 패턴
                        val pattern = Regex("\\[([\\s\\S]*?)\\]")

                        // title 및 content에서 []로 둘러싸인 값을 파싱
                        val parsedTitles = pattern.findAll(title).map { it.groupValues[1] }.toList()
                        Log.d("#DETAIL TITLE", "test : " + parsedTitles)
                        val parsedContents = pattern.findAll(content).map { it.groupValues[1] }.toList()
                        

                        Log.d("#createdPage : ","$parsedTitles, $parsedContents")

                        // 파싱된 값들의 길이가 동일한지 확인하고, 동일하면 Page 객체를 생성하여 pages에 추가
                        // 제목으로 3개를 가져왔는데, 내용으로 2개만 가져오는 경우에 대한 예외처리
                        if (parsedTitles.size == parsedContents.size) {
                            for (i in parsedTitles.indices) {
                                var pTitle = parsedTitles[i]
                                var pSummary = parsedContents[i]
                                val page = Page(
                                    pageTitle = parsedTitles[i],
                                    summary = parsedContents[i]
                                )
                                Log.d("#createdPage : ","페이지 제목 : $pTitle 요약정보 $pSummary ")
                                pages.add(page)
                            }
                            noteViewAdapter.notifyDataSetChanged()
                        } else {
                            // 타이틀과 컨텐츠의 길이가 다르면 로그를 남깁니다.
                            Log.e("ParsingError", "Title and content size mismatch!")
                        }

                        toQuiz = "[${parsedTitles[0]}]\n${parsedContents}"

                        //기존 코드
//                        //노트에 대한 페이지들 파싱
//                        var page = Page(
//                            pageTitle = title,
//                            summary = content
//                        )
//
//                        //
//                        pages.add(page)
//                        noteViewAdapter.notifyDataSetChanged()

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

    private fun serverToGetPro(){

        Log.d("GetPro","#1")
        // timeout setting 해주기
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiManager = retrofit.create(ApiManager::class.java)

        // 요약된 노트 내용을 바탕으로 문제 생성


        // 다이얼로그 표시

        val bundle = Bundle()
        bundle.putString("dialogText", "문제를 생성하는 중입니다...")
        Log.d("GetPro","#2")
        loadingDialog.arguments = bundle
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        Log.d("GetPro","#1")
        Log.d("TO QUIZ TEST", "${toQuiz}")
        val call = apiManager.generateProblem(toQuiz)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("DjangoServer","before response")
                if (response.isSuccessful) {
                    // 서버로 이미지 전송 성공시
                    Log.d("DjangoServer","send success")

                    // 서버 응답에 대한 추가 처리 코드 작성
                    val responseBody = response.body()

                    if (responseBody != null) {
                        // JSON 응답 파싱
                        val jsonString = responseBody.string()
                        val json = JSONObject(jsonString)

                        //String 값 받아오기  => 책의 모든 문자열(추후 ocr코드 개발되면 개선)
                        val questions = json.getString("question")
                        val selections = json.getString("selections")
                        val answers = json.getString("answer")
                        val commentary = json.getString("commentary")

                        Log.d("DjangoServer", "question's Text : $questions")
                        Log.d("DjangoServer", "answer_list's Text : $selections")
                        Log.d("DjangoServer", "answer_num's Text : $answers")
                        Log.d("DjangoServer", "commentary's Text : $commentary")

                        // 사용자 정보 요청 (기본)
                        UserApiClient.instance.me { user, error ->
                            if (error != null) {
                                Log.e(KakaoViewModel.TAG, "사용자 정보 요청 실패", error)
                            } else if (user != null) {
                                var email = user.kakaoAccount?.email.toString()
                                val quiz = CreateQuizRequest(email, clickedNoteId, "$questions", "$selections", "$answers", "$commentary")

                                val appendQuiz = UpdateQuizRequest( "$questions", "$selections", "$answers", "$commentary")

                                // 성공 dialog 띄우기
                                CoroutineScope(Dispatchers.Main).launch {
                                    loadingDialog.dismiss()
                                    val bundle = Bundle()
                                    bundle.putString("dialogText", "문제가 성공적으로 생성되었습니다!")
                                    successDialog.arguments = bundle
                                    successDialog.show(requireActivity().supportFragmentManager, successDialog.tag)
                                    withContext(Dispatchers.Default) { delay(1500) }
                                    successDialog.dismiss()
                                    makeQuiz(quiz, appendQuiz)
                                }

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
                Log.e("ImageUpload", "Image upload error: ${t.message}")
                loadingDialog.dismiss()
                showFailDialog()
            }
        })
        //val call = apiManager.uploadImage(imagePart)
    }

    private fun makeQuiz(request : CreateQuizRequest, request2: UpdateQuizRequest){


        if(!quizExist) {
            val call = RetrofitBuilder.api.createQuiz(request)
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
                        Log.d("#MAKE_QUIZ:", "FAILURE")
                        showFailDialog()
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // 통신에 실패한 경우
                    Log.d("#MAKE_QUIZ FAIL: ", t.localizedMessage)
                    showFailDialog()
                }
            })

        } else {
            val call = RetrofitBuilder.api.updateQuiz(clickedNoteId, request2)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("#UPDATE_QUIZ: ", "SUCCESS")

                        findNavController().popBackStack()
                    } else {
                        // 통신 성공 but 응답 실패
                        Log.d("#UPDATE_QUIZ:", "FAILURE")
                        showFailDialog()
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // 통신에 실패한 경우
                    Log.d("#UPDATE_QUIZ FAIL: ", t.localizedMessage)
                    showFailDialog()
                }
            })
        }
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