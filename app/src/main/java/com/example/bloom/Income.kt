package com.example.bloom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.IncomeData
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.IncomeViewModel
import com.example.bloom.viewmodel.IncomeFilter
import com.example.bloom.viewmodel.DateRangeFilter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: IncomeViewModel = viewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Define colors
    val greenColor = Color(0xFF10B981)
    val blueColor = Color(0xFF3B82F6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
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
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
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
                onClick = { viewModel.showAddDialog() },
                containerColor = greenColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Income",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .windowInsetsPadding(WindowInsets.captionBar)
                        .padding(16.dp)
                ) {
                    // Summary Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "This Month",
                                value = viewModel.formatCurrency(viewModel.totalIncomeThisMonth),
                                subtitle = "Total income",
                                icon = Icons.Default.TrendingUp,
                                backgroundColor = greenColor,
                                iconTint = greenColor,
                                modifier = Modifier.weight(1f)
                            )

                            MetricCard(
                                title = "Average",
                                value = viewModel.formatCurrency(viewModel.averageMonthlyIncome),
                                subtitle = "Per month",
                                icon = Icons.Default.Analytics,
                                backgroundColor = blueColor,
                                iconTint = blueColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Search Bar
                    item {
                        OutlinedTextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search income...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (viewModel.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Filter Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(IncomeFilter.entries) { filter ->
                                FilterChip(
                                    selected = viewModel.selectedFilter == filter,
                                    onClick = { viewModel.updateFilter(filter) },
                                    label = { Text(filter.displayName()) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Date Range Filter
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(DateRangeFilter.entries) { dateRange ->
                                FilterChip(
                                    selected = viewModel.selectedDateRange == dateRange,
                                    onClick = { viewModel.updateDateRange(dateRange) },
                                    label = { Text(dateRange.displayName()) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Results Summary
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
                                Column {
                                    Text(
                                        text = "Filtered Total",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = viewModel.formatCurrency(viewModel.getFilteredTotal()),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = greenColor
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Count",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${viewModel.getIncomeCount()} entries",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Section Header
                    item {
                        Text(
                            text = "Income History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Income List
                    if (viewModel.filteredIncome.isEmpty()) {
                        item {
                            EmptyIncomeCard(
                                message = if (viewModel.searchQuery.isNotEmpty() ||
                                    viewModel.selectedFilter != IncomeFilter.ALL ||
                                    viewModel.selectedDateRange != DateRangeFilter.ALL_TIME) {
                                    "No income found matching your filters"
                                } else {
                                    "No income yet. Add your first income entry!"
                                },
                                onAddClick = { viewModel.showAddDialog() }
                            )
                        }
                    } else {
                        items(viewModel.filteredIncome) { income ->
                            IncomeCard(
                                income = income,
                                onEdit = { viewModel.showEditDialog(income) },
                                onDelete = { viewModel.showDeleteDialog(income) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Loading Overlay
                if (viewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = greenColor)
                    }
                }
            }
        }

        // Add Income Dialog
        if (viewModel.showAddIncomeDialog) {
            AddEditIncomeDialog(
                title = "Add Income",
                income = null,
                onDismiss = { viewModel.hideDialogs() },
                onSave = { income -> viewModel.createIncome(income) }
            )
        }

        // Edit Income Dialog
        if (viewModel.showEditIncomeDialog && viewModel.selectedIncome != null) {
            AddEditIncomeDialog(
                title = "Edit Income",
                income = viewModel.selectedIncome,
                onDismiss = { viewModel.hideDialogs() },
                onSave = { income ->
                    viewModel.selectedIncome?.id?.let { id ->
                        viewModel.updateIncome(id, income)
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (viewModel.showDeleteConfirmation && viewModel.selectedIncome != null) {
            DeleteIncomeDialog(
                incomeName = viewModel.selectedIncome?.source ?: "",
                onDismiss = { viewModel.hideDialogs() },
                onConfirm = {
                    viewModel.selectedIncome?.id?.let { id ->
                        viewModel.deleteIncome(id)
                    }
                }
            )
        }

        // Error Dialog
        viewModel.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearMessages() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK")
                    }
                }
            )
        }

        // Success Snackbar
        viewModel.successMessage?.let { success ->
            LaunchedEffect(success) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearMessages()
            }
        }
    }
}

// =====================================================
// INCOME CARD
// =====================================================

@Composable
fun IncomeCard(
    income: IncomeData,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = try {
        Color(android.graphics.Color.parseColor(income.color_hex ?: "#10B981"))
    } catch (e: Exception) {
        Color(0xFF10B981)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = income.source,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!income.description.isNullOrBlank()) {
                            Text(
                                text = income.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", income.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = income.income_date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (income.is_recurring) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Frequency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = income.recurring_frequency ?: "One-time",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }
            }

            // Tags
            if (!income.tags.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(income.tags.split(",").map { it.trim() }) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// EMPTY STATE
// =====================================================

@Composable
fun EmptyIncomeCard(
    message: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Income")
            }
        }
    }
}

// =====================================================
// ADD/EDIT INCOME DIALOG
// =====================================================

@Composable
fun AddEditIncomeDialog(
    title: String,
    income: IncomeData?,
    onDismiss: () -> Unit,
    onSave: (IncomeData) -> Unit
) {
    var source by remember { mutableStateOf(income?.source ?: "") }
    var amount by remember { mutableStateOf(income?.amount?.toString() ?: "") }
    var date by remember { mutableStateOf(income?.income_date ?: java.time.LocalDate.now().toString()) }
    var description by remember { mutableStateOf(income?.description ?: "") }
    var tags by remember { mutableStateOf(income?.tags ?: "") }
    var isRecurring by remember { mutableStateOf(income?.is_recurring ?: false) }
    var frequency by remember { mutableStateOf(income?.recurring_frequency ?: "Monthly") }
    var colorHex by remember { mutableStateOf(income?.color_hex ?: "#10B981") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source *") },
                    placeholder = { Text("Salary, Freelance, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags") },
                    placeholder = { Text("Comma-separated") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recurring Income")
                }

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("Daily", "Weekly", "Monthly", "Yearly")) { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { frequency = freq },
                                label = { Text(freq) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newIncome = IncomeData(
                        id = income?.id,
                        user_id = income?.user_id ?: "",
                        source = source,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        income_date = date,
                        description = description.ifBlank { null },
                        tags = tags.ifBlank { null },
                        is_recurring = isRecurring,
                        recurring_frequency = if (isRecurring) frequency else null,
                        color_hex = colorHex
                    )
                    onSave(newIncome)
                },
                enabled = source.isNotBlank() && amount.isNotBlank() && date.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =====================================================
// DELETE DIALOG
// =====================================================

@Composable
fun DeleteIncomeDialog(
    incomeName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Income") },
        text = { Text("Are you sure you want to delete the income \"$incomeName\"? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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

// =====================================================
// PREVIEW
// =====================================================

@Preview(showBackground = true)
@Composable
fun IncomeScreenPreview() {
    BloomTheme {
        IncomeScreen(navController = rememberNavController())
    }
}
