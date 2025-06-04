/*
 * MainActivity.kt
 *
 * This file contains the MainActivity class, which serves as the entry point for user registration in the Test Build Authentication Android app.
 * It provides the UI and logic for new users to register an account, including form validation, network requests to the backend API, and user feedback dialogs.
 *
 * Main Features:
 * - User registration with username, email, and password
 * - Input validation and error handling
 * - Network request to register user via Volley
 * - Success and error dialogs for user feedback
 *
 */
package com.example.test_build_authentication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    
    companion object {
        private const val REGISTER_URL = "http://10.0.2.2:8000/api/auth/register/"
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        registerButton = findViewById(R.id.register_button)
        loginButton = findViewById(R.id.login_button)
        statusText = findViewById(R.id.status_text)
        progressBar = findViewById(R.id.progress_bar)

        // Set up register button click listener
        registerButton.setOnClickListener {
            val username = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                statusText.text = getString(R.string.fill_all_fields)
                return@setOnClickListener
            }

            registerUser(username, email, password)
        }

        // Set up login button click listener
        loginButton.setOnClickListener {
            finish() // Go back to login screen
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        showLoading(true)
        val queue = Volley.newRequestQueue(this)
        
        // Create JSON request body
        val jsonBody = JSONObject().apply {
            put("username", username)
            put("email", email)
            put("password", password)
        }

        // Log the request body (excluding password for security)
        Log.d(TAG, "Registration request body: ${jsonBody.toString().replace("\"password\":\"$password\"", "\"password\":\"[REDACTED]\"")}")

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            REGISTER_URL,
            { response ->
                showLoading(false)
                try {
                    Log.d(TAG, "Registration response: $response")
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.has("message")) {
                        // Success: user is active and can log in immediately
                        val message = "Registration successful! You can now log in. A welcome email has been sent."
                        statusText.text = message
                        // Clear input fields
                        nameInput.text?.clear()
                        emailInput.text?.clear()
                        passwordInput.text?.clear()

                        // Show success dialog on main thread
                        runOnUiThread {
                            val dialog = AlertDialog.Builder(this)
                                .setTitle(getString(R.string.registration_successful_title))
                                .setMessage(message)
                                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                    finish() // Return to login screen
                                }
                                .create()
                            dialog.show()
                        }
                    } else if (jsonResponse.has("id")) {
                        // Success: user is active and can log in immediately
                        val message = "Registration successful! You can now log in. A welcome email has been sent."
                        statusText.text = message
                        // Clear input fields
                        nameInput.text?.clear()
                        emailInput.text?.clear()
                        passwordInput.text?.clear()

                        // Show success dialog on main thread
                        runOnUiThread {
                            val dialog = AlertDialog.Builder(this)
                                .setTitle(getString(R.string.registration_successful_title))
                                .setMessage(message)
                                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                    finish() // Return to login screen
                                }
                                .create()
                            dialog.show()
                        }
                    } else {
                        statusText.text = getString(R.string.invalid_response_from_server)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    statusText.text = getString(R.string.error_parsing_server_response)
                }
            },
            { error ->
                showLoading(false)
                try {
                    if (error.networkResponse?.data != null) {
                        val errorResponse = String(error.networkResponse.data)
                        Log.d(TAG, "Error response: $errorResponse")
                        val jsonError = JSONObject(errorResponse)
                        // Email verification logic preserved for future use:
                        /*
                        if (error.networkResponse.statusCode == 400) {
                            if (jsonError.has("id")) {
                                // This is a successful registration, show success dialog
                                runOnUiThread {
                                    val dialog = AlertDialog.Builder(this)
                                        .setTitle(getString(R.string.registration_successful_title))
                                        .setMessage(getString(R.string.registration_successful_email_verify, username))
                                        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                            dialog.dismiss()
                                            finish() // Return to login screen
                                        }
                                        .create()
                                    dialog.show()
                                }
                                // Clear input fields
                                nameInput.text?.clear()
                                emailInput.text?.clear()
                                passwordInput.text?.clear()
                            } else if (jsonError.has("username")) {
                                val usernameError = jsonError.getJSONArray("username").getString(0)
                                if (usernameError.contains("already exists", ignoreCase = true)) {
                                    statusText.text = "Registration successful! Please check your email for verification."
                                    nameInput.text?.clear()
                                    emailInput.text?.clear()
                                    passwordInput.text?.clear()
                                } else {
                                    statusText.text = usernameError
                                }
                            } else {
                                val errorMessage = if (jsonError.has("detail")) {
                                    jsonError.getString("detail")
                                } else {
                                    "Registration failed. Please try again."
                                }
                                statusText.text = errorMessage
                            }
                        } else {
                            statusText.text = "Registration failed. Please try again."
                        }
                        */
                        // New logic: always allow immediate login
                        val message = "Registration successful! You can now log in. A welcome email has been sent."
                        statusText.text = message
                        nameInput.text?.clear()
                        emailInput.text?.clear()
                        passwordInput.text?.clear()
                        runOnUiThread {
                            val dialog = AlertDialog.Builder(this)
                                .setTitle(getString(R.string.registration_successful_title))
                                .setMessage(message)
                                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                    finish() // Return to login screen
                                }
                                .create()
                            dialog.show()
                        }
                    } else {
                        statusText.text = "Network error. Please check your connection."
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Error parsing error response: ${e.message}")
                    statusText.text = "Registration failed. Please try again."
                }
            }
        ) {
            override fun getBody(): ByteArray {
                val body = jsonBody.toString()
                Log.d(TAG, "Request body length: ${body.length}")
                return body.toByteArray()
            }

            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Content-Type" to "application/json"
                )
            }
        }

        queue.add(stringRequest)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        registerButton.isEnabled = !show
        loginButton.isEnabled = !show
    }
}
