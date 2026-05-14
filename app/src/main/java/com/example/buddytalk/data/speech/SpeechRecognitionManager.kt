package com.example.buddytalk.data.speech

import android.content.Context
import kotlinx.coroutines.*
import java.io.File

/**
 * Orchestrates the speech recognition flow using NVIDIA Parakeet-CTC-0.6B-Vi.
 *
 * Flow:
 * 1. User presses and holds the mic button → [startListening] begins recording audio
 * 2. User releases the button → [stopListening] stops recording
 * 3. Audio is sent to NVIDIA cloud (build.nvidia.com) via gRPC
 * 4. API returns transcription → callback is invoked with the result
 *
 * This replaces the old VoskManager which performed on-device recognition.
 * Now we record audio locally and send it to the NVIDIA Parakeet cloud API.
 */
class SpeechRecognitionManager(private val context: Context) {

    private val audioRecorder = AudioRecorder()
    private var parakeetClient: ParakeetApiClient? = null
    private var recordingJob: Job? = null
    private var audioFile: File? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Callback interface for speech recognition events.
     */
    interface RecognitionCallback {
        fun onResult(text: String)
        fun onPartialResult(text: String)
        fun onError(e: Exception)
        fun onReady()
    }

    /**
     * Initializes the Parakeet gRPC client.
     * Connects to NVIDIA cloud (build.nvidia.com) using the config in ParakeetConfig.
     *
     * @param callback Notified when the client is ready or if initialization fails
     */
    fun initialize(callback: RecognitionCallback) {
        try {
            parakeetClient = ParakeetApiClient()
            println("SpeechRecognitionManager: Initialized gRPC client → ${ParakeetConfig.GRPC_HOST}:${ParakeetConfig.GRPC_PORT}")
            callback.onReady()
        } catch (e: Exception) {
            println("SpeechRecognitionManager: Initialization failed: ${e.message}")
            callback.onError(e)
        }
    }

    /**
     * Starts recording audio from the microphone.
     * The recording continues until [stopListening] is called.
     *
     * @param callback Receives partial updates (recording status) and errors
     */
    fun startListening(callback: RecognitionCallback) {
        val client = parakeetClient ?: run {
            callback.onError(Exception("Parakeet API client not initialized"))
            return
        }

        // Create temporary WAV file
        audioFile = File(context.cacheDir, "parakeet_recording_${System.currentTimeMillis()}.wav")

        // Show recording indicator
        callback.onPartialResult("Đang ghi âm...")

        recordingJob = scope.launch {
            try {
                // Start recording in background - this suspends until stopRecording() is called
                audioRecorder.startRecording(audioFile!!)

                // Recording has stopped, now send to NVIDIA cloud via gRPC
                callback.onPartialResult("Đang nhận diện...")

                val result = client.transcribe(audioFile!!)

                if (result.text.isNotBlank()) {
                    callback.onResult(result.text)
                } else {
                    callback.onResult("")
                }
            } catch (e: CancellationException) {
                // Job was cancelled, ignore
                println("SpeechRecognitionManager: Recording cancelled")
            } catch (e: Exception) {
                println("SpeechRecognitionManager: Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.onError(e)
                }
            } finally {
                // Clean up temporary file
                audioFile?.let { file ->
                    if (file.exists()) {
                        file.delete()
                    }
                }
                audioFile = null
            }
        }
    }

    /**
     * Stops recording audio. The recorded audio will be automatically
     * sent to the Parakeet cloud API for transcription via gRPC.
     */
    fun stopListening() {
        audioRecorder.stopRecording()
        // Note: we do NOT cancel recordingJob here — we want it to continue
        // with the gRPC API call after recording stops
    }

    /**
     * Releases all resources. Should be called when the manager is no longer needed.
     */
    fun release() {
        recordingJob?.cancel()
        recordingJob = null
        parakeetClient?.release()
        parakeetClient = null
        audioFile?.let { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        audioFile = null
        scope.cancel()
    }
}
