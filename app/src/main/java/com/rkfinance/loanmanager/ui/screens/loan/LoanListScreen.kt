package com.rkfinance.loanmanager.ui.screens.loan

import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description // Icon for loan
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rkfinance.loanmanager.data.entities.Loan
import com.rkfinance.loanmanager.ui.navigation.Screen
import com.rkfinance.loanmanager.ui.widgets.EmptyListState
import com.rkfinance.loanmanager.ui.theme.PrimaryGold
import com.rkfinance.loanmanager.ui.viewmodel.LoanViewModel
import com.rkfinance.loanmanager.utils.formatCurrency
import com.rkfinance.loanmanager.utils.toFormattedDate

@Composable
fun LoanListScreen(
    navController: NavController,
    viewModel: LoanViewModel = viewModel(),
    clientId: Int // Passed as navigation argument
) {
    // Ensure the ViewModel is aware of the current clientID
    LaunchedEffect(clientId) {
        viewModel.setClientId(clientId)
    }

    val loans by viewModel.loansForClient.collectAsState(initial = emptyList())
    // val clientDetails by viewModel.getClientDetails(clientId).collectAsState(initial = null) // Optional: If you want to show client name

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (loans.isEmpty()) {
            EmptyListState(
                message = "No loans found for this client.",
                details = "Tap the '+' button to add a new loan.",
                icon = Icons.Filled.AddCard // Icon for adding a loan/card
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Optional: Header with Client Name
                // item {
                //     clientDetails?.let { client ->
                //         Text(
                //             "Loans for: ${client.name}",
                //             style = MaterialTheme.typography.headlineSmall,
                //             modifier = Modifier.padding(bottom = 8.dp),
                //             color = MaterialTheme.colorScheme.onBackground
                //         )
                //     }
                // }
                items(loans, key = { loan -> loan.loanId }) { loan ->
                    LoanRow(
                        loan = loan,
                        onLoanClick = {
                            // Navigate to PaymentListScreen for this loan
                            navController.navigate(Screen.PaymentList.createRoute(loan.loanId))
                        },
                        onEditClick = {
                            // Navigate to AddEditLoanScreen for editing this loan
                            navController.navigate(Screen.AddEditLoan.createRoute(clientId = loan.clientId, loanId = loan.loanId))
                        }
                    )
                }
            }
        }
    }
    // FAB is handled by MainActivity
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanRow(loan: Loan, onLoanClick: () -> Unit, onEditClick: () -> Unit) {
    Card(
        // Whole card is clickable to view payments
        onClick = onLoanClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Description, // Icon for a loan/document
                        contentDescription = "Loan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Loan ID: ${loan.loanId}", // Or a more descriptive title
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Principal: ${loan.principal.formatCurrency()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "View Payments",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            LoanDetailItem("Interest Rate:", "${String.format("%.2f", loan.interestRate * 100)}% per Period")
            LoanDetailItem("Frequency:", loan.frequency.name.lowercase().replaceFirstChar { it.titlecase() })
            LoanDetailItem("Start Date:", loan.startDate.toFormattedDate())
            loan.endDate?.let {
                LoanDetailItem("End Date:", it.toFormattedDate())
            }

            // Placeholder for Outstanding Balance - This would ideally come from LoanDetails in ViewModel
            // LoanDetailItem("Outstanding:", "Calculating...", valueColor = PrimaryGold)

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onEditClick, modifier = Modifier.align(Alignment.End)) {
                Text("Edit Loan", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun LoanDetailItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            modifier = Modifier.weight(0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
