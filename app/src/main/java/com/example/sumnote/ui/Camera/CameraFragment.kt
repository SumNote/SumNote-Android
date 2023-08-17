package com.example.sumnote.ui.Camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sumnote.R
import com.example.sumnote.api.ApiManager
import com.example.sumnote.databinding.FragmentCameraBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Type

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    lateinit var capReq : CaptureRequest.Builder
    lateinit var handler : Handler
    lateinit var handlerThread : HandlerThread
    lateinit var cameraManager :CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var camerDevice: CameraDevice
    lateinit var captureRequest : CaptureRequest

    lateinit var bytes : ByteArray

    lateinit var imageReader: ImageReader

    private var isCapturing = false //사진이 촬영된 상태인지 아닌지 여부

    // 서버 통신 테스트
    lateinit var apiManager: ApiManager

    val baseUrl = "http://15.165.186.162:8000/" //장고 통신시

    private val nullOnEmptyConverterFactory = object : Converter.Factory() {
        fun converterFactory() = this
        override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object :
            Converter<ResponseBody, Any?> {
            val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
            override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) {
                try{
                    nextResponseBodyConverter.convert(value)
                }catch (e:Exception){
                    e.printStackTrace()
                    null
                }
            } else{
                null
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //뷰 생성 시점
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onHiddenChanged(true)


        textureView = binding.textureView // 카메라로부터 가져온 프리뷰를 보여주기 위한 화면?
        cameraManager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager //카메라 매니저 가져오기
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                open_camera() //카메라 실행
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            //카메라 종료시?
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }

        }


        //이미지 캡처 = > 사진 촬영에 해당
        imageReader = ImageReader.newInstance(1080,1920,ImageFormat.JPEG,1)
        imageReader.setOnImageAvailableListener(object: ImageReader.OnImageAvailableListener{
            override fun onImageAvailable(reader: ImageReader?) {


                Log.d("test : ", "##3")
                Log.d("CameraApp", "onImageAvailable: Image captured and processing started.")

                //이미지 저장 작업 수행?
                var image = reader?.acquireLatestImage()
                var buffer = image!!.planes[0].buffer
                bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                //캡처한 이미지 저장 => bytes정보 imagebytes
                //saveImageToMediaStore(bytes)
                //저장하지 않고 이미지 서버로 바로 전송
                sendImageToServer(bytes) // 이미지 바이트 배열 전달

                image.close()
                Toast.makeText(this@CameraFragment.activity,"image captured",Toast.LENGTH_SHORT).show()

                Log.d("CameraApp", "onImageAvailable: Image processing completed and saved.")
            }
        },handler)


        // 사진 촬영 버튼 클릭 이벤트 설정
        binding.btnCapture.apply {
            setOnClickListener{

                if (!isCapturing) {
                    Log.d("test : ", "##1")
                    isCapturing = true
                    captureStillPhoto()
                }
            }


//            setOnTouchListener { view, event ->
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        // 버튼을 눌렀을 때의 동작
//                        if (!isCapturing) {
//                            isCapturing = true
//                            captureStillPhoto()
//                        }
//                        true // 이벤트 소비
//                    }
//
//                    MotionEvent.ACTION_UP -> {
//                        // 버튼을 뗀 순간의 동작 (필요한 경우)
//                        // 눌린 상태를 해제하거나 추가 동작 수행
//                        true // 이벤트 소비
//                    }
//
//                    else -> false // 나머지 이벤트는 처리하지 않음
//                }
//            }
        }

    }


    // 사진 촬영 함수
    private fun captureStillPhoto() {

        Log.d("test : ", "##2")
        // 기존 카메라 프리뷰 중지 => 사진 촬영이 완료되었음으로
        cameraCaptureSession.stopRepeating()

        // CaptureRequest 설정
        capReq = camerDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        //imageReader 호출
        capReq.addTarget(imageReader.surface)


        // 사진 촬영 실행
        cameraCaptureSession.capture(capReq.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                Log.d("test : ", "##4")
                // 사진 촬영 완료 시 작업 수행
                activity?.runOnUiThread {
                    Log.d("test : ", "##5")
                    isCapturing = false //사진 촬영된 상태로 변경
                    showCapturedImagePreview() // 정지된 화면 표시
                }
            }
        }, null)
    }



    private fun sendImageToServer(imageBytes: ByteArray){
        Log.d("sendImage","sendImageToServer Call")

        Log.d("test : ", "##8")

//        val file = File(requireContext().cacheDir, "cache.jpg")
//            FileOutputStream(file).use {
//                it.write(imageBytes)
//        }
//
//
//        //개선한 방식 => 파일 객체 그 자체를 보내도록 코드 변경
//        val body = file.asRequestBody(
//            "image/*".toMediaTypeOrNull()
//        )
//
//        //폼 데이터 형식으로 key : image, value : data로 파일 전송
//        val data = MultipartBody.Part.createFormData(
//            name = "image",
//            filename = file.name,
//            body = body
//        )



        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiManager = retrofit.create(ApiManager::class.java)

        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        val call = apiManager.uploadImage(imagePart)
       //val call = apiManager.uploadImage(imagePart)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 서버로 이미지 전송 성공시
                    Log.d("sendImage","send success")
                    Toast.makeText(this@CameraFragment.activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    // 서버 응답에 대한 추가 처리 코드 작성

                    val responseBody = response.body()?.string()

                    try {
                        // Parse JSON response
                        val jsonObject = JSONObject(responseBody)
                        val predictedClass = jsonObject.getString("predicted_class")
                        val predictedProb = jsonObject.getDouble("predicted_prob")

                        Log.d("gang", "Predicted Class: $predictedClass, Predicted Probability: $predictedProb")

                        // Show a toast message
                        Toast.makeText(this@CameraFragment.activity, "Image upload successful!!", Toast.LENGTH_SHORT).show()

                        //노트 생성 화면으로 이동
//                        val bundle = Bundle()
//                        bundle.putString("pillName", predictedClass) //일약 이름 번들에 넣기
//                        findNavController().navigate(R.id.action_menuFragment_to_pillInfoFragment, bundle)


                    } catch (e: JSONException) {
                        Log.e("gang", "Error parsing JSON: ${e.message}")
                    }


                } else {
                    // 서버로 이미지 전송 실패시
                    Toast.makeText(this@CameraFragment.activity, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                Log.e("ImageUpload", "Image upload error: ${t.message}")
            }
        })
        Log.d("sendImage","sendImageToServer Exit")
    }




    // 정지된 화면을 ImageView에 표시
    private fun showCapturedImagePreview() {
        Log.d("test : ", "##6")

        // 이미지 캡처 리더 중지
        //imageReader.setOnImageAvailableListener(null, null)

        // 이미지 프리뷰 표시
        val bitmap = textureView.bitmap
        val imageView = binding.imgviewPreview
        imageView.setImageBitmap(bitmap)
        imageView.visibility = View.VISIBLE

        // TextureView 숨기기
        textureView.visibility = View.INVISIBLE
    }

    private fun saveImageToMediaStore(imageBytes: ByteArray) {
        Log.d("test : ", "##7")


        Log.d("sendImage","saveIamgeToMediaStore call")
        // 사진 촬영 완료 후 서버로 이미지 전송
        sendImageToServer(bytes) // 이미지 바이트 배열 전달

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img.jpeg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val contentResolver = activity?.contentResolver
        val imageUri = contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        imageUri?.let {
            try {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    val rotatedBitmap = rotateBitmap(bitmap, 90) // 90도 회전 (원하는 각도로 수정)
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


    }

    //사진이 90도 뒤집혀 저장되는 사태 방지 => 다시 90도 회전(이미지 크기 때문인듯)
    private fun rotateBitmap(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    //권한이 허용되어 있는 경우에만 작동
    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0],object:CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                camerDevice = camera //작동시킬 카메라 디바이스 설정?
                //캡처 리퀘스트?
                capReq = camerDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                //textureView로부터 surface 생성
                var surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface) //캡처해온 화면을 보여줄 surface설정?

                camerDevice.createCaptureSession(listOf(surface,imageReader.surface),object:CameraCaptureSession.StateCallback(){
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session //카메라 캡처 세션 설정
                        cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {

                    }
                },handler)

            }

            override fun onDisconnected(camera: CameraDevice) {

            }

            override fun onError(camera: CameraDevice, error: Int) {

            }
        },handler)
    }


    override fun onDestroy() {
        super.onDestroy()
        imageReader.setOnImageAvailableListener(null, null)
        camerDevice.close()

    }


    //카메라 화면에서는 바텀네비게이션 뷰 숨기기
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)

        if (hidden) {
            bottomNavigationView?.visibility = View.GONE
        } else {
            bottomNavigationView?.visibility = View.VISIBLE
        }
    }

}