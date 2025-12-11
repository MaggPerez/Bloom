package com.example.bloom

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.FinancialDataModels
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.DashboardViewModel
import com.example.bloom.viewmodel.AnalyticsViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.Locale
import java.time.LocalDate
import java.time.format.TextStyle
import kotlin.collections.listOf
import androidx.compose.material3.NavigationBarItem
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloom.datamodels.BottomNavItemDataModel
import com.example.bloom.viewmodel.BudgetViewModel


val supabase = SupabaseClient.client
val user = supabase.auth.currentUserOrNull()

@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    budgetViewModel: BudgetViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route


    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()




    // Get current month and year
    val currentDate = remember { LocalDate.now() }
    val monthName = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = currentDate.year

    // Define colors for metrics
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val blueColor = Color(0xFF3B82F6)
    val yellowColor = Color(0xFFFBBF24)

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
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.captionBar)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {

                //header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {

                        //Dashboard Title
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))




                // 2x2 Metric Card Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    //monthly budget and Spent
                    MetricCard(
                        title = "Monthly Budget",
                        value = "$${String.format(Locale.US, "%.0f", budgetViewModel.monthlyBudget)}",
                        subtitle = "Total allocated",
                        icon = Icons.Default.Settings,
                        backgroundColor = blueColor,
                        iconTint = blueColor,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Spent",
                        value = "$${String.format(Locale.US, "%.2f", budgetViewModel.totalSpent)}",
                        subtitle = "${String.format(Locale.US, "%.1f", budgetViewModel.spentPercentage)}% of budget",
                        icon = Icons.Default.ShoppingCart,
                        backgroundColor = redColor,
                        iconTint = redColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Remaining",
                        value = "$${String.format(Locale.US, "%.2f", budgetViewModel.remaining)}",
                        subtitle = "Can still spend",
                        icon = Icons.Default.CheckCircle,
                        backgroundColor = greenColor,
                        iconTint = greenColor,
                        modifier = Modifier.weight(1f),
                        progressPercentage = budgetViewModel.remainingPercentage
                    )

                    MetricCard(
                        title = "Savings Goal",
                        value = "$${String.format(Locale.US, "%.0f", budgetViewModel.currentSavings)}",
                        subtitle = "of $${String.format(Locale.US, "%.0f", budgetViewModel.savingsGoal)}",
                        icon = Icons.Default.Star,
                        backgroundColor = yellowColor,
                        iconTint = yellowColor,
                        modifier = Modifier.weight(1f),
                        progressPercentage = budgetViewModel.savingsPercentage
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                //spending pie chart breakdown
                SpendingPieChartAnalytics(
                    categoryBreakdown = analyticsViewModel.categoryBreakdown,
                    totalExpenses = analyticsViewModel.totalExpenses
                )

                Spacer(modifier = Modifier.height(24.dp))


                //quick action section
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionButton(
                        label = "Transactions",
                        icon = Icons.Default.Add,
                        backgroundColor = blueColor,
                        iconTint = blueColor,
                        onClick = { navController.navigate("transaction_screen") },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        label = "Analytics",
                        icon = Icons.Default.Home,
                        backgroundColor = yellowColor,
                        iconTint = yellowColor,
                        onClick = { navController.navigate("analytics_screen") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        label = "Budget",
                        icon = Icons.Default.Settings,
                        backgroundColor = greenColor,
                        iconTint = greenColor,
                        onClick = { navController.navigate("budget_screen") },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        label = "Expenses",
                        icon = Icons.Default.AccountCircle,
                        backgroundColor = redColor,
                        iconTint = redColor,
                        onClick = { navController.navigate("expenses_screen") },
                        modifier = Modifier.weight(1f)
                    )
                }

            }
        }

    }


}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    progressPercentage: Float? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            //Icon and title row
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
                    modifier = Modifier.size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            //value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            //subtitle or progress
            if(progressPercentage != null){
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressBar(percentage = progressPercentage, color = iconTint)
            }

            if(subtitle != null){
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@Composable
fun LinearProgressBar(
    percentage: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}



@Composable
fun SpendingPieChart(
    categories: List<com.example.bloom.datamodels.CategoryWithBudget>,
    totalSpent: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show empty state if no categories with spending
            if (categories.isEmpty() || totalSpent == 0.0) {
                Text(
                    text = "No spending data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                // Only show categories with spending > 0
                val categoriesWithSpending = categories.filter { it.spent > 0 }

                // MPAndroidChart Pie Chart
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
                        val entries = categoriesWithSpending.map { category ->
                            PieEntry(
                                category.spent.toFloat(),
                                category.name
                            )
                        }

                        val dataSet = PieDataSet(entries, "").apply {
                            colors = categoriesWithSpending.map { it.color.toArgb() }
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
                        .height(250.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesWithSpending.forEach { category ->
                        val percentage = if (totalSpent > 0) (category.spent / totalSpent * 100) else 0.0
                        if (percentage > 0) {
                            LegendItem(
                                color = category.color,
                                label = category.name,
                                percentage = percentage.toInt(),
                                amount = category.spent
                            )
                        }
                    }
                }
            }
        }
    }
}


// Analytics version of SpendingPieChart using CategoryBreakdownData
@Composable
fun SpendingPieChartAnalytics(
    categoryBreakdown: List<com.example.bloom.datamodels.CategoryBreakdownData>,
    totalExpenses: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show empty state if no categories with spending
            if (categoryBreakdown.isEmpty() || totalExpenses == 0.0) {
                Text(
                    text = "No spending data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                // Only show categories with spending > 0
                val categoriesWithSpending = categoryBreakdown.filter { it.totalSpent > 0 }

                // MPAndroidChart Pie Chart
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
                        val entries = categoriesWithSpending.map { category ->
                            PieEntry(
                                category.totalSpent.toFloat(),
                                category.categoryName
                            )
                        }

                        val dataSet = PieDataSet(entries, "").apply {
                            colors = categoriesWithSpending.map { it.categoryColor.toArgb() }
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
                        .height(250.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesWithSpending.forEach { category ->
                        LegendItem(
                            color = category.categoryColor,
                            label = category.categoryName,
                            percentage = category.percentage.toInt(),
                            amount = category.totalSpent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    percentage: Int,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                    .background(color, RoundedCornerShape(2.dp))
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}




@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor.copy(alpha = 0.1f),
            contentColor = iconTint
        ),
        shape = RoundedCornerShape(12.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {

    //getting nav items from data model
    val items = BottomNavItemDataModel.nav_items

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        //navigation bar
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        when (item.icon) {
                            is ImageVector -> {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            }
                            is Int -> {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.label,
                                )
                            }
                        }
                    },
                    label = { Text(item.label) },
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    //changing colors for navigation bar selected to match with the icon tint
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = item.color.copy(alpha = 0.15f),
                        selectedIconColor = item.color,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = item.color,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }

        }
    }
}




@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    BloomTheme {
        DashboardScreen(navController = rememberNavController())
    }
}