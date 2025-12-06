package com.example.bloom

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bloom.datamodels.CategoryBreakdownData
import com.example.bloom.datamodels.MonthlyTrendData
import com.example.bloom.datamodels.TimePeriod
import com.example.bloom.viewmodel.AnalyticsViewModel
import kotlin.math.max

@Composable
fun AnalyticsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current

    // Load data initially
    LaunchedEffect(Unit) {
        if (!viewModel.hasData()) {
            viewModel.loadAnalytics()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.captionBar)
            ) {
                // Header with Title and Export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(onClick = { viewModel.exportAsPDF(context) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Main Content
                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (viewModel.errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = viewModel.errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // Time Period Selectors
                        TimePeriodSelector(
                            selectedPeriod = viewModel.selectedTimePeriod,
                            onPeriodSelected = { viewModel.selectTimePeriod(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Summary Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalyticsSummaryCard(
                                title = "Income",
                                value = viewModel.formatCurrency(viewModel.totalIncome),
                                color = Color(0xFF10B981), // Green
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsSummaryCard(
                                title = "Expenses",
                                value = viewModel.formatCurrency(viewModel.totalExpenses),
                                color = Color(0xFFEF4444), // Red
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsSummaryCard(
                                title = "Savings",
                                value = viewModel.formatCurrency(viewModel.netSavings),
                                color = Color(0xFF3B82F6), // Blue
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Income vs Expenses Chart
                        Text(
                            text = "Income vs Expenses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomBarChart(
                            data = viewModel.monthlyTrends,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Net Savings Trend (Line Graph)
                        Text(
                            text = "Net Savings Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomLineGraph(
                            data = viewModel.monthlyTrends,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Category Breakdown Pie Chart
                        Text(
                            text = "Spending by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CategoryPieChart(
                            categories = viewModel.categoryBreakdown,
                            totalSpent = viewModel.totalExpenses
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Insights Section
                        viewModel.insights?.let { insights ->
                            Text(
                                text = "Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InsightsCard(insights, viewModel)
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(TimePeriod.values()) { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun AnalyticsSummaryCard(
    title: String,
    value: String,
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
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ==========================================
// CUSTOM CHART COMPONENTS
// ==========================================

@Composable
fun CustomBarChart(
    data: List<MonthlyTrendData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartState(modifier, "No data available")
        return
    }

    val incomeColor = Color(0xFF10B981)
    val expenseColor = Color(0xFFEF4444)
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val barWidth = size.width / (data.size * 3f) // Space for 2 bars + gap
            val maxValue = data.maxOfOrNull { max(it.totalIncome, it.totalExpenses) }?.toFloat() ?: 0f
            val scale = if (maxValue > 0) size.height / maxValue else 0f

            // Draw Axis Lines
            drawLine(
                color = axisColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
            )

            data.forEachIndexed { index, item ->
                val xOffset = (index * 3 * barWidth) + barWidth / 2

                // Draw Income Bar
                val incomeHeight = item.totalIncome.toFloat() * scale
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(xOffset, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Draw Expense Bar
                val expenseHeight = item.totalExpenses.toFloat() * scale
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(xOffset + barWidth, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Draw X-Axis Label (Month)
                drawContext.canvas.nativeCanvas.drawText(
                    item.month.take(3), // Shorten month name
                    xOffset + barWidth,
                    size.height + 30f,
                    Paint().apply {
                        color = labelColor
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
fun CustomLineGraph(
    data: List<MonthlyTrendData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartState(modifier, "No trend data")
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.primaryContainer
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val minVal = data.minOfOrNull { it.netAmount }?.toFloat() ?: 0f
            val maxVal = data.maxOfOrNull { it.netAmount }?.toFloat() ?: 0f
            val range = maxVal - minVal
            val safeRange = if (range == 0f) 1f else range
            
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            val path = Path()

            data.forEachIndexed { index, item ->
                // Normalize value to height
                val normalizedY = 1f - ((item.netAmount.toFloat() - minVal) / safeRange)
                val x = index * stepX
                val y = normalizedY * size.height

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw Point
                drawCircle(
                    color = pointColor,
                    radius = 8f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = lineColor,
                    radius = 4f,
                    center = Offset(x, y)
                )
            }

            // Draw Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun CategoryPieChart(
    categories: List<CategoryBreakdownData>,
    totalSpent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (categories.isEmpty() || totalSpent == 0.0) {
                Text(
                    text = "No spending data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pie Chart Canvas
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val canvasSize = size.minDimension
                        val radius = canvasSize / 2f
                        val strokeWidth = radius * 0.4f
                        var startAngle = -90f

                        categories.forEach { category ->
                            val sweepAngle = if (totalSpent > 0) ((category.totalSpent / totalSpent) * 360).toFloat() else 0f

                            drawArc(
                                color = category.categoryColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(
                                    (size.width - canvasSize) / 2,
                                    (size.height - canvasSize) / 2
                                ),
                                size = Size(canvasSize, canvasSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Legend
                    Column(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(5).forEach { category ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(category.categoryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = category.categoryName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${String.format("%.1f", category.percentage)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsCard(
    insights: com.example.bloom.datamodels.SpendingInsights,
    viewModel: AnalyticsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InsightRow(
                label = "Top Spending",
                value = insights.topCategory?.categoryName ?: "None",
                subValue = insights.topCategory?.let { viewModel.formatCurrency(it.totalSpent) } ?: ""
            )
            Spacer(modifier = Modifier.height(8.dp))
            InsightRow(
                label = "Avg Daily Spending",
                value = viewModel.formatCurrency(insights.averageDailySpending),
                subValue = ""
            )
            Spacer(modifier = Modifier.height(8.dp))
            InsightRow(
                label = "Budget Health",
                value = viewModel.getBudgetHealthDescription(),
                valueColor = viewModel.getBudgetHealthColor()
            )
        }
    }
}

@Composable
fun InsightRow(
    label: String,
    value: String,
    subValue: String = "",
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            if (subValue.isNotEmpty()) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyChartState(modifier: Modifier, message: String) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
