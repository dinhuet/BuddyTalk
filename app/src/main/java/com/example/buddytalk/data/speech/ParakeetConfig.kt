package com.example.buddytalk.data.speech

/**
 * Configuration for the NVIDIA Parakeet-CTC-0.6B-Vi cloud API.
 *
 * This connects directly to NVIDIA's cloud inference endpoint (build.nvidia.com)
 * via gRPC — no self-hosted server or GPU required!
 *
 * HOW TO GET YOUR API KEY:
 * 1. Go to https://build.nvidia.com
 * 2. Sign up / Log in with a free NVIDIA account
 * 3. Search for "parakeet-ctc-0.6b-vi"
 * 4. Click "Get API Key" → copy the key
 * 5. Paste it below in API_KEY
 */
object ParakeetConfig {

    /**
     * gRPC server host for NVIDIA cloud inference.
     * This is the standard endpoint for all NIM models on build.nvidia.com.
     */
    const val GRPC_HOST = "grpc.nvcf.nvidia.com"

    /**
     * gRPC server port (443 = TLS encrypted).
     */
    const val GRPC_PORT = 443

    /**
     * Your NVIDIA API key from build.nvidia.com.
     *
     * ⚠️ REQUIRED: Replace this with your actual API key!
     * Get it free at: https://build.nvidia.com → Get API Key
     */
    const val API_KEY = "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"

    /**
     * Function ID for parakeet-ctc-0.6b-vi on NVIDIA cloud.
     * This identifies the specific model to use for inference.
     */
    const val FUNCTION_ID = "f3dff2bb-99f9-403d-a5f1-f574a757deb0"

    /**
     * Language code for Vietnamese speech recognition.
     */
    const val LANGUAGE_CODE = "vi-VN"
}
