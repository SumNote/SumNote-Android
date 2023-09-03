package com.example.sumnote.ui.Note

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sumnote.databinding.FragmentNoteBinding


//노트 데이터 클래스
//id, 노트 제목, 요약 정보량
data class Note(
//    val noteId : Int,
    val noteTitle: String,
    val summary: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
//        parcel.readInt() ?:-1,
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(noteId)
        parcel.writeString(noteTitle)
        parcel.writeString(summary)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}


class NoteFragment : Fragment() {

    private lateinit var note: Note //현재 화면에 보여주기 위한 노트 객체
    private var _binding : FragmentNoteBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            note = it.getParcelable(NoteFragment.ARG_NOTE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        var noteTitle = binding.noteTitle
        noteTitle.text = note.noteTitle
        var noteSummary = binding.noteSummary
        noteSummary.text = note.summary


        return binding.root
    }



    companion object {
        private const val ARG_NOTE = "note"
        //번들로 객체를 전달하기 위해 Note객체를 Parcelable객체로 변환
        fun newInstance(note: Note) = NoteFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_NOTE, note)
            }
        }
    }

}