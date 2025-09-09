package com.rkfinance.loanmanager.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rkfinance.loanmanager.data.entities.Payment
import com.rkfinance.loanmanager.model.LoanDetails
import com.rkfinance.loanmanager.ui.viewmodel.LoanViewModel
import com.rkfinance.loanmanager.ui.viewmodel.PaymentViewModel
import com.rkfinance.loanmanager.ui.widgets.EmptyListState
import com.rkfinance.loanmanager.utils.formatCurrency
import com.rkfinance.loanmanager.utils.toFormattedDate



@Composable
fun PaymentListScreen(
    navController: NavController,
    paymentViewModel: PaymentViewModel = viewModel(),
    loanViewModel: LoanViewModel = viewModel(), // To get loan details
    loanId: Int // Passed as navigation argument
) {
    LaunchedEffect(loanId) {
        paymentViewModel.setLoanId(loanId)
        loanViewModel.setSelectedLoanId(loanId) // To fetch loan details
    }

    val payments by paymentViewModel.paymentsForLoan.collectAsState(initial = emptyList())
    val loanDetails by loanViewModel.selectedLoanDetails.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            loanDetails?.let { details ->
                LoanSummaryHeader(details) // Display loan summary at the top
            }

            if (payments.isEmpty()) {
                EmptyListState(
                    message = "No payments recorded for this loan yet.",
                    details = "Tap the '+' button to record a payment.",
                    icon = Icons.Filled.Payment
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(payments, key = { payment -> payment.paymentId }) { payment ->
                        PaymentRow(payment = payment, onDeleteClick = {
                            // TODO: Implement confirmation dialog before deleting
                            paymentViewModel.deletePayment(payment)
                        })
                    }
                }
            }
        }
    }
    // FAB is handled by MainActivity
}

@Composable
fun LoanSummaryHeader(loanDetails: LoanDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp), // Add padding
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Loan Overview (ID: ${loanDetails.loan.loanId})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            RowDetail("Principal:", loanDetails.loan.principal.formatCurrency())
            RowDetail("Total Paid:", loanDetails.totalPaid.formatCurrency(), valueColor = MaterialTheme.colorScheme.tertiary)
            RowDetail("Outstanding:", loanDetails.outstandingBalance.formatCurrency(), valueColor = MaterialTheme.colorScheme.primary)
            if (loanDetails.isOverdue) {
                RowDetail("Status:", "OVERDUE", valueColor = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun RowDetail(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            modifier = Modifier.weight(0.6f)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentRow(payment: Payment, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ReceiptLong, // Icon for payment/receipt
                contentDescription = "Payment",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.amount.formatCurrency(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Date: ${payment.paymentDate.toFormattedDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            // Optional: Delete button for a payment
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Payment",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
