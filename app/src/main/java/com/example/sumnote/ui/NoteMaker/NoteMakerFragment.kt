package com.example.sumnote.ui.NoteMaker

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
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sumnote.R
import com.example.sumnote.api.ApiManager
import com.example.sumnote.databinding.FragmentNoteMakerBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date

class NoteMakerFragment : Fragment() {

    private var _binding: FragmentNoteMakerBinding? = null
    private val binding get() = _binding!!
    lateinit var contentResolver: ContentResolver
    lateinit var apiManager: ApiManager

    lateinit var cameraButton: Button // 노트 촬영 버튼

    //"http://10.0.2.2" => 안드로이드에서의 로컬 호스트를 의미
    val baseUrl = "http://10.0.2.2:8000/" //장고 통신(가상 로컬 아이피)

//    // 권한 허용 요청 및 카메라 열기
//    private val requestMultiplePermission =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
//            results.forEach {
//                if (!it.value) {
//                    Toast.makeText(requireContext(), "권한 허용 필요", Toast.LENGTH_SHORT).show()
//                }
//            }
////            startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
//            contentResolver = requireContext().contentResolver
//            openDialog(requireContext())
//        }
//
//    // 파일 불러오기
//    private val getContentImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri.let {
////            binding.mainImg.setImageURI(uri)
//            System.out.println(uri)
//        }
//    }


//    // 카메라를 실행한 후 찍은 사진을 저장
//    var pictureUri: Uri? = null
//    private val getTakePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
//        if(it) {
//            pictureUri?.let { uri ->
////                binding.mainImg.setImageURI(uri)
//                System.out.println(uri)
//                // 카메라로 사진 촬영 직후 장고 서버에 이미지 전송
//                sendImageToServer(uri)
//            }
//        }
//    }


    private fun createImageFile(): Uri? {
        val now = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_$now.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
    }
//    private fun openDialog(context: Context) {
//        val dialogLayout = layoutInflater.inflate(R.layout.dialog_select_image_method, null)
//        val dialogBuild = AlertDialog.Builder(context).apply {
//            setView(dialogLayout)
//        }
//        val dialog = dialogBuild.create().apply { show() }
//
//        val cameraAddBtn = dialogLayout.findViewById<Button>(R.id.dialog_btn_camera)
//        val fileAddBtn = dialogLayout.findViewById<Button>(R.id.dialog_btn_file)
//
//        //카메라 실행 버튼 누를 경우
//        cameraAddBtn.setOnClickListener {
//            // Create an image file
//            pictureUri = createImageFile()
//            getTakePicture.launch(pictureUri)
//
//            // Note: 사진 찍는 것은 비동기로 처리됨 -> 여기다 서버 전송 코드 적으면 이미지 가져오기 전에 먼저 처리됨
//
//            dialog.dismiss()
//        }
//
//        //갤러리에서 사진 추가?
//        fileAddBtn.setOnClickListener {
//            getContentImage.launch("image/*")
//            dialog.dismiss()
//        }
//    }


    //서버로 이미지 전송
    private fun sendImageToServer(uri: Uri) {
        // Open an InputStream from the Uri
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

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl) // replace this with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                // Note: 타임 아웃 시간은 1분으로 설정 나중에 임의로 변경, Http 통신 로그 확인용 로깅 인터셉터 추가
                OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(
                        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
                            .setLevel(HttpLoggingInterceptor.Level.BODY)
                    ).build()
            )
            .build()

        // Get ApiService instance from Retrofit
        apiManager = retrofit.create(ApiManager::class.java)
        // Call the upload image endpoint
//        val call = apiManager.uploadImage(data)

        // 화질 이슈로 임시로 다른 api로 작성함 => 이슈 개선 후 위 uploadImage로 다시 변경할것
        val call = apiManager.uploadImageTest(data)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                //통신 성공시 => 장고 서버로부터 값을 올바로 받아왔을 경우
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Image upload successful", Toast.LENGTH_SHORT).show()

                    val responseBody = response.body()?.string()
                    try {
                        // Parse JSON response
                        val jsonObject = JSONObject(responseBody)

                        Log.d("Django", "json : ${jsonObject.toString()}")

                        // 받아온 josn데이터로부터, 값 얻어오기
                        val textBook = jsonObject.getString("text")

                        // Show a toast message
                        Toast.makeText(requireContext(), "Image upload successful", Toast.LENGTH_SHORT).show()

                        //교과서 정보 화면으로 이동
                        val bundle = Bundle()
                        bundle.putString("textBook", textBook) //교과서에서 추출한 json데이터 넣어서 넘기기(임시로 string)
                        findNavController().navigate(R.id.action_navigation_note_maker_to_newNoteFragment, bundle)


                    } catch (e: JSONException) {
                        Log.e("DjangoServer-communication", "Error parsing JSON: ${e.message}")
                    }
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("DjangoServer-communication", "fail: ${t.localizedMessage}, ${t.stackTrace.joinToString("\n")}")
            }
        })

    }


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

        //카메라 프래그먼트로 임시 이동하여 사진 촬영해오기
        findNavController().navigate(R.id.action_navigation_note_maker_to_cameraFragement)

//        cameraButton = binding.btnCamera
//        cameraButton.setOnClickListener {
//            // 카메라 권환 확인 및 카메라 열기
//            requestMultiplePermission.launch(permissionList)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}