package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.ChannelSettingsManager
import com.example.data.repository.FtthSupportRepository
import com.example.ui.FtthSupportApp
import com.example.ui.SupportViewModel
import com.example.ui.SupportViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SupportViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val settingsManager = ChannelSettingsManager(applicationContext)
        val repository = FtthSupportRepository(
            database.ticketDao(),
            database.chatMessageDao(),
            settingsManager
        )
        SupportViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FtthSupportApp(viewModel = viewModel)
                }
            }
        }
    }
}
