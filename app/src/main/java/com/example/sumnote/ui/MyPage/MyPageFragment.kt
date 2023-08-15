package com.example.sumnote.ui.MyPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.sumnote.R
import com.example.sumnote.databinding.FragmentMyPageBinding
import com.example.sumnote.loginActivity

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
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