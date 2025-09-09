package com.rkfinance.loanmanager.ui.screens.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Check, AttachMoney, CalendarToday, Repeat, Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rkfinance.loanmanager.data.entities.Loan
import com.rkfinance.loanmanager.data.enums.Frequency
import com.rkfinance.loanmanager.ui.screens.client.LuxTextField // Re-use from client screens
import com.rkfinance.loanmanager.ui.viewmodel.LoanViewModel
import com.rkfinance.loanmanager.utils.toFormattedDate
import kotlinx.coroutines.launch
import java.util.Calendar // For default start date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanScreen(
    navController: NavController,
    viewModel: LoanViewModel = viewModel(),
    clientId: Int, // Mandatory: which client this loan belongs to
    loanId: Int?   // Optional: if editing an existing loan
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") } // Store as string, e.g., "10" for 10%
    var selectedFrequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) } // Default to today
    // var endDate by remember { mutableStateOf<Long?>(null) } // Optional

    val isEditing = loanId != null
    var isLoading by remember { mutableStateOf(isEditing) }

    LaunchedEffect(key1 = loanId) {
        if (loanId != null) {
            isLoading = true
            // TODO: Fetch loan details from viewModel.getLoanById(loanId)
            // viewModel.getLoanById(loanId).collect { loanToEdit ->
            //    if (loanToEdit != null) {
            //        principal = loanToEdit.principal.toString()
            //        interestRate = (loanToEdit.interestRate * 100).toString() // Convert 0.1 to "10"
            //        selectedFrequency = loanToEdit.frequency
            //        startDate = loanToEdit.startDate
            //        // endDate = loanToEdit.endDate
            //        isLoading = false
            //    } else if (isEditing) {
            //        navController.popBackStack() // Or show error
            //    }
            // }
            // SIMULATED LOAD FOR EXAMPLE:
            kotlinx.coroutines.delay(300)
            principal = "10000"
            interestRate = "12.5" // As percentage
            selectedFrequency = Frequency.WEEKLY
            startDate = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = {
                        val principalDouble = principal.toDoubleOrNull()
                        val interestRateDouble = interestRate.toDoubleOrNull()?.div(100) // Convert "10" to 0.1

                        if (principalDouble == null || principalDouble <= 0) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Invalid principal amount.") }
                            return@FloatingActionButton
                        }
                        if (interestRateDouble == null || interestRateDouble <= 0) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Invalid interest rate.") }
                            return@FloatingActionButton
                        }

                        val loanToSave = Loan(
                            loanId = loanId ?: 0,
                            clientId = clientId,
                            principal = principalDouble,
                            interestRate = interestRateDouble,
                            frequency = selectedFrequency,
                            startDate = startDate,
                            // endDate = endDate // Implement if needed
                        )
                        coroutineScope.launch {
                            if (isEditing) {
                                viewModel.updateLoan(loanToSave)
                            } else {
                                viewModel.insertLoan(loanToSave)
                            }
                            navController.popBackStack()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Check, "Save Loan", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit Loan Details" else "Add New Loan",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LuxTextField(
                    value = principal,
                    onValueChange = { principal = it },
                    label = "Principal Amount*",
                    leadingIcon = Icons.Filled.AttachMoney,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                LuxTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = "Interest Rate (% per period)*", // e.g., 10 for 10%
                    leadingIcon = Icons.Filled.Percent,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                FrequencyDropdown(
                    selectedFrequency = selectedFrequency,
                    onFrequencySelected = { selectedFrequency = it },
                    leadingIcon = Icons.Filled.Repeat
                )

                // TODO: Implement a Date Picker for Start Date and optional End Date
                OutlinedButton(
                    onClick = { /* Open Date Picker for startDate */
                        coroutineScope.launch { snackbarHostState.showSnackbar("Date Picker for Start Date to be implemented.")}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Start Date", modifier = Modifier.padding(end = 8.dp))
                    Text("Start Date: ${startDate.toFormattedDate()}")
                }

                // Optional End Date Picker
                // OutlinedButton(onClick = { /* Open Date Picker for endDate */ }) {
                //     Text(endDate?.toFormattedDate() ?: "Set End Date (Optional)")
                // }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyDropdown(
    selectedFrequency: Frequency,
    onFrequencySelected: (Frequency) -> Unit,
    leadingIcon: ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val frequencies = Frequency.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField( // Using OutlinedTextField for consistency with LuxTextField's base
            value = selectedFrequency.name.lowercase().replaceFirstChar { it.titlecase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Payment Frequency*") },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = "Frequency") } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = TextFieldDefaults.colors( // Match LuxTextField style
                focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            ),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
        ) {
            frequencies.forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(frequency.name.lowercase().replaceFirstChar { it.titlecase() }, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onFrequencySelected(frequency)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
