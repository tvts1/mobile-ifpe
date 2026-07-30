package com.ifpe.tanajura

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.util.Consumer
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ifpe.tanajura.api.WeatherService
import com.ifpe.tanajura.db.fb.FBDatabase
import com.ifpe.tanajura.monitor.ForecastMonitor
import com.ifpe.tanajura.ui.nav.BottomNavBar
import com.ifpe.tanajura.ui.nav.BottomNavItem
import com.ifpe.tanajura.ui.nav.MainNavHost
import com.ifpe.tanajura.ui.nav.Route
import com.ifpe.tanajura.ui.theme.TanajuraTheme
import com.ifpe.tanajura.viewmodel.MainViewModel
import com.ifpe.tanajura.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fbDB = remember { FBDatabase() }
            val weatherService = remember { WeatherService(this@MainActivity) }
            val forecastMonitor = remember { ForecastMonitor(this@MainActivity) }
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    fbDB,
                    weatherService,
                    forecastMonitor
                )
            )
            var showDialog by remember { mutableStateOf(false) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState()
            val showButton = currentRoute.value?.destination?.hasRoute(Route.List::class) == true
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = {}
            )

            LaunchedEffect(Unit) {
                val permissions = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }

            DisposableEffect(Unit) {
                val handleIntent: (Intent) -> Unit = { receivedIntent ->
                    receivedIntent.getStringExtra("city")?.let { cityName ->
                        viewModel.city = cityName
                        viewModel.page = Route.Home
                    }
                }
                handleIntent(this@MainActivity.intent)

                val listener = Consumer<Intent> { receivedIntent ->
                    this@MainActivity.intent = receivedIntent
                    handleIntent(receivedIntent)
                }
                addOnNewIntentListener(listener)
                onDispose {
                    removeOnNewIntentListener(listener)
                }
            }

            TanajuraTheme {
                if (showDialog) CityDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { city ->
                        if (city.isNotBlank()) viewModel.addCity(city)
                        showDialog = false
                    })
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                val name = viewModel.user?.name ?: "[carregando...]"
                                Text("Bem-vindo/a! $name")
                            },
                            actions = {
                                IconButton(onClick = {
                                    Firebase.auth.signOut()
                                    this@MainActivity.openLoginScreen()
                                }) {
                                    Icon(
                                        imageVector =
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Localized description"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        val items = listOf(
                            BottomNavItem.HomeButton,
                            BottomNavItem.ListButton,
                            BottomNavItem.MapButton,
                        )
                        BottomNavBar(viewModel, navController, items)
                    },
                    floatingActionButton = {
                        if (showButton) {
                            FloatingActionButton(onClick = { showDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainNavHost(navController = navController, viewModel = viewModel)
                    }
                    LaunchedEffect(viewModel.page) {
                        navController.navigate(viewModel.page) {
                            navController.graph.startDestinationRoute?.let {
                                popUpTo(it) {
                                    saveState = true
                                }
                                restoreState = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}
