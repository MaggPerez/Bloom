package com.example.bloom

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.*
import com.example.bloom.viewmodel.AnalyticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showCustomDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAnalytics() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleExportOptions() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                viewModel.errorMessage != null -> {
                    ErrorView(
                        message = viewModel.errorMessage ?: "Unknown error",
                        onDismiss = { viewModel.clearError() }
                    )
                }
                !viewModel.hasData() -> {
                    EmptyStateView()
                }
                else -> {
                    AnalyticsContent(
                        viewModel = viewModel,
                        onShowCustomDatePicker = { showCustomDatePicker = true }
                    )
                }
            }

            // Export Dialog
            if (viewModel.showExportOptions) {
                AlertDialog(
                    onDismissRequest = { viewModel.toggleExportOptions() },
                    title = { Text("Export Analytics") },
                    text = { Text("Export your analytics report as a PDF") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.exportAsPDF(context) },
                            enabled = !viewModel.isExporting
                        ) {
                            if (viewModel.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Export PDF")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.toggleExportOptions() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AnalyticsContent(
    viewModel: AnalyticsViewModel,
    onShowCustomDatePicker: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Period Selector
        item {
            TimePeriodSelector(
                selectedPeriod = viewModel.selectedTimePeriod,
                onPeriodSelected = { viewModel.selectTimePeriod(it) },
                dateRange = viewModel.getFormattedDateRange()
            )
        }

        // Summary Cards
        item {
            SummaryCards(
                totalIncome = viewModel.totalIncome,
                totalExpenses = viewModel.totalExpenses,
                netSavings = viewModel.netSavings,
                viewModel = viewModel
            )
        }

        // Monthly Trends Chart
        if (viewModel.monthlyTrends.isNotEmpty()) {
            item {
                MonthlyTrendsSection(
                    monthlyTrends = viewModel.monthlyTrends
                )
            }
        }

        // Category Breakdown Chart
        if (viewModel.categoryBreakdown.isNotEmpty()) {
            item {
                CategoryBreakdownSection(
                    categories = viewModel.categoryBreakdown
                )
            }
        }

        // Budget Adherence Chart
        if (viewModel.budgetAdherence.isNotEmpty()) {
            item {
                BudgetAdherenceSection(
                    budgetData = viewModel.budgetAdherence
                )
            }
        }

        // Insights Section
        viewModel.insights?.let { insights ->
            item {
                InsightsSection(
                    insights = insights,
                    viewModel = viewModel
                )
            }
        }

        // Upcoming Bills
        val upcomingBills = viewModel.getUpcomingBills()
        if (upcomingBills.isNotEmpty()) {
            item {
                UpcomingBillsSection(
                    bills = upcomingBills
                )
            }
        }

        // Add spacing at bottom
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =====================================================
// TIME PERIOD SELECTOR
// =====================================================

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    dateRange: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Time Period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimePeriod.values()) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.displayName()) }
                    )
                }
            }
        }
    }
}

// =====================================================
// SUMMARY CARDS
// =====================================================

