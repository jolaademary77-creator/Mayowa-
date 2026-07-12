package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.CropDatabase
import com.example.data.CropRepository
import com.example.ui.CropApp
import com.example.ui.CropViewModel
import com.example.ui.CropViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = CropDatabase.getDatabase(this)
    val dao = database.cropDiagnosisDao()
    val repository = CropRepository(dao)
    val factory = CropViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[CropViewModel::class.java]

    setContent {
      CropApp(viewModel = viewModel)
    }
  }
}
