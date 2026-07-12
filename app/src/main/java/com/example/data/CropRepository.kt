package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class CropRepository(private val cropDiagnosisDao: CropDiagnosisDao) {

    val allDiagnoses: Flow<List<CropDiagnosis>> = cropDiagnosisDao.getAllDiagnoses()

    suspend fun insert(diagnosis: CropDiagnosis): Long = withContext(Dispatchers.IO) {
        cropDiagnosisDao.insertDiagnosis(diagnosis)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        cropDiagnosisDao.deleteById(id)
    }

    suspend fun update(diagnosis: CropDiagnosis) = withContext(Dispatchers.IO) {
        cropDiagnosisDao.updateDiagnosis(diagnosis)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        cropDiagnosisDao.clearAll()
    }

    // Direct REST API client using OkHttp for reliability and timeout settings
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Identifies illness and cure from an image bitmap.
     * Uses Gemini 3.5-flash for fast and accurate multimodal crop diagnosis.
     */
    suspend fun diagnoseCropWithAI(
        imageBitmap: Bitmap,
        customNotes: String = "",
        isVideoScan: Boolean = false
    ): CropDiagnosis? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("CropRepository", "Gemini API Key is empty or placeholder!")
            return@withContext null
        }

        // Convert Bitmap to base64
        val base64Image = bitmapToBase64(imageBitmap)

        // Construct Gemini JSON request body manually using org.json for absolute safety
        try {
            val systemInstruction = "You are an expert plant pathologist and agricultural advisor. " +
                    "Analyze the provided image of a crop plant. " +
                    "Identify the plant/crop type, the disease or illness if any (state 'Healthy' if no illness is found), " +
                    "the confidence percentage (0-100), the disease severity level ('Low', 'Medium', 'High'), " +
                    "and a detailed, actionable organic and chemical cure/remedy guide (numbered list). " +
                    "You MUST reply with ONLY a single valid JSON object. Do not include markdown code block syntax (like ```json). " +
                    "The JSON object must strictly match this schema: " +
                    "{\"cropName\": \"Crop Name\", \"illness\": \"Disease Name or Healthy\", \"severity\": \"Low/Medium/High\", \"confidence\": 95.0, \"cure\": \"1. Action... 2. Action...\"}"

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObject = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Part 1: Text prompt
                            put(JSONObject().apply {
                                put("text", "Please analyze this crop image. Farmer notes: $customNotes. Respond in the requested JSON format.")
                            })
                            // Part 2: Multimodal Inline Image Data
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObject)
                }
                put("contents", contentsArray)

                // Add systemInstruction to guide the model safely
                val systemInstructionObj = JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    }
                    put("parts", partsArr)
                }
                put("systemInstruction", systemInstructionObj)

                // Request strict JSON response format
                val generationConfig = JSONObject().apply {
                    val responseFormat = JSONObject().apply {
                        put("type", "OBJECT") // Standard structured json parameter
                        put("responseMimeType", "application/json")
                    }
                    put("responseFormat", responseFormat)
                    put("temperature", 0.2) // Low temperature for high precision and consistent output
                }
                put("generationConfig", generationConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBodyString.isEmpty()) {
                Log.e("CropRepository", "API Call failed. Code: ${response.code}, Body: $responseBodyString")
                return@withContext null
            }

            // Parse response JSON to find candidate text
            val rootJson = JSONObject(responseBodyString)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                Log.e("CropRepository", "No candidates found in Gemini response!")
                return@withContext null
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            if (parts.length() == 0) {
                Log.e("CropRepository", "No parts found in candidate content!")
                return@withContext null
            }

            val textResultRaw = parts.getJSONObject(0).getString("text").trim()
            Log.d("CropRepository", "Raw result from Gemini: $textResultRaw")

            // Parse structured JSON block from Gemini
            val cleanJsonString = extractJsonContent(textResultRaw)
            val resultJson = JSONObject(cleanJsonString)

            val cropName = resultJson.optString("cropName", "Unknown Crop")
            val illness = resultJson.optString("illness", "Healthy")
            val severity = resultJson.optString("severity", "Low")
            val confidenceObj = resultJson.opt("confidence")
            val confidence = when (confidenceObj) {
                is Double -> confidenceObj.toFloat()
                is Int -> confidenceObj.toFloat()
                else -> 90f
            }
            val cure = resultJson.optString("cure", "Ensure proper crop maintenance and regular inspection.")

            // Create and return complete crop diagnosis object
            val finalDiagnosis = CropDiagnosis(
                cropName = cropName,
                illness = illness,
                cure = cure,
                confidence = confidence,
                severity = severity,
                notes = customNotes,
                isVideo = isVideoScan
            )

            // Save automatically to history database for farmer persistence
            val savedId = insert(finalDiagnosis)
            return@withContext finalDiagnosis.copy(id = savedId.toInt())

        } catch (e: Exception) {
            Log.e("CropRepository", "Error during AI crop diagnosis: ", e)
            return@withContext null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compress bitmap slightly to save bandwidth while keeping crop disease patterns recognizable
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractJsonContent(rawText: String): String {
        // Strip markdown code block wrappers if model ignores instruction and adds them
        var temp = rawText
        if (temp.startsWith("```json")) {
            temp = temp.substring(7)
        } else if (temp.startsWith("```")) {
            temp = temp.substring(3)
        }
        if (temp.endsWith("```")) {
            temp = temp.substring(0, temp.length - 3)
        }
        return temp.trim()
    }
}
