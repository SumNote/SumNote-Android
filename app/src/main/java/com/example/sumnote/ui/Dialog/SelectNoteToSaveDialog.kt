package com.example.sumnote.ui.Dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sumnote.R
import com.example.sumnote.databinding.SelectNoteToSaveDialogBinding
import com.example.sumnote.ui.Note.NoteItem

class SelectNoteToSaveDialog(noteList: List<NoteItem>, selectNoteToSaveDialogInterface: SelectNoteToSaveDialogInterface) : DialogFragment() {
    private var _binding: SelectNoteToSaveDialogBinding? = null
    private val binding get() = _binding!!

    private var selectNoteToSaveDialogInterface: SelectNoteToSaveDialogInterface? = null

    private var noteList :List<NoteItem> = emptyList()

    //생성자를 통해 유저의 노트아이템 리스트 얻어옴
    init {
        this.noteList = noteList
        this.selectNoteToSaveDialogInterface = selectNoteToSaveDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SelectNoteToSaveDialogBinding.inflate(inflater, container, false)
        val view = binding.root

        //레이아웃에 대한 동작 정의 => 리사이클러뷰 활성화, 등등

        //제대로 노트리스트 가져왔는지 확인
        for(note in noteList){
            Log.d("#noteDialog Check","${note.sum_doc_title}")
        }

        val selectableNoteListAdapter = SelectableNoteRecyclerViewAdapter(noteList, LayoutInflater.from(requireContext()))

        val selectableNoteRecyclerView = binding.customDialogRecyclerView
        selectableNoteRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        selectableNoteRecyclerView.adapter = selectableNoteListAdapter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

//제대로 이해하지 못했으나, 이곳에서 함수의 인터페이스를 작성하고 다이얼로그에서 클릭에 대한 동작을 구현해야 하는것으로 보임
interface SelectNoteToSaveDialogInterface {
    //리스트 선택에 대한 동작 정의? => onYesButtonClick을 수정할것
    fun onYesButtonClick(id: Int)
}


//노트 리스트를 보여주기 위한 리사이클러뷰 어댑터 작성
//선택가능노트 리사이클러뷰
class SelectableNoteRecyclerViewAdapter(
    val itemList : List<NoteItem>, //리사이클러뷰로 그려줄 노트들
    val inflater : LayoutInflater, //화면에 붙이기 위한 inflater
): RecyclerView.Adapter<SelectableNoteRecyclerViewAdapter.ViewHoler>(){ //리사이클러뷰 어댑터 상속받기 템플릿은 자기 자신

    interface OnItemClickListener {
        fun onAllNoteItemClick(position: Int)
    }

    //생성자를 통해 받은 뷰를 부모 클래스로 넘겨주기
    //2. onCreateViewHolder에서 만든 뷰를 생성자로 전달받음
    //해당 위치에서 각 아이템에 대한 이벤트를 달 수 있음
    //data class NoteItem constructor(var id:Int, var title:String, var generatedDate:String)
    inner class ViewHoler(itemView: View): RecyclerView.ViewHolder(itemView){
        //노트 이미지
        val noteImage : ImageView
        //노트 값들
        val title : TextView
        val generatedDate : TextView
        //어댑터가 만들어지면 각 뷰의 값 초기화
        //3. init블럭 호출 => title과 generatedDate 텍스트 뷰가 세팅됨
        init {
            noteImage = itemView.findViewById(R.id.imgView_full_note)
            //생성한 노트 값들
            title = itemView.findViewById(R.id.txt_full_note_title)
            generatedDate = itemView.findViewById(R.id.txt_full_note_gen_date)
        }
    }

    //각 아이템을 그려줌

    //1. 호출되고 나면 아이템 하나가 들어갈 뷰를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        //각 뷰를 생성하는 부분
        //아이템에 해당하는 인플레이터 정의
        val view = inflater.inflate(R.layout.all_note_list_item,parent,false)
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
        holder.title.text = itemList[position].sum_doc_title
//        holder.generatedDate.text = itemList[position].generatedDate
        holder.generatedDate.text = itemList[position].created_at

        //이미지는 position에 해당하는 값으로
        // 이미지의 리소스 ID 얻어오기
        val imageNumber = (position % 9) + 1 //모듈러연산 => img_note의 개수를 벗어나지 않도록
        val imageName = "img_note_$imageNumber"
        val resId = holder.itemView.context.resources.getIdentifier(imageName, "drawable", holder.itemView.context.packageName)
        holder.noteImage.setImageResource(resId)
    }
}
