package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CropDiagnosis::class], version = 1, exportSchema = false)
abstract class CropDatabase : RoomDatabase() {
    abstract fun cropDiagnosisDao(): CropDiagnosisDao

    companion object {
        @Volatile
        private var INSTANCE: CropDatabase? = null

        fun getDatabase(context: Context): CropDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CropDatabase::class.java,
                    "crop_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
