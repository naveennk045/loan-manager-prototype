package com.rkfinance.loanmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
// import androidx.compose.material.icons.filled.Home // Example, change as needed
// import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.rkfinance.loanmanager.ui.navigation.AppNavigation
import com.rkfinance.loanmanager.ui.navigation.Screen
import com.rkfinance.loanmanager.ui.theme.LoanmanagerTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoanmanagerTheme {
                val navController = rememberNavController()
                var currentScreenTitle by remember { mutableStateOf("Loan Manager") }
                var showFab by remember { mutableStateOf(false) }
                var fabAction: (() -> Unit)? by remember { mutableStateOf(null) }
                var showUpButton by remember { mutableStateOf(false) }


                // Listen to navigation changes to update title and FAB visibility
                LaunchedEffect(navController) {
                    navController.currentBackStackEntryFlow.collect { backStackEntry ->
                        val route = backStackEntry.destination.route
                        currentScreenTitle = getTitleForRoute(route)
                        showUpButton = navController.previousBackStackEntry != null &&
                                !isTopLevelDestination(route) // Show up if not a top-level screen

                        // Determine FAB visibility and action based on current screen
                        when {
                            route == Screen.ClientList.route -> {
                                showFab = true
                                fabAction = { navController.navigate(Screen.AddEditClient.createRoute()) }
                            }
                            route?.startsWith(Screen.LoanList.route.substringBefore("/{")) == true -> {
                                val clientId = backStackEntry.arguments?.getInt("clientId")
                                if (clientId != null) {
                                    showFab = true
                                    fabAction = { navController.navigate(Screen.AddEditLoan.createRoute(clientId = clientId)) }
                                } else {
                                    showFab = false
                                }
                            }
                            route?.startsWith(Screen.PaymentList.route.substringBefore("/{")) == true -> {
                                val loanId = backStackEntry.arguments?.getInt("loanId")
                                if (loanId != null) {
                                    showFab = true
                                    fabAction = { navController.navigate(Screen.AddPayment.createRoute(loanId = loanId)) }
                                } else {
                                    showFab = false
                                }
                            }
                            else -> {
                                showFab = false
                                fabAction = null
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(currentScreenTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface, // Or primary for more emphasis
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.primary // Gold for back arrow
                            ),
                            navigationIcon = {
                                if (showUpButton) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        // Only show bottom bar for top-level destinations
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        if (isTopLevelDestination(currentRoute)) {
                            AppBottomNavigationBar(navController = navController)
                        }
                    },
                    floatingActionButton = {
                        if (showFab && fabAction != null) {
                            FloatingActionButton(
                                onClick = { fabAction?.invoke() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(Icons.Filled.Add, "Add Item")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun getTitleForRoute(route: String?): String {
    return when {
        route == Screen.Dashboard.route -> "Dashboard"
        route == Screen.ClientList.route -> "Clients"
        route?.startsWith(Screen.AddEditClient.route.substringBefore("?")) == true -> {
            if (route.contains("clientId=")) "Edit Client" else "Add Client"
        }
        route?.startsWith(Screen.LoanList.route.substringBefore("/{")) == true -> "Client Loans"
        route?.startsWith(Screen.AddEditLoan.route.substringBefore("?")) == true -> {
            if (route.contains("loanId=")) "Edit Loan" else "Add Loan"
        }
        route?.startsWith(Screen.PaymentList.route.substringBefore("/{")) == true -> "Loan Payments"
        route?.startsWith(Screen.AddPayment.route.substringBefore("/{")) == true -> "Record Payment"
        else -> "Loan Manager"
    }
}

// Define your top-level destinations for the bottom nav bar
val topLevelNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Clients,
    // Add more top-level items if you have them, e.g.,
    // BottomNavItem("All Loans", Icons.Filled.MonetizationOn, "all_loans_screen_route") // Create a new screen route
)

fun isTopLevelDestination(route: String?): Boolean {
    return topLevelNavItems.any { it.screen_route == route }
}


sealed class BottomNavItem(val title: String, val icon: ImageVector, val screen_route: String) {
    object Dashboard : BottomNavItem("Dashboard", Icons.Filled.Analytics, Screen.Dashboard.route)
    object Clients : BottomNavItem("Clients", Icons.Filled.Group, Screen.ClientList.route)
    // Example for another top level item:
    // object AllLoans : BottomNavItem("Loans", Icons.Filled.MonetizationOn, Screen.AllLoansScreen.route)
}


@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant // Default for unselected items
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        topLevelNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, style = MaterialTheme.typography.labelMedium) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.screen_route } == true,
                onClick = {
                    navController.navigate(screen.screen_route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // Subtle indicator
                )
            )
        }
    }
}
