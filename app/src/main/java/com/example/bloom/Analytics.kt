package com.example.bloom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bloom.datamodels.TimePeriod
import com.example.bloom.datamodels.CategoryBreakdownData
import com.example.bloom.datamodels.MonthlyTrendData
import com.example.bloom.viewmodel.AnalyticsViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    viewModel: AnalyticsViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                actions = {
                    // Time period filter
                    var showTimePeriodMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = { showTimePeriodMenu = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Period")
                    }

                    DropdownMenu(
                        expanded = showTimePeriodMenu,
                        onDismissRequest = { showTimePeriodMenu = false }
                    ) {
                        TimePeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.displayName()) },
                                onClick = {
                                    viewModel.loadAnalytics(period)
                                    showTimePeriodMenu = false
                                }
                            )
                        }
                    }

                    // Export/Share button
                    IconButton(onClick = { viewModel.showExportOptions = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards
                item {
                    SummaryCards(
                        totalIncome = viewModel.totalIncome,
                        totalExpenses = viewModel.totalExpenses,
                        netSavings = viewModel.netSavings
                    )
                }

                // Monthly Trends Chart
                item {
                    if (viewModel.monthlyTrends.isNotEmpty()) {
                        MonthlyTrendsChart(
                            data = viewModel.monthlyTrends,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Income vs Expenses Chart
                item {
                    if (viewModel.monthlyTrends.isNotEmpty()) {
                        IncomeExpensesBarChart(
                            data = viewModel.monthlyTrends,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Category Breakdown
                item {
                    if (viewModel.categoryBreakdown.isNotEmpty()) {
                        CategoryBreakdownSection(
                            categories = viewModel.categoryBreakdown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Budget Adherence
                item {
                    if (viewModel.budgetAdherence.isNotEmpty()) {
                        BudgetAdherenceChart(
                            data = viewModel.budgetAdherence,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Insights
                item {
                    viewModel.insights?.let { insights ->
                        InsightsSection(
                            insights = insights,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Error message
        viewModel.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun SummaryCards(
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Income",
            amount = totalIncome,
            icon = Icons.Default.ArrowDownward,
            iconTint = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Expenses",
            amount = totalExpenses,
            icon = Icons.Default.ArrowUpward,
            iconTint = Color(0xFFEF4444),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Net Savings",
            amount = netSavings,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconTint = if (netSavings >= 0) Color(0xFF3B82F6) else Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonthlyTrendsChart(
    data: List<MonthlyTrendData>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries {
                    series(data.map { it.netAmount })
                }
            }
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Monthly Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Net Amount (Income - Expenses)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun IncomeExpensesBarChart(
    data: List<MonthlyTrendData>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries {
                    series(data.map { it.totalIncome })
                    series(data.map { it.totalExpenses })
                }
            }
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Income vs Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(
                    color = Color(0xFF10B981),
                    label = "Income"
                )
                LegendItem(
                    color = Color(0xFFEF4444),
                    label = "Expenses"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
fun CategoryBreakdownSection(
    categories: List<CategoryBreakdownData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            categories.take(5).forEach { category ->
                CategoryBreakdownItem(
                    category = category,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CategoryBreakdownItem(
    category: CategoryBreakdownData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(category.categoryColor, RoundedCornerShape(2.dp))
                )
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", category.totalSpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", category.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { category.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = category.categoryColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun BudgetAdherenceChart(
    data: List<com.example.bloom.datamodels.BudgetAdherenceData>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries {
                    series(data.map { it.budgetAmount })
                    series(data.map { it.actualSpent })
                }
            }
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Budget Adherence",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(
                    color = Color(0xFF3B82F6),
                    label = "Budget"
                )
                LegendItem(
                    color = Color(0xFFF59E0B),
                    label = "Actual"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
fun InsightsSection(
    insights: com.example.bloom.datamodels.SpendingInsights,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Spending Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Budget Health Status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = insights.budgetHealthStatus.color().copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Budget Health: ${insights.budgetHealthStatus.displayName()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = insights.budgetHealthStatus.color()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insights.budgetHealthStatus.description(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Average Daily Spending
            InsightItem(
                label = "Average Daily Spending",
                value = "$${String.format("%.2f", insights.averageDailySpending)}"
            )

            // Top Category
            insights.topCategory?.let { category ->
                Spacer(modifier = Modifier.height(8.dp))
                InsightItem(
                    label = "Top Spending Category",
                    value = "${category.categoryName} ($${String.format("%.2f", category.totalSpent)})"
                )
            }

            // Most Frequent Category
            insights.mostFrequentCategory?.let { category ->
                Spacer(modifier = Modifier.height(8.dp))
                InsightItem(
                    label = "Most Frequent Category",
                    value = category
                )
            }

            // Largest Transaction
            insights.largestTransaction?.let { transaction ->
                Spacer(modifier = Modifier.height(8.dp))
                InsightItem(
                    label = "Largest Transaction",
                    value = "$${String.format("%.2f", transaction.amount)} - ${transaction.categoryName}"
                )
            }
        }
    }
}

@Composable
fun InsightItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
