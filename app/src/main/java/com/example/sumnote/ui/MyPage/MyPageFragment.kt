package com.example.sumnote.ui.MyPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyPageBinding
import com.example.sumnote.loginActivity

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // view모델과 연결
        // view : 화면에 대한 정보,
        // model : 데이터 관리, 비즈니스 로직 담당(함수 구현)
        // view model : view와 모델간의 매개체
        val myPageViewModel =
            ViewModelProvider(this)[MyPageViewModel::class.java]

        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyPage
        myPageViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val button = view?.findViewById<Button>(R.id.goLogin)

        button?.setOnClickListener {
            val intent = Intent(context, loginActivity::class.java)
            startActivity(intent)

        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}