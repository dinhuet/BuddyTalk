package com.example.buddytalk.data.speech

import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import nvidia.riva.AudioEncoding
import nvidia.riva.asr.RecognitionConfig
import nvidia.riva.asr.RivaSpeechRecognitionGrpc
import nvidia.riva.asr.StreamingRecognitionConfig
import nvidia.riva.asr.StreamingRecognizeRequest
import nvidia.riva.asr.StreamingRecognizeResponse
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * gRPC client for NVIDIA Parakeet-CTC-0.6B-Vi ASR via build.nvidia.com cloud.
 *
 * Uses the Riva gRPC **StreamingRecognize** RPC to send audio for Vietnamese
 * speech-to-text transcription. The cloud endpoint only supports streaming mode.
 *
 * Flow:
 * 1. Open a streaming gRPC call
 * 2. Send StreamingRecognitionConfig (first message)
 * 3. Send audio data in chunks (subsequent messages)
 * 4. Close the client stream and wait for final result
 */
class ParakeetApiClient(
    private val serverHost: String = ParakeetConfig.GRPC_HOST,
    private val serverPort: Int = ParakeetConfig.GRPC_PORT,
    private val apiKey: String = ParakeetConfig.API_KEY,
    private val functionId: String = ParakeetConfig.FUNCTION_ID
) {
    companion object {
        /** Size of each audio chunk sent to the server (32KB) */
        private const val CHUNK_SIZE = 32 * 1024
    }

    private var channel: ManagedChannel? = null
    private var asyncStub: RivaSpeechRecognitionGrpc.RivaSpeechRecognitionStub? = null

    init {
        setupChannel()
    }

    /**
     * Sets up the gRPC channel with TLS and authentication metadata.
     */
    private fun setupChannel() {
        channel = ManagedChannelBuilder
            .forAddress(serverHost, serverPort)
            .useTransportSecurity() // TLS for build.nvidia.com
            .build()

        // Attach authentication metadata (API key + function ID)
        val metadata = Metadata().apply {
            put(
                Metadata.Key.of("function-id", Metadata.ASCII_STRING_MARSHALLER),
                functionId
            )
            put(
                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                "Bearer $apiKey"
            )
        }

        asyncStub = RivaSpeechRecognitionGrpc.newStub(channel)
            .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
    }

    /**
     * Transcribes an audio file using NVIDIA Parakeet-CTC-0.6B-Vi via gRPC streaming.
     *
     * Uses the `StreamingRecognize` RPC:
     * 1. First message: sends StreamingRecognitionConfig
     * 2. Subsequent messages: sends audio data in chunks
     * 3. Closes the stream and collects the final transcript
     *
     * @param audioFile The WAV file to transcribe (must be 16-bit mono PCM at 16kHz).
     * @param languageCode The language code (default: "vi-VN" for Vietnamese).
     * @return [TranscriptionResult] containing the transcribed text.
     * @throws TranscriptionException if the gRPC call fails.
     */
    suspend fun transcribe(
        audioFile: File,
        languageCode: String = ParakeetConfig.LANGUAGE_CODE
    ): TranscriptionResult = withContext(Dispatchers.IO) {
        val stub = asyncStub ?: throw TranscriptionException("gRPC client not initialized")

        if (!audioFile.exists() || audioFile.length() == 0L) {
            throw TranscriptionException("Audio file is empty or does not exist")
        }

        // Read audio bytes (skip the 44-byte WAV header, send raw PCM)
        val audioBytes = audioFile.readBytes()
        val pcmBytes = if (audioBytes.size > 44) {
            audioBytes.copyOfRange(44, audioBytes.size)
        } else {
            throw TranscriptionException("Audio file too small")
        }

        println("ParakeetApiClient: Sending ${pcmBytes.size} bytes of audio via gRPC streaming to $serverHost")

        // Use suspendCancellableCoroutine to bridge gRPC async callback to coroutines
        suspendCancellableCoroutine { continuation ->
            val finalTranscript = StringBuilder()

            // Create response observer
            val responseObserver = object : StreamObserver<StreamingRecognizeResponse> {
                override fun onNext(response: StreamingRecognizeResponse) {
                    for (result in response.resultsList) {
                        if (result.isFinal && result.alternativesList.isNotEmpty()) {
                            val transcript = result.alternativesList[0].transcript
                            if (transcript.isNotBlank()) {
                                finalTranscript.append(transcript).append(" ")
                            }
                            println("ParakeetApiClient: Final transcript chunk: \"$transcript\"")
                        }
                    }
                }

                override fun onError(t: Throwable) {
                    println("ParakeetApiClient: gRPC error: ${t.message}")
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            TranscriptionException("gRPC call failed: ${t.message}", t)
                        )
                    }
                }

                override fun onCompleted() {
                    val result = finalTranscript.toString().trim()
                    println("ParakeetApiClient: Transcription completed: \"$result\"")
                    if (continuation.isActive) {
                        continuation.resume(TranscriptionResult(text = result))
                    }
                }
            }

            try {
                // Open the streaming call
                val requestObserver = stub
                    .withDeadlineAfter(30, TimeUnit.SECONDS)
                    .streamingRecognize(responseObserver)

                // Step 1: Send the config (first message must contain only config)
                val config = RecognitionConfig.newBuilder()
                    .setEncoding(AudioEncoding.LINEAR_PCM)
                    .setSampleRateHertz(16000)
                    .setLanguageCode(languageCode)
                    .setMaxAlternatives(1)
                    .setEnableAutomaticPunctuation(true)
                    .build()

                val streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(config)
                    .setInterimResults(false) // Only need final results
                    .build()

                val configRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingConfig)
                    .build()

                requestObserver.onNext(configRequest)

                // Step 2: Send audio data in chunks
                var offset = 0
                while (offset < pcmBytes.size) {
                    val end = minOf(offset + CHUNK_SIZE, pcmBytes.size)
                    val chunk = pcmBytes.copyOfRange(offset, end)

                    val audioRequest = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(chunk))
                        .build()

                    requestObserver.onNext(audioRequest)
                    offset = end
                }

                // Step 3: Signal that we're done sending audio
                requestObserver.onCompleted()

                // Cancel gRPC call if coroutine is cancelled
                continuation.invokeOnCancellation {
                    requestObserver.onError(
                        io.grpc.StatusRuntimeException(io.grpc.Status.CANCELLED)
                    )
                }

            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        TranscriptionException("Failed to stream audio: ${e.message}", e)
                    )
                }
            }
        }
    }

    /**
     * Releases the gRPC channel resources.
     */
    fun release() {
        try {
            channel?.shutdown()?.awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            channel?.shutdownNow()
        }
        channel = null
        asyncStub = null
    }
}

/**
 * Result of a speech-to-text transcription.
 */
data class TranscriptionResult(
    val text: String
)

/**
 * Exception thrown when transcription fails.
 */
class TranscriptionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
