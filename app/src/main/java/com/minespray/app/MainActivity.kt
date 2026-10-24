package com.minespray.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.minespray.app.ui.screens.*
import com.minespray.app.ui.theme.*
import com.minespray.app.viewmodel.MineViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val vm: MineViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("minespray", MODE_PRIVATE)
        vm.setLang(prefs.getString("lang", "en") ?: "en")
        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            LaunchedEffect(settings.lang) { prefs.edit().putString("lang", settings.lang).apply() }
            val base = LocalContext.current
            val cfg = remember(settings.lang) {
                Configuration(base.resources.configuration).also { val l = Locale(settings.lang); it.setLocale(l); it.setLayoutDirection(l) }
            }
            val ctx = remember(cfg) { base.createConfigurationContext(cfg) }
            CompositionLocalProvider(
                LocalContext provides ctx, LocalConfiguration provides cfg,
                LocalLayoutDirection provides if (settings.lang == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) { MineSprayTheme { Surface(color = Bg) { App(vm) } } }
        }
    }
}

@Composable
fun App(vm: MineViewModel) {
    val nav = rememberNavController()
    val s by vm.state.collectAsStateWithLifecycle()
    val set by vm.settings.collectAsStateWithLifecycle()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(Triple("overview", R.string.nav_overview, Icons.Default.Home), Triple("map", R.string.nav_map, Icons.Default.Place),
        Triple("zones", R.string.nav_zones, Icons.Default.List), Triple("fleet", R.string.nav_fleet, Icons.Default.Menu), Triple("control", R.string.nav_control, Icons.Default.PlayArrow))
    Scaffold(containerColor = Bg, bottomBar = {
        if (route != "splash") NavigationBar(containerColor = Surf) {
            tabs.forEach { (r, l, i) -> NavigationBarItem(route == r, { nav.navigate(r) { popUpTo("overview"); launchSingleTop = true } }, { Icon(i, null) }, label = { Text(stringResource(l)) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = androidx.compose.ui.graphics.Color.Black, indicatorColor = Amber)) }
        }
    }) { pad ->
        NavHost(nav, "splash", Modifier.padding(pad)) {
            composable("splash") { SplashScreen { nav.navigate("overview") { popUpTo("splash") { inclusive = true } } } }
            composable("overview") { DashboardScreen(s, { nav.navigate("zone/$it") }, { nav.navigate("control") }, { nav.navigate("settings") }) }
            composable("map") { MapScreen(s) { nav.navigate("zone/$it") } }
            composable("zones") { ZonesScreen(s) { nav.navigate("zone/$it") } }
            composable("zone/{id}") { ZoneDetailScreen(s, it.arguments?.getString("id")!!.toInt()) { nav.popBackStack() } }
            composable("fleet") { FleetScreen(s) }
            composable("control") { ControlScreen(s, vm) }
            composable("settings") { SettingsScreen(s, set, vm) }
        }
    }
}
