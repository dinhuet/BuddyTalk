package com.example.buddytalk.data.speech

import com.example.buddytalk.BuildConfig

/**
 * Configuration for the NVIDIA Parakeet-CTC-0.6B-Vi cloud API.
 *
 * This connects directly to NVIDIA's cloud inference endpoint (build.nvidia.com)
 * via gRPC — no self-hosted server or GPU required!
 *
 * HOW TO SET YOUR API KEY:
 * 1. Go to https://build.nvidia.com
 * 2. Sign up / Log in with a free NVIDIA account
 * 3. Search for "parakeet-ctc-0.6b-vi"
 * 4. Click "Get API Key" → copy the key
 * 5. Open local.properties (project root) and add:
 *      NVIDIA_API_KEY=your-key-here
 *
 * The key is read from local.properties at build time and injected
 * into BuildConfig. local.properties is gitignored so your key stays safe.
 */
object ParakeetConfig {

    /**
     * gRPC server host for NVIDIA cloud inference.
     */
    const val GRPC_HOST = "grpc.nvcf.nvidia.com"

    /**
     * gRPC server port (443 = TLS encrypted).
     */
    const val GRPC_PORT = 443

    /**
     * NVIDIA API key — read from BuildConfig (injected from local.properties).
     */
    val API_KEY: String = BuildConfig.NVIDIA_API_KEY

    /**
     * Function ID for parakeet-ctc-0.6b-vi on NVIDIA cloud.
     */
    const val FUNCTION_ID = "f3dff2bb-99f9-403d-a5f1-f574a757deb0"

    /**
     * Language code for Vietnamese speech recognition.
     */
    const val LANGUAGE_CODE = "vi-VN"
}
