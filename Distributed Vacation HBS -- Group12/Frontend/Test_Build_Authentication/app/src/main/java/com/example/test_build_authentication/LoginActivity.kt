package com.example.test_build_authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.VolleyError
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var guestLoginButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var resendVerificationButton: Button
    private lateinit var encryptedPrefs: EncryptedSharedPreferences
    private lateinit var emailInput: TextInputEditText
    private lateinit var aboutButton: Button

    companion object {
        private const val TAG = "LoginActivity"
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val UNVERIFIED_EMAIL_STATUS_CODE = 403 // Example status code
        private const val UNVERIFIED_EMAIL_MESSAGE_KEY = "detail" // Example JSON key for error message
        private const val UNVERIFIED_EMAIL_MESSAGE_VALUE = "Email is not verified" // Example error message content
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize encrypted preferences
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        try {
            encryptedPrefs = EncryptedSharedPreferences.create(
                PREFS_NAME,
                PREFS_NAME,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            // Catching general Exception for robustness, though InvalidKeyException is expected
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: ${e.message}")
            // Attempt to clear problematic preferences file and try again
            try {
                deleteFile("${PREFS_NAME}.xml")
                encryptedPrefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    PREFS_NAME,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ) as EncryptedSharedPreferences
                Log.d(TAG, "Successfully re-initialized EncryptedSharedPreferences after clearing.")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to re-initialize EncryptedSharedPreferences after clearing: ${e2.message}")
                // At this point, the app may not be able to function correctly without encrypted prefs.
                // Consider showing an error message to the user or exiting the app.
                 statusText.text = "Secure storage initialization failed. Please clear app data."
            }
        }

        // Initialize views
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)
        guestLoginButton = findViewById(R.id.guest_login_button)
        statusText = findViewById(R.id.status_text)
        progressBar = findViewById(R.id.progress_bar)
        resendVerificationButton = findViewById(R.id.resend_verification_button)
        emailInput = findViewById(R.id.email_input)
        aboutButton = findViewById(R.id.about_button)

        // Set up login button click listener
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                statusText.text = "Please fill in all fields"
                return@setOnClickListener
            }
            // Hide resend button on new login attempt
            resendVerificationButton.visibility = View.GONE
            loginUser(username, password)
        }

        // Set up register button click listener
        registerButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Set up guest login button click listener
        guestLoginButton.setOnClickListener {
            proceedAsGuest()
        }

        // Set up resend verification button click listener
        resendVerificationButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                resendVerificationEmail(email)
            } else {
                statusText.text = "Please enter your email to resend verification link."
            }
        }

        // Set up about button click listener
        aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loginUser(username: String, password: String) {
        showLoading(true)
        val queue = Volley.newRequestQueue(this)

        // Create JSON request body
        val jsonBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        // Log the request body
        Log.d(TAG, "Login request body: ${jsonBody.toString().replace("\"password\":\"$password\"", "\"password\":\"[REDACTED]\"")}")

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            Config.LOGIN_ENDPOINT,
            jsonBody,
            Response.Listener<JSONObject> {
                showLoading(false)
                try {
                    Log.d(TAG, "Login response: $it") // 'it' refers to the JSONObject response
                    if (it.has("access")) {
                        // Login successful
                        val token = it.getString("access")
                        // Store token securely
                        encryptedPrefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
                        // If userId is available in response, store it
                        if (it.has("user_id")) {
                            val userId = it.get("user_id").toString()
                            encryptedPrefs.edit().putString("user_id", userId).apply()
                        }
                        // Redirect to main/guest view
                        val intent = Intent(this, VacationHomeListActivity::class.java)
                        intent.putExtra("token", token)
                        intent.putExtra("isGuest", false)
                        startActivity(intent)
                        finish()
                    } else {
                        statusText.text = "Invalid response from server"
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    statusText.text = "Error parsing server response"
                } catch (e: Exception) {
                     Log.e(TAG, "Unexpected error in login response listener: ${e.message}")
                     statusText.text = "An unexpected error occurred."
                }
            },
            Response.ErrorListener { error ->
                showLoading(false)
                val errorMessage = when {
                    error.networkResponse == null -> {
                        Log.e(TAG, "Network error: ${error.message}")
                        "Network error. Please check your connection."
                    }
                    error.networkResponse.statusCode == UNVERIFIED_EMAIL_STATUS_CODE -> {
                         // Simplified error handling for unverified email
                         resendVerificationButton.visibility = View.VISIBLE
                         "Email not verified. Please check your inbox or resend verification email."
                    }
                    error.networkResponse.statusCode == 400 -> {
                         // Simplified error handling for 400 bad request
                         val errorResponse = error.networkResponse.data?.let { String(it) } ?: "Unknown error"
                         Log.e(TAG, "Server error response (400): $errorResponse")
                         "Invalid request. Please check your credentials."
                    }
                    error.networkResponse.statusCode == 401 -> "Invalid username or password"
                    error.networkResponse.statusCode == 404 -> "Server endpoint not found"
                    else -> {
                        Log.e(TAG, "Error connecting to server: ${error.networkResponse.statusCode}")
                        "Error connecting to server (${error.networkResponse.statusCode})"
                    }
                }
                statusText.text = errorMessage
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun resendVerificationEmail(email: String) {
        showLoading(true)
        resendVerificationButton.isEnabled = false // Prevent multiple clicks
        val queue = Volley.newRequestQueue(this)

        val jsonBody = JSONObject().apply {
            put("email", email)
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            Config.RESEND_VERIFICATION_ENDPOINT,
            jsonBody,
            Response.Listener<JSONObject> {
                showLoading(false)
                resendVerificationButton.isEnabled = true
                statusText.text = "Verification email sent. Please check your inbox."
                // Optionally, hide the resend button again or show a success state
                resendVerificationButton.visibility = View.GONE
                // You can optionally parse a success message from the response JSON here if needed
            },
            Response.ErrorListener { error ->
                showLoading(false)
                resendVerificationButton.isEnabled = true
                val errorMessage = when {
                    error.networkResponse == null -> {
                        Log.e(TAG, "Network error (resend): ${error.message}")
                        "Network error. Please check your connection."
                    }
                     error.networkResponse.statusCode == 400 -> {
                        // Simplified error handling for 400 bad request
                        val errorResponse = error.networkResponse.data?.let { String(it) } ?: "Unknown error"
                        Log.e(TAG, "Server error response (resend 400): $errorResponse")
                        "Invalid request for resend verification."
                     }
                    else -> {
                        Log.e(TAG, "Error resending verification email: ${error.message}")
                        "Failed to resend verification email. Please try again later."
                    }
                }
                statusText.text = errorMessage
                 // Optionally, keep the resend button visible on failure
                 resendVerificationButton.visibility = View.VISIBLE
            }
        ) {
             override fun getHeaders(): Map<String, String> {
                 val headers = HashMap<String, String>()
                 headers["Content-Type"] = "application/json"
                 return headers
             }
        }

        queue.add(jsonObjectRequest)
    }

    private fun proceedAsGuest() {
        // Navigate to the guest view activity
        val intent = Intent(this, GuestViewActivity::class.java)
        // intent.putExtra("isGuest", true) // You might want to pass a flag indicating it's a guest
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        loginButton.isEnabled = !show
        registerButton.isEnabled = !show
        guestLoginButton.isEnabled = !show
        // Control resend button enabled state separately
        if(!show) resendVerificationButton.isEnabled = true
    }

    private fun showAboutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_about, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }
}