package com.rkfinance.loanmanager.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Map // For Area
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rkfinance.loanmanager.data.entities.Client
import com.rkfinance.loanmanager.data.enums.Area
import com.rkfinance.loanmanager.ui.viewmodel.ClientViewModel // Assuming ClientViewModel has getClientById
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    navController: NavController,
    viewModel: ClientViewModel = viewModel(),
    clientId: Int? // Null if adding, non-null if editing
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var clientName by remember { mutableStateOf("") }
    var clientContact by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") } // Not nullable string for TextField
    var selectedArea by remember { mutableStateOf(Area.PLAINS) }

    val isEditing = clientId != null
    var isLoading by remember { mutableStateOf(isEditing) } // Show loading if editing initially

    // If editing, load client data
    LaunchedEffect(key1 = clientId) {
        if (clientId != null) {
            isLoading = true
            // In a real app, ClientViewModel would expose a Flow for the client
            // For now, this is a placeholder. ViewModel should handle the null case.
            // viewModel.loadClient(clientId) // ViewModel would update its internal state
            // Example: Collect a client StateFlow from ViewModel
            // viewModel.getClientById(clientId).collect { client ->
            //    if (client != null) {
            //        clientName = client.name
            //        clientContact = client.contact
            //        clientAddress = client.address ?: ""
            //        selectedArea = client.area
            //        isLoading = false
            //    } else if (isEditing) {
            //        // Handle case where client to edit is not found
            //        navController.popBackStack() // Or show error
            //    }
            // }
            // SIMULATED LOAD FOR EXAMPLE:
            kotlinx.coroutines.delay(300) // Simulate network/db delay
            clientName = "Existing Client ${clientId}"
            clientContact = "1234567890"
            clientAddress = "123 Main St, Old Town"
            selectedArea = Area.CITY
            isLoading = false
        } else {
            isLoading = false // Not editing, no need to load
        }
    }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // TopBar is handled by MainActivity's Scaffold
        floatingActionButton = {
            if (!isLoading) { // Show FAB only when not loading
                FloatingActionButton(
                    onClick = {
                        val trimmedName = clientName.trim()
                        val trimmedContact = clientContact.trim()
                        val trimmedAddress = clientAddress.trim().takeIf { it.isNotBlank() }

                        if (trimmedName.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Client name cannot be empty.") }
                            return@FloatingActionButton
                        }
                        if (trimmedContact.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Client contact cannot be empty.") }
                            return@FloatingActionButton
                        }

                        val clientToSave = Client(
                            clientId = clientId ?: 0, // Room handles autoGenerate if 0 and new
                            name = trimmedName,
                            contact = trimmedContact,
                            address = trimmedAddress,
                            area = selectedArea
                        )
                        coroutineScope.launch {
                            if (isEditing) {
                                viewModel.updateClient(clientToSave)
                            } else {
                                viewModel.insertClient(clientToSave)
                            }
                            navController.popBackStack()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Check, "Save Client", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
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
                    text = if (isEditing) "Edit Client Details" else "Add New Client",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LuxTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = "Client Name*",
                    leadingIcon = Icons.Filled.Person,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                LuxTextField(
                    value = clientContact,
                    onValueChange = { clientContact = it },
                    label = "Contact Number*",
                    leadingIcon = Icons.Filled.Phone,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                LuxTextField(
                    value = clientAddress,
                    onValueChange = { clientAddress = it },
                    label = "Address (Optional)",
                    leadingIcon = Icons.Filled.Place,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                AreaDropdown(
                    selectedArea = selectedArea,
                    onAreaSelected = { selectedArea = it },
                    label = "Client Area*",
                    leadingIcon = Icons.Filled.Map
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false // You can add error state handling
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = label) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = isError,
        colors = TextFieldDefaults.colors( // M3 TextFieldDefaults.colors
            focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDropdown(
    selectedArea: Area,
    onAreaSelected: (Area) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val areas = Area.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField( // Re-using LuxTextField styling concepts
            value = selectedArea.name.lowercase().replaceFirstChar { it.titlecase() },
            onValueChange = {}, // Not directly changed by typing
            readOnly = true,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = label) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)) // Dropdown background
        ) {
            areas.forEach { area ->
                DropdownMenuItem(
                    text = {
                        Text(
                            area.name.lowercase().replaceFirstChar { it.titlecase() },
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onAreaSelected(area)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

