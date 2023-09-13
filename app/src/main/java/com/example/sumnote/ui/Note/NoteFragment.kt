package com.example.sumnote.ui.Note

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sumnote.databinding.FragmentNoteBinding


//한 노트에 대한 페이지 클래스
data class Page(
//    val noteId : Int,
    val pageTitle: String,
    val summary: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
//        parcel.readInt() ?:-1,
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(noteId)
        parcel.writeString(pageTitle)
        parcel.writeString(summary)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Page> {
        override fun createFromParcel(parcel: Parcel): Page {
            return Page(parcel)
        }

        override fun newArray(size: Int): Array<Page?> {
            return arrayOfNulls(size)
        }
    }
}


class NoteFragment : Fragment() {

    private lateinit var page: Page //현재 화면에 보여주기 위한 노트 객체
    private var _binding : FragmentNoteBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
             page = it.getParcelable(NoteFragment.ARG_PAGE)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNoteBinding.inflate(inflater, container, false)

        var noteTitle = binding.noteTitle
        noteTitle.text = page.pageTitle
        var noteSummary = binding.noteSummary
        noteSummary.text = page.summary


        return binding.root
    }



    companion object {
        private const val ARG_PAGE = "note"
        //번들로 객체를 전달하기 위해 Page객체를 Parcelable객체로 변환
        fun newInstance(page: Page) = NoteFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PAGE, page)
            }
        }
    }

}