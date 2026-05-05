package com.example.buddytalk.data.vosk

import android.content.Context
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

/**
 * Manager class for VOSK Speech Recognition.
 * Handles model loading, service initialization, and start/stop listening.
 */
class VoskManager(private val context: Context) {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    
    interface VoskCallback {
        fun onResult(text: String)
        fun onPartialResult(text: String)
        fun onError(e: Exception)
        fun onModelLoaded()
    }

    /**
     * Initializes the VOSK model. This should be called once (e.g., in ViewModel or Activity).
     * Unpacks the model from assets to internal storage if necessary.
     */
    fun initModel(modelPath: String, callback: VoskCallback) {
        println("VoskManager: Bắt đầu unpack model từ assets: $modelPath")
        StorageService.unpack(context, modelPath, "model",
            { model: Model ->
                this.model = model
                println("VoskManager: Unpack THÀNH CÔNG")
                callback.onModelLoaded()
            },
            { exception: IOException ->
                println("VoskManager: LỖI unpack: ${exception.message}")
                callback.onError(exception)
            }
        )
    }

    /**
     * Starts the speech recognition service.
     */
    fun startListening(callback: VoskCallback) {
        val model = this.model ?: run {
            callback.onError(Exception("Model not loaded"))
            return
        }

        try {
            // Initialize recognizer with 16kHz sample rate
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String) {
                    val text = parseVoskJson(hypothesis, "partial")
                    callback.onPartialResult(text)
                }

                override fun onResult(hypothesis: String) {
                    val text = parseVoskJson(hypothesis, "text")
                    callback.onResult(text)
                }

                override fun onFinalResult(hypothesis: String) {
                    val text = parseVoskJson(hypothesis, "text")
                    callback.onResult(text)
                }

                override fun onError(exception: Exception) {
                    callback.onError(exception)
                }

                override fun onTimeout() {
                    // Handle timeout if needed
                }
            })
        } catch (e: IOException) {
            callback.onError(e)
        }
    }

    /**
     * Stops the speech recognition service.
     */
    fun stopListening() {
        speechService?.stop()
        speechService = null
    }

    /**
     * Releases resources. Should be called when the manager is no longer needed.
     */
    fun release() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        // Model doesn't need explicit release in this version of Vosk-Android
    }

    /**
     * Parses the JSON output from VOSK to extract the recognized text.
     */
    private fun parseVoskJson(json: String, key: String): String {
        return try {
            val obj = JSONObject(json)
            obj.optString(key, "")
        } catch (e: Exception) {
            ""
        }
    }
}
