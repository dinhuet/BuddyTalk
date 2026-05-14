package com.example.buddytalk.data.speech

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

/**
 * Records audio from the microphone and saves it as a 16-bit mono WAV file at 16kHz.
 * This format is required by NVIDIA Parakeet-CTC ASR model.
 */
class AudioRecorder {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    @Volatile
    private var isRecording = false

    /**
     * Starts recording audio and writes PCM data to a temporary WAV file.
     * This function is suspendable and will keep recording until [stopRecording] is called.
     *
     * @param outputFile The file to write the WAV data to.
     */
    @SuppressLint("MissingPermission")
    suspend fun startRecording(outputFile: File) = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw IllegalStateException("Unable to get valid buffer size for AudioRecord")
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize * 2
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            throw IllegalStateException("AudioRecord failed to initialize")
        }

        isRecording = true
        audioRecord?.startRecording()

        val buffer = ByteArray(bufferSize)
        var totalBytesWritten = 0L

        FileOutputStream(outputFile).use { fos ->
            // Write placeholder WAV header (44 bytes), will be updated later
            writeWavHeader(fos, 0)

            while (isRecording && isActive) {
                val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (bytesRead > 0) {
                    fos.write(buffer, 0, bytesRead)
                    totalBytesWritten += bytesRead
                }
            }
        }

        // Update the WAV header with actual data size
        updateWavHeader(outputFile, totalBytesWritten)

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    /**
     * Stops the current recording session.
     */
    fun stopRecording() {
        isRecording = false
    }

    /**
     * Writes a standard WAV file header for 16-bit mono PCM at 16kHz.
     */
    private fun writeWavHeader(outputStream: FileOutputStream, dataSize: Long) {
        val totalDataLen = dataSize + 36
        val byteRate = SAMPLE_RATE * 1 * 16 / 8 // sampleRate * channels * bitsPerSample / 8
        val blockAlign = 1 * 16 / 8 // channels * bitsPerSample / 8

        val header = ByteArray(44)

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        writeIntLE(header, 4, totalDataLen.toInt())
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt sub-chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        writeIntLE(header, 16, 16) // Sub-chunk size (16 for PCM)
        writeShortLE(header, 20, 1) // Audio format (1 = PCM)
        writeShortLE(header, 22, 1) // Number of channels (1 = Mono)
        writeIntLE(header, 24, SAMPLE_RATE) // Sample rate
        writeIntLE(header, 28, byteRate) // Byte rate
        writeShortLE(header, 32, blockAlign) // Block align
        writeShortLE(header, 34, 16) // Bits per sample

        // data sub-chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeIntLE(header, 40, dataSize.toInt())

        outputStream.write(header)
    }

    /**
     * Updates the WAV header with the actual data length after recording is complete.
     */
    private fun updateWavHeader(file: File, dataSize: Long) {
        RandomAccessFile(file, "rw").use { raf ->
            val totalDataLen = dataSize + 36
            // Update RIFF chunk size at offset 4
            raf.seek(4)
            raf.write(intToByteArrayLE(totalDataLen.toInt()))
            // Update data sub-chunk size at offset 40
            raf.seek(40)
            raf.write(intToByteArrayLE(dataSize.toInt()))
        }
    }

    private fun writeIntLE(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xFF).toByte()
        array[offset + 1] = ((value shr 8) and 0xFF).toByte()
        array[offset + 2] = ((value shr 16) and 0xFF).toByte()
        array[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xFF).toByte()
        array[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    private fun intToByteArrayLE(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
}