@Composable
fun SummaryCards(
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    viewModel: AnalyticsViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Income",
                amount = viewModel.formatCurrency(totalIncome),
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Expenses",
                amount = viewModel.formatCurrency(totalExpenses),
                icon = Icons.Default.TrendingDown,
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        SummaryCard(
            title = "Net Savings",
            amount = viewModel.formatCurrency(netSavings),
            icon = Icons.Default.Savings,
            color = if (netSavings >= 0) Color(0xFF3B82F6) else Color(0xFFF59E0B),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            // Icon and title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Value
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// =====================================================
// MONTHLY TRENDS CHART (LINE CHART)
// =====================================================

@Composable
fun MonthlyTrendsSection(
    monthlyTrends: List<MonthlyTrendData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        legend.isEnabled = true
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)
                        legend.textColor = AndroidColor.GRAY

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.textColor = AndroidColor.GRAY
                        xAxis.granularity = 1f

                        axisLeft.textColor = AndroidColor.GRAY
                        axisLeft.setDrawGridLines(true)
                        axisLeft.gridColor = AndroidColor.LTGRAY

                        axisRight.isEnabled = false

                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                    }
                },
                update = { chart ->
                    val incomeEntries = monthlyTrends.mapIndexed { index, data ->
                        Entry(index.toFloat(), data.totalIncome.toFloat())
                    }
                    val expenseEntries = monthlyTrends.mapIndexed { index, data ->
                        Entry(index.toFloat(), data.totalExpenses.toFloat())
                    }

                    val incomeDataSet = LineDataSet(incomeEntries, "Income").apply {
                        color = AndroidColor.rgb(16, 185, 129) // Green
                        lineWidth = 2.5f
                        setCircleColor(AndroidColor.rgb(16, 185, 129))
                        circleRadius = 4f
                        valueTextColor = AndroidColor.GRAY
                        valueTextSize = 9f
                        setDrawFilled(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    val expenseDataSet = LineDataSet(expenseEntries, "Expenses").apply {
                        color = AndroidColor.rgb(239, 68, 68) // Red
                        lineWidth = 2.5f
                        setCircleColor(AndroidColor.rgb(239, 68, 68))
                        circleRadius = 4f
                        valueTextColor = AndroidColor.GRAY
                        valueTextSize = 9f
                        setDrawFilled(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    chart.data = LineData(incomeDataSet, expenseDataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(monthlyTrends.map { it.month })
                    chart.xAxis.labelCount = monthlyTrends.size
                    chart.animateX(800)
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

// =====================================================
// CATEGORY BREAKDOWN CHART (PIE CHART)
// =====================================================

@Composable
fun CategoryBreakdownSection(
    categories: List<CategoryBreakdownData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Take top 8 categories for better visualization
            val topCategories = categories.take(8)

            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        holeRadius = 40f
                        transparentCircleRadius = 45f
                        setDrawCenterText(true)
                        centerText = "Categories"
                        setCenterTextSize(14f)
                        setCenterTextColor(AndroidColor.GRAY)

                        legend.isEnabled = true
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)
                        legend.textColor = AndroidColor.GRAY
                        legend.textSize = 10f
                        legend.formSize = 10f
                        legend.xEntrySpace = 5f
                        legend.yEntrySpace = 2f

                        setEntryLabelColor(AndroidColor.WHITE)
                        setEntryLabelTextSize(11f)

                        setTouchEnabled(true)
                        rotationAngle = 0f
                        isRotationEnabled = true
                        isHighlightPerTapEnabled = true
                    }
                },
                update = { chart ->
                    val entries = topCategories.map { category ->
                        PieEntry(
                            category.percentage,
                            category.categoryName
                        )
                    }

                    val dataSet = PieDataSet(entries, "").apply {
                        colors = topCategories.map { it.categoryColor.toArgb() }
                        sliceSpace = 2f
                        selectionShift = 5f
                        valueTextColor = AndroidColor.WHITE
                        valueTextSize = 11f
                        valueFormatter = PercentFormatter(chart)
                    }

                    chart.data = PieData(dataSet)
                    chart.setUsePercentValues(true)
                    chart.animateY(800)
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            // Category List
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topCategories.forEach { category ->
                    CategoryItem(category)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: CategoryBreakdownData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(category.categoryColor, RoundedCornerShape(2.dp))
            )
            Column {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${category.transactionCount} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$${String.format("%.2f", category.totalSpent)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${String.format("%.1f", category.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =====================================================
// BUDGET ADHERENCE CHART (BAR CHART)
// =====================================================

@Composable
fun BudgetAdherenceSection(
    budgetData: List<BudgetAdherenceData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        legend.isEnabled = true
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)
                        legend.textColor = AndroidColor.GRAY

                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.textColor = AndroidColor.GRAY
                        xAxis.granularity = 1f

                        axisLeft.textColor = AndroidColor.GRAY
                        axisLeft.setDrawGridLines(true)
                        axisLeft.gridColor = AndroidColor.LTGRAY
                        axisLeft.axisMinimum = 0f

                        axisRight.isEnabled = false

                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setPinchZoom(false)
                    }
                },
                update = { chart ->
                    val budgetEntries = budgetData.mapIndexed { index, data ->
                        BarEntry(index.toFloat(), data.budgetAmount.toFloat())
                    }
                    val spentEntries = budgetData.mapIndexed { index, data ->
                        BarEntry(index.toFloat(), data.actualSpent.toFloat())
                    }

                    val budgetDataSet = BarDataSet(budgetEntries, "Budget").apply {
                        color = AndroidColor.rgb(59, 130, 246) // Blue
                        valueTextColor = AndroidColor.GRAY
                        valueTextSize = 9f
                    }

                    val spentDataSet = BarDataSet(spentEntries, "Spent").apply {
                        setColors(budgetData.map { data ->
                            if (data.isOverBudget) {
                                AndroidColor.rgb(239, 68, 68) // Red
                            } else {
                                AndroidColor.rgb(16, 185, 129) // Green
                            }
                        })
                        valueTextColor = AndroidColor.GRAY
                        valueTextSize = 9f
                    }

                    val barData = BarData(budgetDataSet, spentDataSet)
                    barData.barWidth = 0.35f

                    chart.data = barData
                    chart.groupBars(0f, 0.3f, 0f)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(budgetData.map { it.month })
                    chart.xAxis.labelCount = budgetData.size
                    chart.xAxis.setCenterAxisLabels(true)
                    chart.animateY(800)
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            // Budget Summary
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                budgetData.forEach { data ->
                    BudgetItem(data)
                }
            }
        }
    }
}

@Composable
fun BudgetItem(data: BudgetAdherenceData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (data.isOverBudget)
                Color(0xFFEF4444).copy(alpha = 0.1f)
            else
                Color(0xFF10B981).copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            if (data.isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = data.month,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", data.adherencePercentage)}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", data.actualSpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (data.isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981)
                )
                Text(
                    text = "of $${String.format("%.2f", data.budgetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =====================================================
// INSIGHTS SECTION
// =====================================================

@Composable
fun InsightsSection(
    insights: SpendingInsights,
    viewModel: AnalyticsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Budget Health Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = insights.budgetHealthStatus.color().copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Budget Health",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = insights.budgetHealthStatus.displayName(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = insights.budgetHealthStatus.color()
                        )
                        Text(
                            text = insights.budgetHealthStatus.description(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = when (insights.budgetHealthStatus) {
                            BudgetHealthStatus.EXCELLENT -> Icons.Default.CheckCircle
                            BudgetHealthStatus.GOOD -> Icons.Default.ThumbUp
                            BudgetHealthStatus.WARNING -> Icons.Default.Warning
                            BudgetHealthStatus.CRITICAL -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = insights.budgetHealthStatus.color(),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Average Daily Spending
            InsightItem(
                label = "Average Daily Spending",
                value = viewModel.formatCurrency(insights.averageDailySpending),
                icon = Icons.Default.CalendarToday
            )

            // Top Category
            insights.topCategory?.let { category ->
                InsightItem(
                    label = "Top Spending Category",
                    value = "${category.categoryName} (${viewModel.formatCurrency(category.totalSpent)})",
                    icon = Icons.Default.Category
                )
            }

            // Most Frequent Category
            insights.mostFrequentCategory?.let { category ->
                InsightItem(
                    label = "Most Frequent Category",
                    value = category,
                    icon = Icons.Default.Repeat
                )
            }

            // Largest Transaction
            insights.largestTransaction?.let { txn ->
                InsightItem(
                    label = "Largest Transaction",
                    value = "${viewModel.formatCurrency(txn.amount)} in ${txn.categoryName}",
                    icon = Icons.Default.AttachMoney,
                    subtitle = txn.description
                )
            }
        }
    }
}

@Composable
fun InsightItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =====================================================
// UPCOMING BILLS SECTION
// =====================================================

@Composable
fun UpcomingBillsSection(
    bills: List<UpcomingBill>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Bills (Next 7 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            bills.forEach { bill ->
                BillItem(bill)
            }
        }
    }
}

@Composable
fun BillItem(bill: UpcomingBill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                bill.daysUntilDue <= 1 -> Color(0xFFEF4444).copy(alpha = 0.1f)
                bill.daysUntilDue <= 3 -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        bill.daysUntilDue == 0 -> "Due today"
                        bill.daysUntilDue == 1 -> "Due tomorrow"
                        else -> "Due in ${bill.daysUntilDue} days"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        bill.daysUntilDue <= 1 -> Color(0xFFEF4444)
                        bill.daysUntilDue <= 3 -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = bill.frequency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${String.format("%.2f", bill.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =====================================================
// EMPTY & ERROR STATES
// =====================================================

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Data Available",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some transactions to see your analytics",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onDismiss) {
            Text("Dismiss")
        }
    }
}

// =====================================================
// PREVIEW
// =====================================================

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreen(navController = rememberNavController())
}