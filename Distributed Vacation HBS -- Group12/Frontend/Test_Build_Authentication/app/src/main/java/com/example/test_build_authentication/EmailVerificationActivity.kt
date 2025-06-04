package com.example.test_build_authentication

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

/*
 * EmailVerificationActivity.kt
 *
 * Handles the email verification process for new users after registration.
 * Manages UI and logic for verifying user email addresses.
 */

class EmailVerificationActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "EmailVerificationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Check if the URL matches the verification endpoint structure
                if (url?.contains("${Config.BASE_URL}/auth/verify/") == true) {
                    // Extract the token from the URL
                    val parts = url.split("${Config.BASE_URL}/auth/verify/")
                    if (parts.size == 2) {
                        val token = parts[1].trimEnd('/')
                        if (token.isNotEmpty()) {
                            verifyEmail(token)
                            return true
                        }
                    }
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // After the page finishes loading, check if the URL is the verification endpoint
                if (url?.contains("${Config.BASE_URL}/auth/verify/") == true) {
                    // Extract the token from the URL
                    val parts = url.split("${Config.BASE_URL}/auth/verify/")
                    if (parts.size == 2) {
                        val token = parts[1].trimEnd('/')
                        if (token.isNotEmpty()) {
                            verifyEmail(token)
                        }
                    }
                }
            }
        }

        // Load the verification URL
        val verificationUrl = intent.getStringExtra("verification_url")
        if (verificationUrl != null) {
            webView.loadUrl(verificationUrl)
        } else {
            Log.e(TAG, "Verification URL is null")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun verifyEmail(token: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonBody = JSONObject().apply {
            put("token", token)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            "${Config.BASE_URL}/auth/verify/",
            jsonBody,
            { response ->
                Log.d(TAG, "Verification response: $response")
                if (response.has("message")) {
                    // Show success message
                    setResult(RESULT_OK)
                    finish()
                }
            },
            { error ->
                Log.e(TAG, "Verification error: ${error.message}")
                try {
                    val errorResponse = String(error.networkResponse.data)
                    val jsonError = JSONObject(errorResponse)
                    if (jsonError.has("error")) {
                        jsonError.getString("error")
                    } else {
                        "Registration failed. Please try again."
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing error response: ${e.message}")
                    "Registration failed. Please try again."
                }
                setResult(RESULT_CANCELED)
                finish()
            }
        )

        queue.add(jsonObjectRequest)
    }
} 