package com.example.sumnote.ui.Camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
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
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sumnote.R
import com.example.sumnote.api.ApiManager
import com.example.sumnote.databinding.FragmentCameraBinding
import com.example.sumnote.ui.Dialog.CircleProgressDialog
import com.example.sumnote.ui.Dialog.SuccessDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


//
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

    // 로딩 dialog
    private val loadingDialog = CircleProgressDialog()
    private val successDialog = SuccessDialog()

    private val baseUrl = "http://223.194.135.114:8000/" //장고 서버 url
//    private val baseUrl = "http://43.201.71.53:80/"

    // 전달받은 json값이 null인 경우에 대한 예외처리 => 아직 적용 안했음
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

        onHiddenChanged(true) //하단바 안보이게 하기 => 실제 카메라 처럼 보이도록

        textureView = binding.textureView // 카메라로부터 가져온 프리뷰를 보여주기 위한 화면
        cameraManager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager //카메라 매니저 가져오기 => 나중에 닫아야함
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

                //이미지 저장 작업 수행?
                var image = reader?.acquireLatestImage()
                var buffer = image!!.planes[0].buffer
                bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                //캡처한 이미지 저장 => bytes정보 imagebytes
                //saveImageToMediaStore(bytes)
                //저장하지 않고 이미지 서버로 바로 전송 => 저장 희망하면 아래 주석하고 위 주석 풀기
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

        // 이미지를 Bitmap으로 변환
        val originalBitmap = byteArrayToBitmap(imageBytes)

        // Bitmap을 90도 회전
        val rotatedBitmap = rotateBitmap(originalBitmap, 270)

        // 회전된 Bitmap을 ByteArray로 다시 변환
        val rotatedImageBytes = bitmapToByteArray(rotatedBitmap)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiManager = retrofit.create(ApiManager::class.java)

        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), rotatedImageBytes)
        val imagePart = MultipartBody.Part.createFormData("image", "book.jpg", requestBody)

        val call = apiManager.uploadImageTest(imagePart)

        val bundle = Bundle()
        bundle.putString("dialogText", "노트를 생성하는 중입니다...")
        loadingDialog.arguments = bundle
        loadingDialog.show(requireActivity().supportFragmentManager, loadingDialog.tag)

        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("DjangoServer","send success")
                    Toast.makeText(this@CameraFragment.activity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()

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
                            findNavController().navigate(R.id.action_cameraFragement_to_newNoteFragment,bundle)
                        }

                    } catch (e: JSONException) {
                        Log.e("DjangoServer", "Error parsing JSON: ${e.message}")
                        loadingDialog.dismiss()
                    }
                } else {
                    Toast.makeText(this@CameraFragment.activity, "Image upload failed", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ImageUpload", "Image upload error: ${t.message}")
                loadingDialog.dismiss()
            }
        })
        Log.d("sendImage","sendImageToServer Exit")
    }

    // 정지된 화면을 ImageView에 표시
    private fun showCapturedImagePreview() {
        Log.d("test : ", "##6")

        // 이미지 캡처 리스너 중지 => 무한 루프 방지(프래그먼트 종료시점에 닫기)
        //imageReader.setOnImageAvailableListener(null, null)

        // 이미지 프리뷰 표시
        val bitmap = textureView.bitmap
        val imageView = binding.imgviewPreview
        imageView.setImageBitmap(bitmap)
        imageView.visibility = View.VISIBLE

        // TextureView 숨기기
        textureView.visibility = View.INVISIBLE
    }

    //ByteArray를 MediaStore객체 사용해서 이미지로 저장
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

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }


    //비트맵을 90도 회전하여 사진이 90도 뒤집혀 저장되는 사태를 방지(이미지 크기가 너무 큰것이 원인으로 보임)
    private fun rotateBitmap(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    //권한이 허용되어 있는 경우에만 작동 => 권한은 MainActivity에서
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


    //프래그먼트 종료 시점 => 이미지리더, 카메라 디바이스 닫기
    override fun onDestroy() {
        super.onDestroy()
        imageReader.setOnImageAvailableListener(null, null)
        camerDevice.close()
        handlerThread.quit()

    }




    //카메라 화면에서는 바텀네비게이션 뷰 숨기기 => 실제 카메라 처럼 보이도록
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