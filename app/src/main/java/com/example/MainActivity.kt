package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.PreferencesManager
import com.example.data.local.SquadDoDatabase
import com.example.data.remote.PeerSimulatorEngine
import com.example.data.remote.SupabaseRealtimeClient
import com.example.data.remote.SupabaseRestClient
import com.example.data.repository.SquadRepository
import com.example.ui.SquadViewModel
import com.example.ui.SquadViewModelFactory
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SquadViewModel by viewModels {
        val database = SquadDoDatabase.getDatabase(applicationContext)
        val prefs = PreferencesManager(applicationContext)
        val realtimeClient = SupabaseRealtimeClient()
        val restClient = SupabaseRestClient()
        val simulator = PeerSimulatorEngine(database.dao(), realtimeClient)
        val repository = SquadRepository(
            dao = database.dao(),
            preferencesManager = prefs,
            realtimeClient = realtimeClient,
            restClient = restClient,
            simulatorEngine = simulator
        )
        SquadViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}
