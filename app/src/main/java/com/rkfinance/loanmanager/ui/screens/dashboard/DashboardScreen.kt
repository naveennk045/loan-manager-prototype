package com.rkfinance.loanmanager.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rkfinance.loanmanager.ui.theme.PrimaryGold
import com.rkfinance.loanmanager.ui.theme.SurfaceDark
import com.rkfinance.loanmanager.ui.viewmodel.DashboardViewModel
import com.rkfinance.loanmanager.utils.formatCurrency // Assuming you have this util

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val dashboardState by viewModel.dashboardUiState.collectAsState()
    // val areaSummaries by viewModel.areaSummariesForChart.collectAsState(initial = emptyList())
    // val overdueLoans by viewModel.overdueLoans.collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme background
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Make content scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Financial Overview",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Example Summary Cards - Replace with actual charts and more detailed info
            SummaryCard(
                title = "Total Principal Loaned",
                value = dashboardState.totalPrincipalLoaned.formatCurrency()
            )
            SummaryCard(
                title = "Total Currently Outstanding",
                value = dashboardState.totalCurrentlyOutstanding.formatCurrency(),
                valueColor = PrimaryGold // Highlight important figures
            )
            SummaryCard(
                title = "Total Repaid (Active Loans)",
                value = dashboardState.totalRepaidOnActiveLoans.formatCurrency()
            )
            SummaryCard(
                title = "Active Loans",
                value = dashboardState.activeLoansCount.toString()
            )
            SummaryCard(
                title = "Overdue Loans",
                value = dashboardState.overdueLoansCount.toString(),
                valueColor = if (dashboardState.overdueLoansCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder for charts
            Text(
                text = "Charts will be displayed here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            // You would iterate through areaSummaries to display them
            // dashboardState.areaSummaries.forEach { summary ->
            //     AreaSummaryCard(areaSummary = summary)
            // }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, valueColor: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(color = valueColor),
            )
        }
    }
}

// @Composable
// fun AreaSummaryCard(areaSummary: AreaSummary) { /* ... UI for area summary ... */ }

// Dummy currency formatter - replace with your actual utility
fun Double.formatCurrency(): String {
    return String.format("$%.2f", this) // Very basic, use proper locale-aware formatting
}
