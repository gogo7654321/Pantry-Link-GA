package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.PantryLinkRepository
import com.example.data.FirebaseSyncManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.PantryLinkAppScreen
import com.example.ui.PantryLinkViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: PantryLinkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase safely
        try {
            FirebaseApp.initializeApp(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Firebase failed to initialize on launch: ${e.message}")
        }
        
        // 1. Initialize Room Database and Repository
        // Passing lifecycleScope to support seed pre-population callbacks
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val syncManager = FirebaseSyncManager(database.dao(), lifecycleScope)
        val repository = PantryLinkRepository(database.dao(), syncManager)
        
        // 2. Instantiate our Unified ViewModel
        viewModel = PantryLinkViewModel(repository, applicationContext)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PantryLinkAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshSessionAndProfile()
        }
    }
}
