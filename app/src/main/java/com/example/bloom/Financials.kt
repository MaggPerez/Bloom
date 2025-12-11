package com.example.bloom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme

@Composable
fun FinancialsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Financials",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2x2 Metric Card Layout - First Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialMetricCard(
                        title = "Budget",
                        description = "Manage your monthly budget",
                        icon = Icons.Default.AccountBalanceWallet,
                        backgroundColor = Color(0xFF3B82F6), // Blue
                        iconTint = Color(0xFF3B82F6),
                        onClick = { navController.navigate("budget_screen") },
                        modifier = Modifier.weight(1f)
                    )

                    FinancialMetricCard(
                        title = "Income",
                        description = "View and manage income",
                        icon = Icons.AutoMirrored.Default.TrendingUp,
                        backgroundColor = Color(0xFF10B981), // Green
                        iconTint = Color(0xFF10B981),
                        onClick = { navController.navigate("income_screen") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 Metric Card Layout - Second Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialMetricCard(
                        title = "Transactions",
                        description = "View transaction history",
                        icon = Icons.Default.Receipt,
                        backgroundColor = Color(0xFFEC4899), // Pink
                        iconTint = Color(0xFFEC4899),
                        onClick = { navController.navigate("transaction_screen") },
                        modifier = Modifier.weight(1f)
                    )

                    FinancialMetricCard(
                        title = "Expenses",
                        description = "Track your expenses",
                        icon = Icons.Default.ShoppingCart,
                        backgroundColor = Color(0xFFEF4444), // Red
                        iconTint = Color(0xFFEF4444),
                        onClick = { navController.navigate("expenses_screen") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Analytics Card (Full Width)
                FinancialMetricCard(
                    title = "Analytics",
                    description = "Analyze your financial data and trends",
                    icon = Icons.Default.PieChart,
                    backgroundColor = Color(0xFFF59E0B), // Orange
                    iconTint = Color(0xFFF59E0B),
                    onClick = { navController.navigate("analytics_screen") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
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

            // Title and description
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2
                )
            }
        }
    }
}

@Preview
@Composable
fun FinancialsPreviewScreen() {
    BloomTheme {
        FinancialsScreen(navController = rememberNavController())
    }
}
