package com.rkfinance.loanmanager.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rkfinance.loanmanager.ui.screens.client.AddEditClientScreen
import com.rkfinance.loanmanager.ui.screens.dashboard.DashboardScreen
import com.rkfinance.loanmanager.ui.screens.client.ClientListScreen
import com.rkfinance.loanmanager.ui.screens.loan.AddEditLoanScreen
import com.rkfinance.loanmanager.ui.screens.loan.LoanListScreen
import com.rkfinance.loanmanager.ui.screens.payment.AddPaymentScreen
import com.rkfinance.loanmanager.ui.screens.payment.PaymentListScreen
import com.rkfinance.loanmanager.ui.viewmodel.ClientViewModel
import com.rkfinance.loanmanager.ui.viewmodel.DashboardViewModel
import com.rkfinance.loanmanager.ui.viewmodel.LoanViewModel
import com.rkfinance.loanmanager.ui.viewmodel.PaymentViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    clientViewModel: ClientViewModel = viewModel(),
    loanViewModel: LoanViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route, // Or ClientList.route
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = dashboardViewModel)
        }
        composable(Screen.ClientList.route) {
            ClientListScreen(navController = navController, viewModel = clientViewModel)
        }
        composable(
            route = Screen.AddEditClient.route,
            arguments = listOf(navArgument("clientId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val clientIdString = backStackEntry.arguments?.getString("clientId")
            AddEditClientScreen(
                navController = navController,
                viewModel = clientViewModel, // Or a dedicated AddEditClientViewModel
                clientId = clientIdString?.toIntOrNull()
            )
        }
        composable(
            route = Screen.LoanList.route,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments!!.getInt("clientId")
            loanViewModel.setClientId(clientId) // Important: Set client ID in ViewModel
            LoanListScreen(
                navController = navController,
                viewModel = loanViewModel,
                clientId = clientId
            )
        }
        composable(
            route = Screen.AddEditLoan.route,
            arguments = listOf(
                navArgument("clientId") { type = NavType.IntType },
                navArgument("loanId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments!!.getInt("clientId")
            val loanIdString = backStackEntry.arguments?.getString("loanId")
            AddEditLoanScreen(
                navController = navController,
                viewModel = loanViewModel, // Or dedicated AddEditLoanViewModel
                clientId = clientId,
                loanId = loanIdString?.toIntOrNull()
            )
        }
        composable(
            route = Screen.PaymentList.route,
            arguments = listOf(navArgument("loanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments!!.getInt("loanId")
            paymentViewModel.setLoanId(loanId) // Set loan ID

            // Show PaymentListScreen (replaces the placeholder)
            PaymentListScreen(
                navController = navController,
                paymentViewModel = paymentViewModel,
                loanId = loanId,
                loanViewModel = loanViewModel
            )
        }

        composable(
            route = Screen.AddPayment.route,
            arguments = listOf(navArgument("loanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments!!.getInt("loanId")
            paymentViewModel.setLoanId(loanId)
            AddPaymentScreen(
                navController = navController,
                paymentViewModel = paymentViewModel,
                loanViewModel = loanViewModel, // To display loan info
                loanId = loanId
            )
        }
    }
}