package com.example.qr_codescannerml

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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


        // Request camera permissions
        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setContent {
            QRCodeScannerMLTheme {
                Surface {
                    CameraPreview(
                        context = this,
                    )
                }
            }
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        context: Context,
    ) {

        val cameraController = LifecycleCameraController(context)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val screenHeightDp = configuration.screenHeightDp
        val density = LocalDensity.current.density
        val screenWidthPx = screenWidthDp * density
        val screenHeightPx = screenHeightDp * density

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PreviewView(ctx).apply {
//                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    layoutParams = LayoutParams(screenWidthPx.toInt(),screenHeightPx.toInt())
                    setBackgroundColor(Color.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(this.surfaceProvider)
                    preview.resolutionSelector?.allowedResolutionMode


                    cameraController.setImageAnalysisAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        MlKitAnalyzer(
                            listOf(barcodeScanner),
                            COORDINATE_SYSTEM_ORIGINAL,
                            ContextCompat.getMainExecutor(context)
                        ) { result: MlKitAnalyzer.Result? ->
                            val barcodeResults = result?.getValue(barcodeScanner)
                            if ((barcodeResults == null) ||
                                (barcodeResults.size == 0) ||
                                (barcodeResults.first() == null)
                            ) {
                                this.overlay.clear()
                                return@MlKitAnalyzer
                            }

                            val qrCodeViewModel = QrCodeViewModel(barcodeResults[0])
                            val myDraw = MyDraw(qrCodeViewModel)

                            this.overlay.clear()
                            this.overlay.add(myDraw)
                        }
                    )

                    cameraController.bindToLifecycle(this@MainActivity)
                    this.controller = cameraController

                }

            }
        )


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


