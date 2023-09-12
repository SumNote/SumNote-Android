package com.example.sumnote.ui.Note


//리사이클러뷰에서 사용할 노트 클래스
//노트아이템 => my note에서 보여주기 위한 초기 형태
//데이터 저장이 목적인 클래스는 data키워드를 붙여서 생성한다.
//먼저 노트의 개수만큼 목록들을 보여주고, 사용자가 노트를 클릭했을때 서버또는 Room으로부터 해당 노트에 대한 정보 요청
//노트아이템(아이디=>클릭시 해당 노트에 대한 정보 요청,노트 제목"데이터베이스",노트 생성날짜
data class NoteItem constructor(var id:Int, var title:String, var created_at:String)