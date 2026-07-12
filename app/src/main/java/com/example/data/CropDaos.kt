package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDiagnosisDao {
    @Query("SELECT * FROM crop_diagnoses ORDER BY timestamp DESC")
    fun getAllDiagnoses(): Flow<List<CropDiagnosis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(diagnosis: CropDiagnosis): Long

    @Update
    suspend fun updateDiagnosis(diagnosis: CropDiagnosis)

    @Delete
    suspend fun deleteDiagnosis(diagnosis: CropDiagnosis)

    @Query("DELETE FROM crop_diagnoses WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM crop_diagnoses")
    suspend fun clearAll()
}
