package com.example.sumnote.ui.Note

import android.icu.text.CaseMap.Title
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sumnote.R

//리사이클러뷰 어댑터 작성 => 재활용을 위해
class NoteRecyclerViewAdapter(
    val itemList : ArrayList<NoteItem>, //리사이클러뷰로 그려줄 노트들
    val inflater : LayoutInflater //화면에 붙이기 위한 inflater
): RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHoler>(){ //리사이클러뷰 어댑터 상속받기 템플릿은 자기 자신

    //생성자를 통해 받은 뷰를 부모 클래스로 넘겨주기
    //2. onCreateViewHolder에서 만든 뷰를 생성자로 전달받음
    //해당 위치에서 각 아이템에 대한 이벤트를 달 수 있음
    //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
    inner class ViewHoler(itemView: View): RecyclerView.ViewHolder(itemView){
        val id : TextView
        val title : TextView
        val generatedDate : TextView
        //어댑터가 만들어지면 각 뷰의 값 초기화
        //3. init블럭 호출 => carName과 carEngine 텍스트 뷰가 세팅됨
        init {
//             = itemView.findViewById(R.id.car_name)
//            carEngine = itemView.findViewById(R.id.car_engine)

            //각 아이템 클릭에 대한 이벤트 달기
            itemView.setOnClickListener{
                val position: Int = absoluteAdapterPosition //아이템 위치 가져오기
                val engineName = itemList.get(position).engine
                val carName = itemList.get(position).name
                Log.d("engine",engineName)

            }
        }
    }

    //각 아이템을 그려줌

    //1. 호출되고 나면 아이템 하나가 들어갈 뷰를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        //각 뷰를 생성하는 부분
        //아이템에 해당하는 인플레이터 정의
        val view = inflater.inflate(R.layout.note_list_item,parent,false)
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
        holder.carName.setText(itemList.get(position).name)
        holder.carEngine.setText(itemList.get(position).engine)
    }
}