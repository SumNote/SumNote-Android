package com.example.sumnote.ui.NoteMaker

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
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


    private val baseUrl = "http://3.35.138.31:8000/"
    //private val baseUrl = "http://10.0.2.2:8000/" //장고 서버 url


    // 요청하고자 하는 권한들
    private val permissionList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
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


        //카메라 촬영 버튼
        binding.cameraButton.setOnClickListener{
            // 카메라 권환 확인 및 카메라 열기
            Log.d("#NOTEMAKER DEBUG: ","camera 1")
            requestMultiplePermission.launch(permissionList)
        }

        //갤러리 버튼
        binding.galleryButton.setOnClickListener{
            Log.d("#NOTEMAKER DEBUG: ","gallery 1")

            getContentImage.launch("image/*")
        }

        //테스트용 => 나중에 지울것
        binding.howToCapture.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_note_maker_to_newNoteFragment)
        }
    }

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

    private fun openDialog(context: Context) {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_select_image_method, null)
        val dialogBuild = AlertDialog.Builder(context).apply {
            setView(dialogLayout)
        }
        val dialog = dialogBuild.create().apply { show() }

        val cameraAddBtn = dialogLayout.findViewById<Button>(R.id.dialog_btn_camera)
        val fileAddBtn = dialogLayout.findViewById<Button>(R.id.dialog_btn_file)

        cameraAddBtn.setOnClickListener {
            // Create an image file
            pictureUri = createImageFile()
            getTakePicture.launch(pictureUri)

            // Note: 사진 찍는 것은 비동기로 처리됨 -> 여기다 서버 전송 코드 적으면 이미지 가져오기 전에 먼저 처리됨
            dialog.dismiss()
        }

        fileAddBtn.setOnClickListener {
            getContentImage.launch("image/*")
            dialog.dismiss()
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


    // 파일 불러오기
    private val getContentImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri.let {
            Log.d("#NOTEMAKER DEBUG: ","call getContentImage")
            if (it != null) {
                sendImageToServer(it)
            }
        }
    }

    // 카메라를 실행한 후 찍은 사진을 저장
    var pictureUri: Uri? = null
    private val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if(it) {
            pictureUri?.let { uri ->
                // binding.mainImg.setImageURI(uri)
                // Note: 카메라로 사진 촬영 직후 서버에 이미지 전송하기
                Log.d("#NOTEMAKER DEBUG: ","6")
                sendImageToServer(uri)
            }
        }
    }

    private fun sendImageToServer(uri: Uri) {
        Log.d("#NOTEMAKER DEBUG: ","call sendImageToServer")
        contentResolver = requireContext().contentResolver
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        val file = File(requireContext().cacheDir, "cache.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }


        //개선한 방식 => 파일 객체 그 자체를 보내도록 코드 변경
        val body = file.asRequestBody(
            "image/*".toMediaTypeOrNull()
        )

        //폼 데이터 형식으로 key : image, value : data로 파일 전송
        val data = MultipartBody.Part.createFormData(
            name = "image",
            filename = file.name,
            body = body
        )


        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        apiManager = retrofit.create(ApiManager::class.java)

        val call = apiManager.uploadImage(data)

        val bundle = Bundle()
        bundle.putString("dialogText", "노트를 생성하는 중입니다...")
        loadingDialog.arguments = bundle
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("DjangoServer","send success")
                    Toast.makeText(this@NoteMakerFragment.activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()

                    val responseBody = response.body()?.string()
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val textBook = jsonObject.getString("text")
                        Log.d("DjangoServer", "Text Book's Text : $textBook")
                        val textGpt = jsonObject.getString("sum_result")
                        Log.d("DjangoServer", "Text GPT's Text : $textGpt")
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
                        Log.e("DjangoServer", "Error parsing JSON: ${e.message}")
                        showFailDialog()
                    }
                } else {
                    Toast.makeText(this@NoteMakerFragment.activity, "Image upload failed", Toast.LENGTH_SHORT).show()
                    showFailDialog()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ImageUpload", "Image upload error: ${t.message}")
                showFailDialog()
            }
        })
        Log.d("sendImage","sendImageToServer Exit")
    }

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