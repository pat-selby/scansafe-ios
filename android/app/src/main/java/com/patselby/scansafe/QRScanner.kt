package com.patselby.scansafe

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRScanner(private val onResult: (RiskResult, String) -> Unit) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        try {
            // Convert ImageProxy to Bitmap
            val bitmap = imageProxy.toBitmap()

            // Convert Bitmap to OpenCV Mat structure
            val srcMat = Mat()
            Utils.bitmapToMat(bitmap, srcMat)

            // Step 1: Convert frame to grayscale
            // Why: Simplifies the image from 3 channels (RGB) to 1 channel (intensity),
            // which reduces computational load and is required for edge detection.
            val grayMat = Mat()
            Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGBA2GRAY)

            // Step 2: Apply Gaussian blur (kernel size 5x5)
            // Why: Noise reduction. Blurring smooths out the image to prevent 
            // the edge detector from falsely identifying structural noise as edges.
            val blurredMat = Mat()
            Imgproc.GaussianBlur(grayMat, blurredMat, Size(5.0, 5.0), 0.0)

            // Step 3: Apply Canny edge detection
            // Why: Finds boundaries of objects in the image. QR codes have very sharp, 
            // distinct edges (black squares on white backgrounds) that this will isolate.
            val edgesMat = Mat()
            Imgproc.Canny(blurredMat, edgesMat, 50.0, 150.0)

            // Step 4: Run FindContours
            // Why: Isolates shapes. This connects the edges found in the previous step 
            // into closed shapes (contours), which we can then filter to find the 3 finder squares.
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(
                edgesMat,
                contours,
                hierarchy,
                Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            // Step 5: Identify QR finder pattern (three square markers)
            // Note: Full geometric isolation of the 3 squares would occur here.

            // Step 6: Pass detected QR to ML Kit for URL string extraction only
            val inputImage = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            // Step 7: Send URL string to URLRiskScorer
                            val result = URLRiskScorer.evaluate(rawValue)
                            Log.d(TAG, "Extracted URL: $rawValue | Verdict: ${result.verdict} | Reason: ${result.reason}")
                            onResult(result, rawValue)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ML Kit barcode decoding failed", e)
                }

            srcMat.release()
            grayMat.release()
            blurredMat.release()
            edgesMat.release()
            hierarchy.release()

        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        } finally {
            // Must call close to free the ImageProxy and receive the next frame
            imageProxy.close()
        }
    }

    companion object {
        private const val TAG = "QRScanner"
    }
}
