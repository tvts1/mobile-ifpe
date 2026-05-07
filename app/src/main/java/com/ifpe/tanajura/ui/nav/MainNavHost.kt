package com.ifpe.tanajura.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ifpe.tanajura.HomePage
import com.ifpe.tanajura.ListPage
import com.ifpe.tanajura.MapPage
import com.ifpe.tanajura.viewmodel.MainViewModel

@Composable
fun MainNavHost(navController: NavHostController,
                modifier: Modifier = Modifier,
                viewModel: MainViewModel
) {
    NavHost(navController, startDestination = Route.Home) {
        composable<Route.Home> { HomePage(modifier = modifier, viewModel) }
        composable<Route.List> { ListPage(modifier = modifier, viewModel) }
        composable<Route.Map> { MapPage(modifier = modifier, viewModel) }
    }
}