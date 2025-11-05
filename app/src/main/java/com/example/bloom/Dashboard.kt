package com.example.bloom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


val supabase = SupabaseClient.client
val user = supabase.auth.currentUserOrNull()

// Data Models for Financial Dashboard
data class BudgetSummary(
    val monthlyBudget: Double,
    val totalSpent: Double,
    val savingsGoal: Double,
    val currentSavings: Double
) {
    val remaining: Double get() = monthlyBudget - totalSpent
    val remainingPercentage: Float get() = ((remaining / monthlyBudget) * 100).toFloat().coerceIn(0f, 100f)
    val spentPercentage: Float get() = ((totalSpent / monthlyBudget) * 100).toFloat()
    val savingsPercentage: Float get() = ((currentSavings / savingsGoal) * 100).toFloat().coerceIn(0f, 100f)
}

data class CategorySpending(
    val category: String,
    val amount: Double,
    val color: Color,
    val icon: ImageVector
)

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Serializable
data class Instrument(
    val id: Int,
    val name: String,
)


// Metric Card Component
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
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon and Title Row
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

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Subtitle or Progress
            if (progressPercentage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressBar(
                    percentage = progressPercentage,
                    color = iconTint
                )
            }

            if (subtitle != null) {
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

// Linear Progress Bar Component
@Composable
fun LinearProgressBar(
    percentage: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
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

// Spending Pie Chart Component
@Composable
fun SpendingPieChart(
    categories: List<CategorySpending>,
    totalSpent: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
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
                // Pie Chart
                PieChart(
                    categories = categories,
                    modifier = Modifier.size(160.dp)
                )

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val percentage = if (totalSpent > 0) (category.amount / totalSpent * 100).toInt() else 0
                        LegendItem(
                            color = category.color,
                            label = category.category,
                            percentage = percentage
                        )
                    }
                }
            }
        }
    }
}

// Pie Chart Drawing Component
@Composable
fun PieChart(
    categories: List<CategorySpending>,
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

        // Center circle for donut effect
        drawCircle(
            color = Color.Transparent,
            radius = radius - strokeWidth,
            center = center
        )
    }
}

// Legend Item Component
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
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
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

// Quick Action Button Component
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
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
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

// Main Dashboard Screen
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val email = user?.email
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Placeholder data - will be replaced with Supabase data
    val budgetSummary = remember {
        BudgetSummary(
            monthlyBudget = 1200.0,
            totalSpent = 847.50,
            savingsGoal = 500.0,
            currentSavings = 325.0
        )
    }

    val categorySpending = remember {
        listOf(
            CategorySpending("Food", 285.0, Color(0xFF4CAF50), Icons.Default.Add),
            CategorySpending("Transport", 120.0, Color(0xFF2196F3), Icons.Default.Add),
            CategorySpending("Education", 200.0, Color(0xFFFFC107), Icons.Default.Add),
            CategorySpending("Entertainment", 142.50, Color(0xFFFF5722), Icons.Default.Add),
            CategorySpending("Bills", 100.0, Color(0xFF9C27B0), Icons.Default.Add)
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = email?.substringBefore("@") ?: "User",
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
                            supabase.auth.signOut()
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

            // Metric Cards Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // Spending Breakdown Chart
            SpendingPieChart(
                categories = categorySpending,
                totalSpent = budgetSummary.totalSpent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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