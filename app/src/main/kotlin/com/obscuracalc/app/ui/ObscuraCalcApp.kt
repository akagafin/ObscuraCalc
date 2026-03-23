package com.obscuracalc.app.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.view.WindowManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.obscuracalc.app.AppContainer
import com.obscuracalc.core.security.model.LockReason
import com.obscuracalc.core.security.model.SecuritySettings
import com.obscuracalc.feature.calculator.CalculatorScreen
import com.obscuracalc.feature.calculator.rememberCalculatorController
import com.obscuracalc.feature.converter.ConverterScreen
import com.obscuracalc.feature.converter.rememberConverterController
import com.obscuracalc.feature.legal.LegalDocumentScreen
import com.obscuracalc.feature.settings.LegalDocDestination
import com.obscuracalc.feature.settings.SettingsScreen
import com.obscuracalc.feature.vault.VaultAuthScreen
import com.obscuracalc.feature.vault.VaultHomeScreen
import com.obscuracalc.feature.vault.VaultSetupScreen
import com.obscuracalc.feature.vault.rememberVaultHomeController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class Destination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    data object Calculator : Destination("calculator", "Calculator", Icons.Outlined.Calculate)
    data object Converter : Destination("converter", "Converter", Icons.Outlined.SwapHoriz)
    data object Settings : Destination("settings", "Settings", Icons.Outlined.MoreVert)
    data object Vault : Destination("vault", "History", Icons.Outlined.History) // Disguised as History
    data object Auth : Destination("auth", "Auth", Icons.Outlined.History)
    data object Setup : Destination("setup", "Setup", Icons.Outlined.History)
    data object Legal : Destination("legal/{doc}", "Legal", Icons.Outlined.Description)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ObscuraCalcApp(container: AppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val calculatorController = rememberCalculatorController()
    val converterController = rememberConverterController()
    val vaultController =
        rememberVaultHomeController(container.vaultRepository, container.backupService)
    val sessionState by container.authManager.sessionState.collectAsState()
    var securitySettings by remember { mutableStateOf(SecuritySettings()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(sessionState.isConfigured, sessionState.isUnlocked) {
        securitySettings = container.authManager.currentSettings()
        if (!sessionState.isUnlocked && currentRoute == Destination.Vault.route) {
            navController.navigate(Destination.Calculator.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    AppLockEffects(container = container)
    WindowPrivacyEffects(
        shouldSecureWindow = sessionState.isUnlocked ||
                currentRoute == Destination.Auth.route ||
                currentRoute == Destination.Setup.route,
    )

    val bottomDestinations = buildList {
        add(Destination.Calculator)
        add(Destination.Converter)
        if (sessionState.isUnlocked) {
            add(Destination.Vault)
        }
        add(Destination.Settings)
    }

    BoxWithConstraints {
        val useRail = maxWidth >= 700.dp
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!useRail && currentRoute in bottomDestinations.map { it.route }) {
                    NavigationBar {
                        bottomDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        destination.icon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) {
                    NavigationRail {
                        bottomDestinations.forEach { destination ->
                            NavigationRailItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        destination.icon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                }
                NavHost(
                    navController = navController,
                    startDestination = Destination.Calculator.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    composable(Destination.Calculator.route) {
                        CalculatorScreen(
                            controller = calculatorController,
                            onEvaluateExpression = { expression ->
                                scope.launch {
                                    if (sessionState.isConfigured && !sessionState.isUnlocked &&
                                        container.authManager.matchesHiddenTrigger(expression.toCharArray())
                                    ) {
                                        navController.navigate(Destination.Auth.route)
                                    }
                                }
                            },
                            onNavigateToSettings = { navController.navigate(Destination.Settings.route) },
                            onNavigateToConverter = { navController.navigate(Destination.Converter.route) },
                            onNavigateToVault = {
                                if (sessionState.isUnlocked) {
                                    navController.navigate(Destination.Vault.route)
                                } else if (sessionState.isConfigured) {
                                    navController.navigate(Destination.Auth.route)
                                } else {
                                    navController.navigate(Destination.Setup.route)
                                }
                            }
                        )
                    }
                    composable(Destination.Converter.route) {
                        ConverterScreen(controller = converterController)
                    }
                    composable(Destination.Settings.route) {
                        SettingsScreen(
                            securitySettings = securitySettings,
                            vaultConfigured = sessionState.isConfigured,
                            vaultUnlocked = sessionState.isUnlocked,
                            appVersion = "0.1.0",
                            onOpenLegalDoc = { doc ->
                                navController.navigate("legal/${doc.name}")
                            },
                            onVersionTapThresholdReached = {
                                navController.navigate(Destination.Setup.route)
                            },
                            onUpdateLockTimeoutSeconds = { seconds ->
                                scope.launch {
                                    container.authManager.updateSettings { current ->
                                        current.copy(lockTimeoutSeconds = seconds)
                                    }
                                    securitySettings = container.authManager.currentSettings()
                                }
                            },
                            onUpdateWipeAfterFailures = { failures ->
                                scope.launch {
                                    container.authManager.updateSettings { current ->
                                        current.copy(wipeAfterFailures = failures)
                                    }
                                    securitySettings = container.authManager.currentSettings()
                                }
                            },
                            onUpdateDecoyMode = { enabled ->
                                scope.launch {
                                    container.authManager.updateSettings { current ->
                                        current.copy(decoyModeEnabled = enabled)
                                    }
                                    securitySettings = container.authManager.currentSettings()
                                }
                            },
                            onLockVault = {
                                scope.launch {
                                    container.authManager.lock(LockReason.EXPLICIT)
                                }
                            },
                        )
                    }
                    composable(Destination.Auth.route) {
                        VaultAuthScreen(
                            authManager = container.authManager,
                            onAuthenticated = {
                                navController.navigate(Destination.Vault.route) {
                                    popUpTo(Destination.Auth.route) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(Destination.Setup.route) {
                        VaultSetupScreen(
                            authManager = container.authManager,
                            onSetupComplete = {
                                navController.navigate(Destination.Vault.route) {
                                    popUpTo(Destination.Setup.route) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(Destination.Vault.route) {
                        VaultHomeScreen(
                            controller = vaultController,
                            onLockVault = {
                                scope.launch {
                                    container.authManager.lock(LockReason.EXPLICIT)
                                    navController.navigate(Destination.Calculator.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            },
                        )
                    }
                    composable(
                        route = Destination.Legal.route,
                        arguments = listOf(navArgument("doc") { type = NavType.StringType }),
                    ) { backStackEntry ->
                        val doc = LegalDocDestination.valueOf(
                            backStackEntry.arguments?.getString("doc") ?: "TERMS"
                        )
                        LegalDocumentScreen(title = doc.title, assetPath = doc.assetPath)
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowPrivacyEffects(shouldSecureWindow: Boolean) {
    val context = LocalContext.current
    DisposableEffect(context, shouldSecureWindow) {
        val window = context.findActivity()?.window
        if (shouldSecureWindow) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (shouldSecureWindow) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

@Composable
private fun AppLockEffects(container: AppContainer) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingLockJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                pendingLockJob?.cancel()
                pendingLockJob = null
            }

            override fun onStop(owner: LifecycleOwner) {
                scope.launch {
                    val timeout = container.authManager.currentSettings().lockTimeoutSeconds
                    pendingLockJob?.cancel()
                    pendingLockJob = if (timeout <= 0) {
                        launch { container.authManager.lock(LockReason.APP_BACKGROUND) }
                    } else {
                        launch {
                            delay(timeout * 1000L)
                            container.authManager.lock(LockReason.APP_BACKGROUND)
                        }
                    }
                }
            }
        }
        val processLifecycle = ProcessLifecycleOwner.get().lifecycle
        processLifecycle.addObserver(lifecycleObserver)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    scope.launch {
                        container.authManager.lock(LockReason.SCREEN_OFF)
                    }
                }
            }
        }

        // Menggunakan ContextCompat untuk registrasi receiver agar tidak deprecated
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            pendingLockJob?.cancel()
            processLifecycle.removeObserver(lifecycleObserver)
            context.unregisterReceiver(receiver)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
