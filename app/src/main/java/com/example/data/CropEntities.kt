package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_diagnoses")
data class CropDiagnosis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cropName: String,
    val illness: String,
    val cure: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: String, // "Low", "Medium", "High"
    val notes: String = "",
    val imageUri: String? = null,
    val isVideo: Boolean = false
)

data class PreloadedCrop(
    val id: String,
    val name: String,
    val typicalIllness: String,
    val description: String,
    val assetResId: Int, // Placeholder drawable or custom asset
    val localImageBase64: String? = null // Optional pre-coded small base64 image of disease for live API call
)
