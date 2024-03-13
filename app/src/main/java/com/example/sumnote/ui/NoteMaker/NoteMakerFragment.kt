package com.example.sumnote.ui.NoteMaker

import android.Manifest
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sumnote.R
import com.example.sumnote.api.ApiManager
import com.example.sumnote.databinding.FragmentNoteMakerBinding
import com.example.sumnote.ui.Dialog.CircleProgressDialog
import com.example.sumnote.ui.Dialog.FailDialog
import com.example.sumnote.ui.Dialog.SuccessDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class NoteMakerFragment : Fragment() {

    private var _binding: FragmentNoteMakerBinding? = null
    private val binding get() = _binding!!

    lateinit var contentResolver: ContentResolver

    lateinit var apiManager: ApiManager

    // 로딩 dialog
    private val loadingDialog = CircleProgressDialog()
    private val successDialog = SuccessDialog()
    private val failDialog = FailDialog()

    // private val baseUrl = "http://3.35.138.31:8000/"
    private val baseUrl = "http://10.0.2.2:8000/" //fastAPI 서버 url

    var pictureUri: Uri? = null // 촬영한 사진에 대한 uri

    // 요청하고자 하는 권한들
    private val permissionList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach {
                if (!it.value) {
                    //Toast.makeText(requireContext(), "권한 허용 필요", Toast.LENGTH_SHORT).show()
                }
            }
            contentResolver = requireContext().contentResolver
            //openDialog(requireContext())
            // Note: 사진 찍는 것은 비동기로 처리됨 -> 여기다 서버 전송 코드 적으면 이미지 가져오기 전에 먼저 처리됨
            pictureUri = createImageFile()
            getTakePicture.launch(pictureUri)
        }


    // 클래스 멤버로 OkHttpClient와 Retrofit 인스턴스를 선언하여 재사용
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    // Life Cycle
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteMakerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 카메라 촬영 버튼
        binding.cameraButton.setOnClickListener{
            // 카메라 권환 확인 및 카메라 열기
            requestMultiplePermission.launch(permissionList)
        }

        // 갤러리 버튼
        binding.galleryButton.setOnClickListener{
            getContentImage.launch("image/*") // 이미지 로드
        }

        // pdf 파일 로드
        binding.pdfButton.setOnClickListener {
            // PDF 파일만 선택하도록 "application/pdf" MIME 타입 지정
            getContentPdf.launch("application/pdf")
        }

    }

    private fun createImageFile(): Uri? {
        val now = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_$now.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
    }


    // 이미지 파일 얻어오기
    private val getContentImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri.let {
            Log.d("##NoteMakerFragment", "Selected IMAGE URI: $it")
            if (it != null) {
                // 사용자의 확인을 받아서 처리
                showConfirmationDialog(it) {
                    sendImageToServer(it) // 사용자가 '확인'을 누른 경우에만 이미지를 서버로 전송
                }
            }
        }
    }

    // pdf 파일 로드용
    private val getContentPdf = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("##NoteMakerFragment", "Selected PDF URI: $it")
            if (it != null) {
                // 사용자의 확인을 받아서 처리
                showConfirmationDialog(it) {
                    sendPdfToServer(it) // 사용자가 '확인'을 누른 경우에만 PDF를 서버로 전송
                }
            }
        }
    }

    // 카메라로 촬영한 이미지 얻어오기
    private val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it) {
            pictureUri?.let { uri ->
                sendImageToServer(uri)
            }
        }
    }



    private fun sendImageToServer(uri: Uri) {
        // 이미지 파일을 임시 파일로 복사
        val file = File(requireContext().cacheDir, "temp_image.jpg").apply {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(this).use { output ->
                    input.copyTo(output)
                }
            }
        }

        // 네트워크 요청 준비
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)


        val bundle = Bundle()
        bundle.putString("dialogText", "노트를 생성하는 중입니다...")
        loadingDialog.arguments = bundle
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        // API 호출
        retrofit.create(ApiManager::class.java).uploadImage(multipartBody).enqueue(object : retrofit2.Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                handleResponse(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleError(t)
            }
        })
    }

    // 파일을 전송할건지 취소할 것인지 여부를 선택 받음
    fun showConfirmationDialog(uri: Uri, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("파일 전송 확인")
            setMessage("선택한 파일을 서버로 전송하시겠습니까?")
            setPositiveButton("확인") { dialog, which ->
                onConfirm() // 사용자가 '확인'을 클릭하면 콜백을 호출
            }
            setNegativeButton("취소", null) // '취소'를 클릭하면 아무것도 하지 않음
            show()
        }
    }

    // 이미지 전송 관련 성공/실패 여부
    private fun handleResponse(response: Response<ResponseBody>) {
        if (response.isSuccessful) {
            //Toast.makeText(this@NoteMakerFragment.activity, "이미지가 전송에 성공하였습니다.", Toast.LENGTH_SHORT).show()

            val responseBody = response.body()?.string()
            try {
                val jsonObject = JSONObject(responseBody)
                val textBook = jsonObject.getString("text")
                Log.d("FastAPI", "Text Book's Text : $textBook")
                val textGpt = jsonObject.getString("sum_result")
                Log.d("FastAPI", "Text GPT's Text : $textGpt")
                val noteTitle = jsonObject.getString("title")
                val summary = jsonObject.getString("summary")

                val bundle = Bundle()
                bundle.putString("title", noteTitle)
                bundle.putString("textBook", summary)


                // 성공 후 dialog 띄우기
                CoroutineScope(Dispatchers.Main).launch {
                    loadingDialog.dismiss()
                    val dialogBundle = Bundle()
                    dialogBundle.putString("dialogText", "노트가 성공적으로 생성되었습니다!")
                    successDialog.arguments = dialogBundle
                    successDialog.show(requireActivity().supportFragmentManager, successDialog.tag)
                    withContext(Dispatchers.Default) { delay(1500) }
                    successDialog.dismiss()

                    //생성된 노트 화면으로 이동
                    findNavController().navigate(R.id.action_navigation_note_maker_to_newNoteFragment,bundle)
                }

            } catch (e: JSONException) {
                Log.e("FastAPI", "Error parsing JSON: ${e.message}")
                showFailDialog()
            }
        } else {
            Toast.makeText(this@NoteMakerFragment.activity, "이미지 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            showFailDialog()
        }
    }

    private fun handleError(t: Throwable) {
        Log.e("ImageUpload", "Image upload error: ${t.message}")
        showFailDialog()
    }


    // pdf 파일 multipart로 fastAPI서버로 전송 -> GPT 노트 얻어오기
    private fun sendPdfToServer(uri: Uri) {
        // PDF 파일을 임시 파일로 복사
        val file = File(requireContext().cacheDir, "temp_pdf.pdf").apply {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(this).use { output ->
                    input.copyTo(output)
                }
            }
        }

        // 네트워크 요청 준비
        val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("pdf", file.name, requestBody)

        val bundle = Bundle().apply {
            putString("dialogText", "PDF 파일을 업로드하는 중입니다...")
        }
        loadingDialog.arguments = bundle
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        // API 호출
        retrofit.create(ApiManager::class.java).uploadPdf(multipartBody).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                handleResponse(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleError(t)
            }
        })
    }



    // 노트 생성 실패시
    private fun showFailDialog() {
        loadingDialog.dismiss()
        CoroutineScope(Dispatchers.Main).launch {
            val dialogBundle = Bundle()
            dialogBundle.putString("dialogText", "노트 생성에 실패하였습니다.")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}