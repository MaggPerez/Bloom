package com.example.bloom

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.PaymentMethod
import com.example.bloom.datamodels.TransactionFilter
import com.example.bloom.datamodels.TransactionWithCategory
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.BudgetViewModel
import com.example.bloom.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    transactionViewModel: TransactionViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel()
) {
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Colors
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val blueColor = Color(0xFF3B82F6)
    val yellowColor = Color(0xFFFBBF24)

    // Load categories for dropdown
    LaunchedEffect(Unit) {
        // budgetViewModel will load automatically on init
    }

    // Calculate totals
    val totalIncome by remember {
        derivedStateOf {
            transactionViewModel.transactions.filter { it.isIncome }.sumOf { it.amount }
        }
    }

    val totalExpenses by remember {
        derivedStateOf {
            transactionViewModel.transactions.filter { it.isExpense }.sumOf { it.amount }
        }
    }

    val netBalance = totalIncome - totalExpenses

    // Show error messages
    transactionViewModel.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            transactionViewModel.clearError()
        }
    }

    // Show export messages
    transactionViewModel.exportMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            transactionViewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { transactionViewModel.openAddTransactionDialog() },
                containerColor = blueColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val listState = rememberLazyListState()

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    state = listState
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryMetricCard(
                                title = "Income",
                                value = totalIncome,
                                color = greenColor,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricCard(
                                title = "Expenses",
                                value = totalExpenses,
                                color = redColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Net Balance
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Net Balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", netBalance)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netBalance >= 0) greenColor else redColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Search Bar
                    item {
                        OutlinedTextField(
                            value = transactionViewModel.searchQuery,
                            onValueChange = { transactionViewModel.searchTransactions(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search transactions...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (transactionViewModel.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { transactionViewModel.searchTransactions("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Filter Chips
                    item {
                        FilterChipsRow(
                            transactionViewModel = transactionViewModel,
                            onFilterClick = { /* Will open bottom sheet */ }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Transactions Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${transactionViewModel.transactions.size} of ${transactionViewModel.totalTransactionCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Transaction List
                    if (transactionViewModel.transactions.isEmpty() && !transactionViewModel.isLoading) {
                        item {
                            EmptyStateCard(
                                message = "No transactions yet. Tap + to add your first transaction!",
                                onAddClick = { transactionViewModel.openAddTransactionDialog() }
                            )
                        }
                    } else {
                        items(transactionViewModel.transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                onEdit = { transactionViewModel.openEditTransactionDialog(transaction) },
                                onDelete = { transactionViewModel.openDeleteConfirmDialog(transaction) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Load more indicator
                        if (transactionViewModel.hasMoreTransactions) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = { transactionViewModel.loadMoreTransactions() }) {
                                        Text("Load More")
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }

                // Loading Overlay
                if (transactionViewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = blueColor)
                    }
                }
            }
        }

        // Dialogs
        if (transactionViewModel.showAddTransactionDialog) {
            AddEditTransactionDialog(
                title = "Add Transaction",
                transactionViewModel = transactionViewModel,
                onDismiss = { transactionViewModel.closeDialogs() },
                onSave = { transactionViewModel.addTransaction() }
            )
        }

        if (transactionViewModel.showEditTransactionDialog) {
            AddEditTransactionDialog(
                title = "Edit Transaction",
                transactionViewModel = transactionViewModel,
                onDismiss = { transactionViewModel.closeDialogs() },
                onSave = { transactionViewModel.updateTransaction() }
            )
        }

        if (transactionViewModel.showDeleteConfirmDialog) {
            DeleteTransactionDialog(
                transaction = transactionViewModel.selectedTransaction,
                onDismiss = { transactionViewModel.closeDialogs() },
                onConfirm = { transactionViewModel.deleteTransaction() }
            )
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format(Locale.US, "%.2f", value)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FilterChipsRow(
    transactionViewModel: TransactionViewModel,
    onFilterClick: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = transactionViewModel.currentFilter.transactionTypes.contains("expense"),
                onClick = {
                    val types = if (transactionViewModel.currentFilter.transactionTypes.contains("expense")) {
                        transactionViewModel.currentFilter.transactionTypes - "expense"
                    } else {
                        transactionViewModel.currentFilter.transactionTypes + "expense"
                    }
                    transactionViewModel.filterByTransactionType(types)
                },
                label = { Text("Expenses") }
            )
        }

        item {
            FilterChip(
                selected = transactionViewModel.currentFilter.transactionTypes.contains("income"),
                onClick = {
                    val types = if (transactionViewModel.currentFilter.transactionTypes.contains("income")) {
                        transactionViewModel.currentFilter.transactionTypes - "income"
                    } else {
                        transactionViewModel.currentFilter.transactionTypes + "income"
                    }
                    transactionViewModel.filterByTransactionType(types)
                },
                label = { Text("Income") }
            )
        }

        item {
            AssistChip(
                onClick = { /* Show date picker */ },
                label = { Text("This Month") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )
        }

        if (transactionViewModel.isFilterActive) {
            item {
                AssistChip(
                    onClick = { transactionViewModel.clearFilters() },
                    label = { Text("Clear All") },
                    leadingIcon = {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionWithCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category indicator and details
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(transaction.categoryColor)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (transaction.description != null) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatDate(transaction.transactionDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Amount and menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${if (transaction.isExpense) "-" else "+"}$${String.format(Locale.US, "%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    title: String,
    transactionViewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var showPaymentMethodDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Transaction Name
                OutlinedTextField(
                    value = transactionViewModel.formTransactionName,
                    onValueChange = { transactionViewModel.formTransactionName = it },
                    label = { Text("Transaction Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                OutlinedTextField(
                    value = transactionViewModel.formAmount,
                    onValueChange = { transactionViewModel.formAmount = it },
                    label = { Text("Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = transactionViewModel.formTransactionType == "expense",
                        onClick = { transactionViewModel.formTransactionType = "expense" },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionViewModel.formTransactionType == "income",
                        onClick = { transactionViewModel.formTransactionType = "income" },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Date
                OutlinedTextField(
                    value = formatDate(transactionViewModel.formTransactionDate),
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = transactionViewModel.formDescription,
                    onValueChange = { transactionViewModel.formDescription = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Payment Method
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = transactionViewModel.formPaymentMethod?.displayName() ?: "None",
                        onValueChange = {},
                        label = { Text("Payment Method") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showPaymentMethodDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select payment method")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPaymentMethodDropdown = true }
                    )
                    DropdownMenu(
                        expanded = showPaymentMethodDropdown,
                        onDismissRequest = { showPaymentMethodDropdown = false }
                    ) {
                        PaymentMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.displayName()) },
                                onClick = {
                                    transactionViewModel.formPaymentMethod = method
                                    showPaymentMethodDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        transactionViewModel.formTransactionDate = date.toString()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DeleteTransactionDialog(
    transaction: TransactionWithCategory?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Transaction") },
        text = {
            transaction?.let {
                Text("Are you sure you want to delete this ${it.transactionType} of $${String.format(Locale.US, "%.2f", it.amount)} (${it.displayName})?")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val date = LocalDate.parse(dateString, inputFormat)
        date.format(outputFormat)
    } catch (e: Exception) {
        dateString
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionScreenPreview() {
    BloomTheme {
        TransactionScreen(navController = rememberNavController())
    }
}
