package com.example.bloom.aifeatures

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.BottomNavigationBar
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.SmartInsightsViewModel

@Composable
fun SmartInsightsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SmartInsightsViewModel = viewModel()
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "Smart Insights",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Discover patterns in your spending and get actionable recommendations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Summary Card
                SummaryCard(
                    totalSpent = viewModel.totalSpent,
                    totalIncome = viewModel.totalIncome
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Insights Section
                Text(
                    text = "Your Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Loading state
                if (viewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (viewModel.insights.isEmpty()) {
                    // Empty state
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                imageVector = Icons.Default.Info,
                                contentDescription = "No insights",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No insights available yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Add transactions and budget data to get personalized insights",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Insights list
                    viewModel.insights.forEach { insight ->
                        InsightCard(insight = insight)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // AI Insights Section
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "AI-Powered Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                viewModel.aiInsights?.let { aiText ->
                    AIInsightsCard(insights = aiText)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = { viewModel.generateAIInsights() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoadingAI,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isLoadingAI) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Generate",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (viewModel.aiInsights == null) "Generate AI Insights" else "Regenerate AI Insights")
                    }
                }

                // Error message
                viewModel.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    totalSpent: Double,
    totalIncome: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", totalIncome)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", totalSpent)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Net",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val net = totalIncome - totalSpent
                Text(
                    text = "$${String.format("%.2f", net)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (net >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    insight: SmartInsightsViewModel.Insight,
    modifier: Modifier = Modifier
) {
    val iconAndColor = getInsightIconAndColor(insight.type)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconAndColor.second.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconAndColor.first,
                    contentDescription = insight.title,
                    tint = iconAndColor.second,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                insight.value?.let { value ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = iconAndColor.second
                    )
                }

                insight.change?.let { change ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (change > 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (change > 0) "Increase" else "Decrease",
                            tint = if (change > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", kotlin.math.abs(change))}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (change > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIInsightsCard(
    insights: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "AI",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = insights,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun getInsightIconAndColor(type: SmartInsightsViewModel.InsightType): Pair<ImageVector, Color> {
    return when (type) {
        SmartInsightsViewModel.InsightType.SPENDING_TREND -> Pair(Icons.Default.DateRange, Color(0xFF3B82F6))
        SmartInsightsViewModel.InsightType.TOP_CATEGORY -> Pair(Icons.Default.Star, Color(0xFFFBBF24))
        SmartInsightsViewModel.InsightType.DAY_PATTERN -> Pair(Icons.Default.DateRange, Color(0xFF8B5CF6))
        SmartInsightsViewModel.InsightType.BUDGET_ALERT -> Pair(Icons.Default.Warning, Color(0xFFEF4444))
        SmartInsightsViewModel.InsightType.SAVINGS_TIP -> Pair(Icons.Default.Settings, Color(0xFF10B981))
        SmartInsightsViewModel.InsightType.POSITIVE -> Pair(Icons.Default.CheckCircle, Color(0xFF10B981))
        SmartInsightsViewModel.InsightType.WARNING -> Pair(Icons.Default.Warning, Color(0xFFF97316))
    }
}

@Preview(showBackground = true)
@Composable
fun SmartInsightsScreenPreview() {
    BloomTheme {
        SmartInsightsScreen(navController = rememberNavController())
    }
}
