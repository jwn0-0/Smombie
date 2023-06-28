package com.example.smombie.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.smombie.R

@SuppressLint("ViewConstructor")
class AlertPreviewView(context: Context) : OverlayView(context) {

    private val preview: Preview

    private val previewView: PreviewView
    private val blinkView: View

    private val blinkHandler = android.os.Handler(Looper.getMainLooper())
    private val blinkRunnable: Runnable

    private var ready = false

    init {
        require(context is LifecycleOwner)

        inflate(context, R.layout.alert_preview, this)

        previewView = findViewById(R.id.preview)
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        previewView.scaleType = PreviewView.ScaleType.FILL_START

        preview = Preview.Builder().build()

        blinkView = findViewById(R.id.color_overlay)

        blinkRunnable = object : Runnable {
            override fun run() {
                blinkView.visibility = if (blinkView.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                blinkHandler.postDelayed(this, 500)
            }
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
            Log.d("BPreview", "${cameraProvider.isBound(preview)}")
            ready = true
        }, ContextCompat.getMainExecutor(context))
    }

    override fun show() {
        super.show()
        if (ready.not()) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
                Log.d("SPreview", "${cameraProvider.isBound(preview)}")
            }, ContextCompat.getMainExecutor(context))
        }

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    override fun hide() {
        super.hide()
        preview.setSurfaceProvider(null)
        stopBlink()
    }

    override fun startAlert() {
        startBlink()
    }

    override fun stopAlert() {
        stopBlink()
    }

    private fun startBlink() {
        stopBlink()
        blinkHandler.post(blinkRunnable)
    }

    private fun stopBlink() {
        blinkView.visibility = View.GONE
        blinkHandler.removeCallbacksAndMessages(null)
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        preview.setSurfaceProvider(previewView.surfaceProvider)
        cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
    }
}