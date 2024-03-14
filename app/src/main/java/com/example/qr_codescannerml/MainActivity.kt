package com.example.qr_codescannerml

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageProxy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qr_codescannerml.ui.theme.QRCodeScannerMLTheme
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val previewView = PreviewView(this)


        setContent {
            QRCodeScannerMLTheme {
                // A surface container using the 'background' color from the theme
                Surface() {
                    // Request camera permissions
                    if (allPermissionsGranted()) {
                        // startCamera()
                        CameraPreview(previewView = previewView)
                    } else {
                        this.requestPermissions(
                            REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                        )
                    }
                    cameraExecutor = Executors.newSingleThreadExecutor()

                }
            }
        }
    }


    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        previewView: PreviewView
    ) {
        val context = LocalContext.current
        val cameraController = LifecycleCameraController(context)

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(Color.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_START
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(this)
                }
            }
        )


        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                result?.getValue(barcodeScanner)?.map {
                    val name = it.contactInfo?.name
                }
                result?.getValue(barcodeScanner)?.forEach {
                    Toast.makeText(context, it.displayValue, Toast.LENGTH_SHORT).show()
                }
            }
        )


        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController


    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        private const val TAG = "CameraX-MLKit"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).toTypedArray()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //Do Something
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

    }
}


