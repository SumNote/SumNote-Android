package com.example.sumnote.ui.Note

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentNoteViewerBinding
import kotlin.properties.Delegates

class NoteViewerFragment : Fragment() {

    private var _binding: FragmentNoteViewerBinding? = null
    private val binding get() = _binding!!

    var position by Delegates.notNull<Int>()
    lateinit var noteTitle : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt("position")
            noteTitle = it.getString("notetitle").toString()
            Log.d("noteClicked #2","$position")
            Log.d("noteClicked #2","$noteTitle")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteViewerBinding.inflate(inflater, container, false)


        //노트들을 보여주기 위한 뷰 페이저
        var noteViewPager = binding.noteViewPager

        var noteTitle = binding.txtNoteViewrTitle
        noteTitle.text = "Note "+position.toString()

        //서버 요청 받고 파싱하면됨







        //번들로부터 값 얻어오기


        //테스트용 더미 데이터 생성 => 여기서 서버로부터 정보 받아와 파싱하는 코드 작성 필요
        val notes = listOf(
            Note(
                noteTitle = "데이터베이스와 기본 용어",
                summary = "데이터베이스 : 구조화된 데이터의 집합체\n\n\n" +
                        "스키마 : 데이터베이스의 구조와 설계\n\n\n" +
                        "테이블 : 데이터의 행과 열 구조\n\n\n" +
                        "레코드 : 테이블 내의 하나의 행\n\n\n" +
                        "필드 : 데이터의 속성 또는 열\n\n\n" +
                        "기본 키 : 레코드를 고유하게 식별하는 키\n\n\n" +
                        "외래 키 : 다른 테이블의 기본 키를 참조하는 키\n\n\n"
            ),
            Note(
                noteTitle = "질의어와 SQL",
                summary = "질의어 : 데이터베이스 정보 요구를 위한 언어\n\n\n" +
                        "SQL : 표준 데이터베이스 질의 언어\n\n\n" +
                        "SELECT : 데이터를 조회하는 명령\n\n\n" +
                        "INSERT : 데이터를 추가하는 명령\n\n\n" +
                        "WHERE : 데이터 검색 조건을 설정하는 절\n\n\n" +
                        "UPDATE : 데이터를 수정하는 명령\n\n\n" +
                        "DELETE : 데이터를 삭제하는 명령\n\n\n"
            ),
            Note(
                noteTitle = "데이터베이스 설계",
                summary = "정규화 : 데이터 중복을 최소화하는 설계 방법\n\n\n" +
                        "엔터티 : 정보의 기본 단위, 테이블로 표현\n\n\n" +
                        "관계 : 테이블 간의 연결\n\n\n" +
                        "인덱스 : 데이터 검색 속도 향상을 위한 구조\n\n\n" +
                        "일대일, 일대다, 다대다 : 엔터티 간 관계 유형\n\n\n" +
                        "뷰 : 하나 이상의 테이블로부터 파생된 가상 테이블\n\n\n" +
                        "무결성 : 데이터의 정확성과 일관성을 유지하는 규칙\n\n"
            ),
            Note(
                noteTitle = "트랜잭션과 보안",
                summary = "트랜잭션 : 여러 연산의 묶음, 모두 성공 또는 실패\n\n\n" +
                        "커밋 : 트랜잭션이 성공적으로 완료된 경우\n\n\n" +
                        "롤백 : 오류로 인해 트랜잭션을 취소하고 복구하는 연산\n\n\n" +
                        "인증 : 사용자 신원 확인 과정\n\n\n" +
                        "인가 : 인증된 사용자에게 접근 권한 부여\n\n\n" +
                        "암호화 : 데이터를 보호하기 위해 변환하는 과정\n\n\n" +
                        "로그 : 데이터베이스 활동의 기록\n\n"
            )

        )

        //뷰 페이저에 붙일 어댑터 생성
        val noteViewAdapter = NotePagerAdapter(this, notes)
        noteViewPager.adapter = noteViewAdapter //어댑터 붙이기


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

// NotePagerAdapter 클래스
class NotePagerAdapter(
    fragmentActivity: NoteViewerFragment,
    private val notes: List<Note>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun createFragment(position: Int): Fragment {
        return NoteFragment.newInstance(notes[position])
    }
}