/*
 * ApiClient.kt (api)
 *
 * Provides network client setup and utility functions for API calls in the api package.
 */
package com.example.test_build_authentication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import org.bson.types.ObjectId
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import java.lang.reflect.Type
import android.content.Context
import com.example.test_build_authentication.Config

object ApiClient {
    private const val TAG = "ApiClient"
    private val BASE_URL = Config.LISTINGS_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, "API Response Raw: $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d(TAG, """
                API Request:
                - URL: ${request.url}
                - Method: ${request.method}
                - Headers: ${request.headers}
            """.trimIndent())
            chain.proceed(request)
        }
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(ObjectId::class.java, ObjectIdTypeAdapter())
        .registerTypeAdapter(String::class.java, object : JsonDeserializer<String> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): String? {
                Log.d(TAG, "Deserializing string value: $json")
                return when {
                    json.isJsonNull -> null
                    json.isJsonPrimitive -> json.asString
                    json.isJsonObject -> {
                        val obj = json.asJsonObject
                        if (obj.has("\$oid")) {
                            obj.get("\$oid").asString
                        } else if (obj.has("\$string")) {
                            obj.get("\$string").asString
                        } else if (obj.size() == 1) {
                            obj.entrySet().first().value.asString
                        } else {
                            null
                        }
                    }
                    else -> {
                        Log.w(TAG, "Unexpected JSON type for string: ${json.javaClass.simpleName}")
                        json.toString()
                    }
                }
            }
        })
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val vacationHomeService: VacationHomeService by lazy {
        retrofit.create(VacationHomeService::class.java)
    }

    // Singleton instance
    private var instance: ApiClient? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = ApiClient
        }
    }

    fun getInstance(): ApiClient {
        return instance ?: throw IllegalStateException("ApiClient not initialized")
    }

    private class ObjectIdTypeAdapter : TypeAdapter<ObjectId>() {
        override fun write(out: JsonWriter, value: ObjectId?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.toHexString())
            }
        }

        override fun read(in_: JsonReader): ObjectId? {
            try {
                val value = in_.nextString()
                Log.d(TAG, "Reading MongoDB ObjectId: $value")
                if (value.isNullOrEmpty()) {
                    Log.w(TAG, "Empty or null ObjectId value")
                    return null
                }
                return ObjectId(value)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading MongoDB ObjectId", e)
                return null
            }
        }
    }
}

