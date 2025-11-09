package com.example.bloom

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.FinancialDataModels
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.DashboardViewModel
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
import com.example.bloom.datamodels.BottomNavItemDataModel


//val supabase = SupabaseClient.client
//val user = supabase.auth.currentUserOrNull()
@Serializable
data class Instrument(
    val id: Int,
    val name: String,
)


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
    categories: List<FinancialDataModels.CategorySpending>,
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
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PieChart(
                    categories = categories,
                    modifier = Modifier.size(160.dp)
                )


                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (category, amount, color, icon) ->
                        val percentage = if (totalSpent > 0) (amount / totalSpent * 100).toInt() else 0
                        LegendItem(
                            color = color,
                            label = category,
                            percentage = percentage
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun PieChart(
    categories: List<FinancialDataModels.CategorySpending>,
    modifier: Modifier = Modifier
) {
    val total = categories.sumOf { it.amount }

    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f
        val strokeWidth = radius * 0.4f

        var startAngle = -90f

        categories.forEach { category ->
            val sweepAngle = if (total > 0) ((category.amount / total) * 360).toFloat() else 0f

            drawArc(
                color = category.color,
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

        //center circle for donut effect
        drawCircle(
            color = Color.Transparent,
            radius = radius - strokeWidth,
            center = center
        )
    }
}


@Composable
fun LegendItem(
    color: Color,
    label: String,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(12.dp).clip(CircleShape).background(color)
        )

        Column{
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
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
                }
            )
        }

    }
}





@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route


    //    val email = user?.email
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val budgetSummary = remember {
        FinancialDataModels.BudgetSummary(
            monthlyBudget = 1200.0,
            totalSpent = 847.50,
            savingsGoal = 500.0,
            currentSavings = 325.0
        )
    }

    val categorySpending = remember {
        listOf(
            FinancialDataModels.CategorySpending("Food", 285.0, Color(0xFF4CAF50), Icons.Default.Add),
            FinancialDataModels.CategorySpending("Transport", 120.0, Color(0xFF2196F3), Icons.Default.Add),
            FinancialDataModels.CategorySpending("Education", 200.0, Color(0xFFFFC107), Icons.Default.Add),
            FinancialDataModels.CategorySpending("Entertainment", 142.50, Color(0xFFFF5722), Icons.Default.Add),
            FinancialDataModels.CategorySpending("Bills", 100.0, Color(0xFF9C27B0), Icons.Default.Add)
        )
    }



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
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
//            Text("Hello $email")
//            InstrumentsList()

                    //header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )

                            //placeholder user
                            Text(
                                text = "User",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    navController.navigate("main_screen") {
                                        popUpTo(0) { inclusive = true }
                                    }

                                    //todo supabase signout
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text("Sign Out")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$monthName $year",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))



                    // 2x2 Metric Card Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        //monthly budget and Spent
                        MetricCard(
                            title = "Monthly Budget",
                            value = "$${String.format(Locale.US, "%.0f", budgetSummary.monthlyBudget)}",
                            subtitle = "Total allocated",
                            icon = Icons.Default.Settings,
                            backgroundColor = blueColor,
                            iconTint = blueColor,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Spent",
                            value = "$${String.format(Locale.US, "%.2f", budgetSummary.totalSpent)}",
                            subtitle = "${String.format(Locale.US, "%.1f", budgetSummary.spentPercentage)}% of budget",
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
                            value = "$${String.format(Locale.US, "%.2f", budgetSummary.remaining)}",
                            subtitle = "Can still spend",
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = greenColor,
                            iconTint = greenColor,
                            modifier = Modifier.weight(1f),
                            progressPercentage = budgetSummary.remainingPercentage
                        )

                        MetricCard(
                            title = "Savings Goal",
                            value = "$${String.format(Locale.US, "%.0f", budgetSummary.currentSavings)}",
                            subtitle = "of $${String.format(Locale.US, "%.0f", budgetSummary.savingsGoal)}",
                            icon = Icons.Default.Star,
                            backgroundColor = yellowColor,
                            iconTint = yellowColor,
                            modifier = Modifier.weight(1f),
                            progressPercentage = budgetSummary.savingsPercentage
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                    //spending pie chart breakdown
                    SpendingPieChart(
                        categories = categorySpending,
                        totalSpent = budgetSummary.totalSpent
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
                            label = "Add Transaction",
                            icon = Icons.Default.Add,
                            backgroundColor = blueColor,
                            iconTint = blueColor,
                            onClick = { /* TODO: Navigate to add transaction */ },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionButton(
                            label = "View Categories",
                            icon = Icons.Default.Home,
                            backgroundColor = yellowColor,
                            iconTint = yellowColor,
                            onClick = { /* TODO: Navigate to categories */ },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            label = "Set Budget",
                            icon = Icons.Default.Settings,
                            backgroundColor = greenColor,
                            iconTint = greenColor,
                            onClick = { /* TODO: Navigate to budget settings */ },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionButton(
                            label = "View Reports",
                            icon = Icons.Default.AccountCircle,
                            backgroundColor = redColor,
                            iconTint = redColor,
                            onClick = { /* TODO: Navigate to reports */ },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))



                    Button(
                        onClick = {
                            coroutineScope.launch {
                                navController.navigate("main_screen") {
                                    popUpTo(0) { inclusive = true }
                                }
//                        supabase.auth.signOut()
                            }
                        }
                    ) {
                        Text(text = "Sign Out")
                    }
            }
        }

    }


}

//@Composable
//fun InstrumentsList() {
//    var instruments by remember { mutableStateOf<List<Instrument>>(listOf()) }
//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.IO) {
//            instruments = supabase.from("instruments")
//                .select().decodeList<Instrument>()
//        }
//    }
//    LazyColumn {
//        items(
//            instruments,
//            key = { instrument -> instrument.id },
//        ) { instrument ->
//            Text(
//                instrument.name,
//                modifier = Modifier.padding(8.dp),
//            )
//        }
//    }
//}



@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    BloomTheme {
        DashboardScreen(navController = rememberNavController())
    }
}