package com.example.sumnote.ui.Quiz

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sumnote.R

//리사이클러뷰 어댑터 작성 => 재활용을 위해
class QuizRecyclerViewAdapter(
    val itemList : ArrayList<QuizListItem>, //리사이클러뷰로 그려줄 퀴즈들
    val inflater : LayoutInflater, //화면에 붙이기 위한 inflater
    val onItemClickListener: OnItemClickListener // 클릭 리스너 => 사용자가 아이템 클릭시 화면 이동
): RecyclerView.Adapter<QuizRecyclerViewAdapter.ViewHoler>(){ //리사이클러뷰 어댑터 상속받기 템플릿은 자기 자신

    interface OnItemClickListener {
        fun onQuizItemClick(position: Int)
    }


    //생성자를 통해 받은 뷰를 부모 클래스로 넘겨주기
    //2. onCreateViewHolder에서 만든 뷰를 생성자로 전달받음
    //해당 위치에서 각 아이템에 대한 이벤트를 달 수 있음
    //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
    inner class ViewHoler(itemView: View): RecyclerView.ViewHolder(itemView){
        //퀴즈 이미지
        val quizImage : ImageView
        //노트 값들
        val quizTitle : TextView
        val genData : TextView
        //어댑터가 만들어지면 각 뷰의 값 초기화
        //3. init블럭 호출 => title과 generatedDate 텍스트 뷰가 세팅됨
        init {
            quizImage = itemView.findViewById(R.id.imgView_quiz)
            //생성할 퀴즈 값들
            quizTitle = itemView.findViewById(R.id.txt_quiz_title)
            genData = itemView.findViewById(R.id.txt_quiz_gen_data)

            //각 아이템 클릭에 대한 이벤트 달기
            itemView.setOnClickListener{
                val position: Int = absoluteAdapterPosition //아이템 위치 가져오기
                //문제집 번호와 함께 fragment_quiz_viewer로 이동
                //-> 이동 시점에서 문제집 번호를 서버로 보내어, 원하는 문제집 요청
                //-> 서버로부터 받아온 문제집을 뷰 페이저를 통해 화면에 보여줌
                //findNavController().navigate(R.id.action_navigation_my_note_to_allNoteFragment)
                Log.d("itemClickedTest",position.toString()+"is Clicked before Listner")
                onItemClickListener.onQuizItemClick(position) //클릭 리스너로 현재 위치(아이템 아이디)를 보냄
            }
        }

    }

    //각 아이템을 그려줌

    //1. 호출되고 나면 아이템 하나가 들어갈 뷰를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        //각 뷰를 생성하는 부분
        //아이템에 해당하는 인플레이터 정의
        val view = inflater.inflate(R.layout.quiz_list_item,parent,false)
        return ViewHoler(view) //뷰 홀더에 위에서 만든 뷰 넣어주기 => class ViewHolder의 생성자가 호출됨?
    }

    //리사이클러뷰에서 보여줄 아이템리스트의 사이즈
    override fun getItemCount(): Int {
        return itemList.size
    }

    //뷰를 그려주는 부분(바인딩 해준다.)
    // 4. 세팅해둔 텍스트뷰에 값 채워줌
    override fun onBindViewHolder(holder: ViewHoler, position: Int) {
        //홀더(위에서 생성한 홀더)에 값 할당
        holder.quizTitle.text = itemList[position].title
        holder.genData.text = itemList[position].created_at

        //이미지는 position에 해당하는 값으로
        // 이미지의 리소스 ID 얻어오기
        val imageNumber = (position % 9) + 1 //모듈러연산 => img_quiz 개수를 벗어나지 않도록
        val imageName = "img_quiz_$imageNumber"
        val resId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)
        holder.quizImage.setImageResource(resId)
    }
}